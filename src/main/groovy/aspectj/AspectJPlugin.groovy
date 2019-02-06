package aspectj

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile

/**
 *
 * @author Luke Taylor
 * @author Mike Noordermeer
 */
class AspectJPlugin implements Plugin<Project> {

    private static final String CONFIGURATION_AJTOOLS = 'ajtools'

    void apply(final Project project) {

        project
                .plugins
                .apply(JavaPlugin)

        final AspectJExtension aspectj = project
                .extensions
                .create('aspectj', AspectJExtension, project)

        if (project.configurations.findByName(CONFIGURATION_AJTOOLS) == null) {
            configureAspectJDependencies(project, aspectj)
        }

        project
                .convention
                .getPlugin(JavaPluginConvention)
                .sourceSets
                .forEach({ final SourceSet sourceSet -> configureSourceSet(project, sourceSet) })
    }

    private static void configureAspectJDependencies(final Project project, final AspectJExtension extension) {

        project
                .configurations
                .maybeCreate(CONFIGURATION_AJTOOLS)

        project.afterEvaluate { final Project p ->
            if (extension.version == null) {
                throw new GradleException("No aspectj version supplied")
            }

            final DependencyHandler dependencyHandler = p.dependencies

            dependencyHandler.add(CONFIGURATION_AJTOOLS, "org.aspectj:aspectjtools:${extension.version}")
            dependencyHandler.add(JavaPlugin.COMPILE_CONFIGURATION_NAME, "org.aspectj:aspectjrt:${extension.version}")
        }
    }

    private static void configureSourceSet(final Project project, final SourceSet sourceSet) {

        final NamingConventions namingConventions = getConfigurationNamingConventions(sourceSet)
        project.configurations.maybeCreate(namingConventions.getAspectPathConfigurationName(sourceSet))
        project.configurations.maybeCreate(namingConventions.getAspectInpathConfigurationName(sourceSet))

        if (!sourceSet.java.isEmpty()) {
            configureTasks(project, namingConventions, sourceSet)
        }
    }

    private static void configureTasks(final Project project,
                                       final NamingConventions namingConventions,
                                       final SourceSet projectSourceSet) {

        final Ajc ajc = project
                .tasks
                .create(namingConventions.getAspectCompileTaskName(projectSourceSet), Ajc)

        final JavaCompile javaCompile = project
                .tasks
                .withType(JavaCompile)
                .getByName(projectSourceSet.compileJavaTaskName)

        ajc.description = "Compiles AspectJ Source for ${projectSourceSet.name} source set"
        ajc.sourceDirectories = projectSourceSet.java.srcDirs
        ajc.destinationDir = projectSourceSet.java.outputDir
        ajc.classpath = projectSourceSet.compileClasspath
        ajc.aspectPath = project
                .configurations
                .getByName(namingConventions.getAspectPathConfigurationName(projectSourceSet))
        ajc.ajInPath = project
                .configurations
                .getByName(namingConventions.getAspectInpathConfigurationName(projectSourceSet))

        ajc.dependsOn = javaCompile.dependsOn
        ajc.dependsOn(ajc.aspectPath, ajc.ajInPath, javaCompile.classpath)

        javaCompile.getTaskActions().clear()
        javaCompile.dependsOn(ajc)
    }

    private static NamingConventions getConfigurationNamingConventions(final SourceSet sourceSet) {
        sourceSet.name == SourceSet.MAIN_SOURCE_SET_NAME ?
                new MainNamingConventions() :
                new DefaultNamingConventions()
    }
}


