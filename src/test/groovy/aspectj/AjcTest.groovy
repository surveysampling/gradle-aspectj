package aspectj

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.internal.impldep.com.google.common.collect.ImmutableMap
import org.gradle.internal.impldep.com.google.common.collect.ImmutableSet
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 * @author Brian Connolly
 */
class AjcTest extends Specification {

    private Project project

    private JavaPluginConvention javaPluginConvention
    private Configuration ajtools

    private SourceSet sourceSet
    private FileCollection aspectpath
    private FileCollection ajInpath

    private Iajc iajc

    @Subject
    private Ajc ajc

    def setup() {
        project = new ProjectBuilder().build()

        project.plugins.apply(JavaPlugin)
        ajtools = project.configurations.create('ajtools')

        javaPluginConvention = project.convention.getPlugin(JavaPluginConvention)
        javaPluginConvention.sourceCompatibility = JavaVersion.VERSION_1_8
        javaPluginConvention.targetCompatibility = JavaVersion.VERSION_1_8

        sourceSet = javaPluginConvention.sourceSets.getByName('main')
        aspectpath = project.files('somelocation/aspects')
        ajInpath = project.files('somelocation/java')

        iajc = Mock(Iajc)

        ajc = project.tasks.create('ajc', Ajc)
    }

    @Unroll
    def "Compile xlint=#xlint"() {

        setup:
        final FileCollection classpath = sourceSet.compileClasspath
        final File destinationDir = sourceSet.java.outputDir
        final Set<File> srcDirs = sourceSet.java.srcDirs

        ajc.classpath = classpath
        ajc.destinationDir = destinationDir
        ajc.sourceDirectories = srcDirs
        ajc.aspectPath = aspectpath
        ajc.ajInPath = ajInpath
        ajc.xlint = xlint
        ajc.iajc = iajc

        final Map<String, ?> expectedArgs = [classpath           : classpath.asPath,
                                             destDir             : destinationDir.absolutePath,
                                             s                   : destinationDir.absolutePath,
                                             source              : javaPluginConvention.sourceCompatibility,
                                             target              : javaPluginConvention.targetCompatibility,
                                             inpath              : ajInpath.asPath,
                                             xlint               : xlint,
                                             fork                : true,
                                             aspectPath          : aspectpath.asPath,
                                             sourceRootCopyFilter: '**/*.java,**/*.aj',
                                             showWeaveInfo       : true]

        when:
        ajc.compile()

        then:
        1 * iajc.execute(project, srcDirs, _ as Map<String, ?>) >> {
            final Project p, final Set<File> s, final Map<String, ?> value ->
                assert p == project
                assert s == srcDirs
                assert ImmutableMap.copyOf(value) == ImmutableMap.copyOf(expectedArgs)
        }

        where:
        xlint << ['ignore', 'warning']
    }

    @Unroll
    def """Compile
        classpath=#classpath
        srcDirs=#srcDirs
        destinationDir=#destinationDir
        aspectpath=#aspectpath
        ajInpath=#ajInpath"""() {

        setup:
        ajc.classpath = classpath
        ajc.sourceDirectories = srcDirs
        ajc.destinationDir = destinationDir
        ajc.aspectPath = aspectpath
        ajc.ajInPath = ajInpath
        ajc.iajc = iajc

        when:
        ajc.compile()

        then:
        thrown(IllegalArgumentException)

        where:
        classpath            || srcDirs           || destinationDir || aspectpath           || ajInpath
        null                 || ImmutableSet.of() || Mock(File)     || Mock(FileCollection) || Mock(FileCollection)
        Mock(FileCollection) || null              || Mock(File)     || Mock(FileCollection) || Mock(FileCollection)
        Mock(FileCollection) || ImmutableSet.of() || null           || Mock(FileCollection) || Mock(FileCollection)
        Mock(FileCollection) || ImmutableSet.of() || Mock(File)     || null                 || Mock(FileCollection)
        Mock(FileCollection) || ImmutableSet.of() || Mock(File)     || Mock(FileCollection) || null
    }

    def "Create"() {

        expect:
        ajc.classpath == null
        ajc.sourceDirectories == null
        ajc.destinationDir == null
        ajc.aspectPath == null
        ajc.ajInPath == null
        ajc.additionalAjcArgs.isEmpty()
        ajc.xlint == 'ignore'
        ajc.maxmem == null
    }
}
