package aspectj

import org.gradle.api.Project

/**
 * @author Brian Connolly
 */
class Iajc {

    void execute(final Project project, final Set<File> sourceDirectories, final Map<String, ?> iajcArgs) {

        final String classpath = project.configurations.ajtools.asPath

        final AntBuilder ant = project.ant

        ant.taskdef(resource: "org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties", classpath: classpath)
        ant.iajc(iajcArgs) {
            sourceRoots {
                sourceDirectories.each {
                    project.logger.info("   sourceRoot $it")
                    pathelement(location: it.absolutePath)
                }
            }
        }
    }
}
