package aspectj


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

        final DependencyHandler dependencyHandler = project.dependencies
        dependencyHandler.add(CONFIGURATION_AJTOOLS, "org.aspectj:aspectjtools:${extension.version}")
        dependencyHandler.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, "org.aspectj:aspectjrt:${extension.version}")
    }

    private static void configureSourceSet(final Project project, final SourceSet sourceSet) {

        project.configurations.maybeCreate(NamingUtils.getAspectPathConfigurationName(sourceSet))
        project.configurations.maybeCreate(NamingUtils.getAspectInPathConfigurationName(sourceSet))

        if (!sourceSet.java.isEmpty()) {
            configureTasks(project, sourceSet)
        }
    }

    private static void configureTasks(final Project project,
                                       final SourceSet projectSourceSet) {

        final Ajc ajc = project
                .tasks
                .create(NamingUtils.getAspectCompileTaskName(projectSourceSet), Ajc)

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
                .getByName(NamingUtils.getAspectPathConfigurationName(projectSourceSet))
        ajc.ajInPath = project
                .configurations
                .getByName(NamingUtils.getAspectInPathConfigurationName(projectSourceSet))

        ajc.dependsOn = javaCompile.dependsOn
        ajc.dependsOn(ajc.aspectPath, ajc.ajInPath, javaCompile.classpath)

        javaCompile.getTaskActions().clear()
        javaCompile.dependsOn(ajc)
    }
}


