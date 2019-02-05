package aspectj

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

class Ajc extends DefaultTask {

    SourceSet sourceSet

    FileCollection aspectpath

    FileCollection ajInpath

    // ignore or warning
    String xlint = 'ignore'

    String maxmem

    Map<String, String> additionalAjcArgs = [] as Map<String, String>

    Iajc iajc = new Iajc()

    Ajc() {
        logging.captureStandardOutput(LogLevel.INFO)
    }

    @TaskAction
    void compile() {
        verifyArguments()

        logger.info("=" * 30)
        logger.info("=" * 30)
        logger.info("Running ajc ...")
        logger.info("classpath: ${sourceSet.compileClasspath.asPath}")
        logger.info("srcDirs $sourceSet.java.srcDirs")

        iajc.execute(project, sourceSet, getIajcArguments())
    }

    private Map<String, ?> getIajcArguments() {

        final String destinationDir = sourceSet
                .java
                .outputDir
                .absolutePath

        final JavaPluginConvention javaPluginConvention = project
                .convention
                .getPlugin(JavaPluginConvention)

        final Map<String, ?> iajcArgs = [classpath           : sourceSet.compileClasspath.asPath,
                                         destDir             : destinationDir,
                                         s                   : destinationDir,
                                         source              : javaPluginConvention.sourceCompatibility,
                                         target              : javaPluginConvention.targetCompatibility,
                                         inpath              : ajInpath.asPath,
                                         xlint               : xlint,
                                         fork                : true,
                                         aspectPath          : aspectpath.asPath,
                                         sourceRootCopyFilter: '**/*.java,**/*.aj',
                                         showWeaveInfo       : true]

        if (null != maxmem) {
            iajcArgs['maxmem'] = maxmem
        }

        if (null != additionalAjcArgs) {
            for (pair in additionalAjcArgs) {
                iajcArgs[pair.key] = pair.value
            }
        }
        iajcArgs
    }

    private void verifyArguments() {

        if (sourceSet == null || aspectpath == null || ajInpath == null) {
            throw new IllegalArgumentException()
        }
    }
}
