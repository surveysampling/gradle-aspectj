package aspectj

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Brian Connolly
 */
class AspectJPluginTest extends Specification {

    private static final String SOURCE_SET_INTEG_TEST = 'integTest'
    private static final String SOURCE_SET_SLOW_INTEG_TEST = 'slowIntegTest'

    private static final String CONFIGURATION_AJTOOLS = 'ajtools'
    private static final String CONFIGURATION_ASPECTPATH = 'aspectpath'
    private static final String CONFIGURATION_AJ_INPATH = 'ajInpath'

    private static final String DEFAULT_AJ_VERSION = '1.8.12'
    private static final String PROPERTY_ASPECTJ_VERSION = 'aspectjVersion'

    @Rule
    private TemporaryFolder mainSourceFolder = new TemporaryFolder()

    @Rule
    private TemporaryFolder testSourceFolder = new TemporaryFolder()

    @Rule
    private TemporaryFolder integTestSourceFolder = new TemporaryFolder()

    private Project project

    @Unroll
    def "Apply ajVersion=#ajVersion"() {

        setup:
        project = new ProjectBuilder().build()
        project.plugins.apply(JavaPlugin)

        mainSourceFolder.create()
        mainSourceFolder.newFile('main.java')

        testSourceFolder.create()
        testSourceFolder.newFile('test.java')

        integTestSourceFolder.create()
        integTestSourceFolder.newFile('integTest.java')

        final JavaPluginConvention javaPluginConvention = project
                .convention
                .getPlugin(JavaPluginConvention)

        final SourceSet main = getAndConfigureSourceSet(javaPluginConvention, SourceSet.MAIN_SOURCE_SET_NAME, mainSourceFolder)
        final SourceSet test = getAndConfigureSourceSet(javaPluginConvention, SourceSet.TEST_SOURCE_SET_NAME, testSourceFolder)
        final SourceSet integTest = getAndConfigureSourceSet(javaPluginConvention, SOURCE_SET_INTEG_TEST, integTestSourceFolder)
        final SourceSet slowIntegTest = getAndConfigureSourceSet(javaPluginConvention, SOURCE_SET_SLOW_INTEG_TEST, null)

        if (ajVersion != null) {
            project
                    .extensions
                    .getByType(ExtraPropertiesExtension)
                    .set(PROPERTY_ASPECTJ_VERSION, ajVersion)
        }

        when:
        project.plugins.apply(AspectJPlugin)

        then:
        final AspectJExtension extension = project.extensions.getByType(AspectJExtension)
        extension.version == (ajVersion != null ? ajVersion : DEFAULT_AJ_VERSION)

        assertAspectJDependencies(project, ajVersion)
        assertSourceSet(project, main)
        assertSourceSet(project, test)
        assertSourceSet(project, integTest)
        assertSourceSet(project, slowIntegTest)

        cleanup:
        mainSourceFolder.delete()
        testSourceFolder.delete()

        where:
        ajVersion << [null, '1.9.0']
    }

    @Unroll
    def "Apply No Source ajVersion=#ajVersion"() {

        setup:
        project = new ProjectBuilder().build()
        project.plugins.apply(JavaPlugin)

        final JavaPluginConvention javaPluginConvention = project
                .convention
                .getPlugin(JavaPluginConvention)

        final SourceSet main = getAndConfigureSourceSet(javaPluginConvention, SourceSet.MAIN_SOURCE_SET_NAME, null)
        final SourceSet test = getAndConfigureSourceSet(javaPluginConvention, SourceSet.TEST_SOURCE_SET_NAME, null)

        if (ajVersion != null) {
            project
                    .extensions
                    .getByType(ExtraPropertiesExtension)
                    .set(PROPERTY_ASPECTJ_VERSION, ajVersion)
        }

        when:
        project.plugins.apply(AspectJPlugin)
        project.evaluate()

        then:
        final AspectJExtension extension = project.extensions.getByType(AspectJExtension)
        extension.version == (ajVersion != null ? ajVersion : DEFAULT_AJ_VERSION)

        assertAspectJDependencies(project, ajVersion)
        assertSourceSet(project, main)
        assertSourceSet(project, test)

        where:
        ajVersion << [null, '1.9.0']
    }

    private static SourceSet getAndConfigureSourceSet(final JavaPluginConvention javaPluginConvention,
                                                      final String name,
                                                      final TemporaryFolder temporaryFolder) {

        final SourceSet sourceSet = javaPluginConvention
                .sourceSets
                .maybeCreate(name)

        if (temporaryFolder != null) {
            sourceSet.java.srcDir(temporaryFolder.root)
        }

        sourceSet
    }

    private static void assertAspectJDependencies(final Project project, final String ajVersion) {

        final Configuration implementation = project.configurations[JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME]
        final Configuration ajTools = project.configurations[CONFIGURATION_AJTOOLS]

        assert implementation
                .dependencies
                .stream()
                .map({ final Dependency dependency -> convertToDependencyNotation(dependency) })
                .filter({ final String value -> value == "org.aspectj:aspectjrt:${getVersion(ajVersion)}" })
                .findAny()
                .present

        assert ajTools
                .dependencies
                .stream()
                .map({ final Dependency dependency -> convertToDependencyNotation(dependency) })
                .filter({ final String value -> value == "org.aspectj:aspectjtools:${getVersion(ajVersion)}" })
                .findAny()
                .present
    }

    private static void assertSourceSet(final Project project, final SourceSet sourceSet) {

        final TaskCollection<Ajc> ajcTasks = project.tasks.withType(Ajc)

        final Configuration aspectPath = project.configurations[getConfigurationName(sourceSet.name, CONFIGURATION_ASPECTPATH)]
        final Configuration ajInPath = project.configurations[getConfigurationName(sourceSet.name, CONFIGURATION_AJ_INPATH)]

        final JavaCompile compileJava = project
                .tasks
                .withType(JavaCompile)
                .getByName(getCompileTaskName(sourceSet.name, 'Java'))

        if (!sourceSet.java.isEmpty()) {

            final Ajc compileAspect = ajcTasks[getCompileTaskName(sourceSet.name, 'Aspect')]

            assert compileJava.actions.isEmpty()
            assert compileJava.taskDependencies.getDependencies(compileJava).contains(compileAspect)

            assert compileAspect.classpath == sourceSet.compileClasspath
            assert compileAspect.sourceDirectories == sourceSet.java.srcDirs
            assert compileAspect.destinationDir == sourceSet.java.outputDir
            assert compileAspect.dependsOn.contains(aspectPath)
            assert compileAspect.dependsOn.contains(ajInPath)

        } else {

            assert !project
                    .tasks
                    .withType(Ajc)
                    .stream()
                    .filter({ final Ajc task -> task.name == getCompileTaskName(sourceSet.name, 'Aspect') })
                    .findAny()
                    .present

            assert !compileJava.actions.isEmpty()
        }
    }

    private static String getVersion(final String version) {
        version != null ? version : DEFAULT_AJ_VERSION
    }

    private static String convertToDependencyNotation(final Dependency dependency) {
        "${dependency.group}:${dependency.name}:${dependency.version}"
    }

    private static String getConfigurationName(final String sourceSetName, final String baseConfiguration) {

        if (sourceSetName == SourceSet.MAIN_SOURCE_SET_NAME) {
            return baseConfiguration
        }

        return sourceSetName + baseConfiguration.capitalize()
    }

    private static String getCompileTaskName(final String sourceSetName, final String postFix) {

        if (sourceSetName == SourceSet.MAIN_SOURCE_SET_NAME) {
            return "compile${postFix}"
        }

        return "compile${sourceSetName.capitalize()}${postFix}"
    }
}
