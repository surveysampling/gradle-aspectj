package aspectj

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
/**
 *
 * @author Luke Taylor
 * @author Mike Noordermeer
 */
class AspectJPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.plugins.apply(JavaPlugin)

        def aspectj = project.extensions.create('aspectj', AspectJExtension, project)

        if (project.configurations.findByName('ajtools') == null) {
            project.configurations.create('ajtools')
            project.afterEvaluate { p ->
                if (aspectj.version == null) {
                    throw new GradleException("No aspectj version supplied")
                }

                p.dependencies {
                    ajtools "org.aspectj:aspectjtools:${aspectj.version}"
                    compile "org.aspectj:aspectjrt:${aspectj.version}"
                }
            }
        }

        for (projectSourceSet in project.sourceSets) {
            def namingConventions = projectSourceSet.name.equals('main') ? new MainNamingConventions() : new DefaultNamingConventions();
            for (configuration in [namingConventions.getAspectPathConfigurationName(projectSourceSet), namingConventions.getAspectInpathConfigurationName(projectSourceSet)]) {
                if (project.configurations.findByName(configuration) == null) {
                    project.configurations.create(configuration)
                }
            }

            if (!projectSourceSet.allJava.isEmpty()) {
                def aspectTaskName = namingConventions.getAspectCompileTaskName(projectSourceSet)
                def javaTaskName = namingConventions.getJavaCompileTaskName(projectSourceSet)

                project.tasks.create(name: aspectTaskName, overwrite: true, description: "Compiles AspectJ Source for ${projectSourceSet.name} source set", type: Ajc) {
                    sourceSet = projectSourceSet
                    inputs.files(sourceSet.allJava)
                    outputs.dir(sourceSet.java.outputDir)
                    aspectpath = project.configurations.findByName(namingConventions.getAspectPathConfigurationName(projectSourceSet))
                    ajInpath = project.configurations.findByName(namingConventions.getAspectInpathConfigurationName(projectSourceSet))
                }

                project.tasks[aspectTaskName].setDependsOn(project.tasks[javaTaskName].dependsOn)
                project.tasks[aspectTaskName].dependsOn(project.tasks[aspectTaskName].aspectpath)
                project.tasks[aspectTaskName].dependsOn(project.tasks[aspectTaskName].ajInpath)
                project.tasks[javaTaskName].deleteAllActions()
                project.tasks[javaTaskName].dependsOn(project.tasks[aspectTaskName])
            }
        }
    }
}


