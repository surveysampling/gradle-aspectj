package aspectj

import org.gradle.api.Project
import org.slf4j.Logger

/**
 * @author Brian Connolly
 */
class Iajc {

    void execute(final Project project, final Set<File> sourceDirectories, final Map<String, ?> iajcArgs) {

        final Logger logger = project.logger
        logger.info("=" * 30)
        logger.info("=" * 30)
        logger.info("Running iajc with arguments=${iajcArgs}")
        logger.info("srcDirs=${sourceDirectories}")

        final String classpath = project.configurations.ajtools.asPath

        final AntBuilder ant = project.ant

        ant.taskdef(resource: "org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties", classpath: classpath)
        ant.iajc(iajcArgs) {
            sourceRoots {
                sourceDirectories.each {
                    logger.info("   sourceRoot $it")
                    pathelement(location: it.absolutePath)
                }
            }
        }
    }
}
