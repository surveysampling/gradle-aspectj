package aspectj

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.internal.impldep.com.google.common.collect.ImmutableMap
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
        ajc.sourceSet = sourceSet
        ajc.aspectpath = aspectpath
        ajc.ajInpath = ajInpath
        ajc.xlint = xlint
        ajc.iajc = iajc

        final String destDir = sourceSet.java.outputDir.absolutePath

        final Map<String, ?> expectedArgs = [classpath           : sourceSet.compileClasspath.asPath,
                                             destDir             : destDir,
                                             s                   : destDir,
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
        1 * iajc.execute(project, sourceSet, _ as Map<String, ?>) >> {
            final Project p, final SourceSet s, final Map<String, ?> value ->
                assert p == project
                assert s == sourceSet
                assert ImmutableMap.copyOf(value) == ImmutableMap.copyOf(expectedArgs)
        }

        where:
        xlint << ['ignore', 'warning']
    }

    @Unroll
    def "Compile sourceSet=#sourceSet aspectpath=#aspectpath ajInpath=#ajInpath"() {

        setup:
        ajc.sourceSet = sourceSet
        ajc.aspectpath = aspectpath
        ajc.ajInpath = ajInpath
        ajc.iajc = iajc

        when:
        ajc.compile()

        then:
        thrown(IllegalArgumentException)

        where:
        sourceSet       || aspectpath           || ajInpath
        null            || Mock(FileCollection) || Mock(FileCollection)
        Mock(SourceSet) || null                 || Mock(FileCollection)
        Mock(SourceSet) || Mock(FileCollection) || null
    }

    def "Create"() {

        expect:
        ajc.aspectpath == null
        ajc.ajInpath == null
        ajc.sourceSet == null
        ajc.additionalAjcArgs.isEmpty()
        ajc.xlint == 'ignore'
        ajc.maxmem == null
    }
}
