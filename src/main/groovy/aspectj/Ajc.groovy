package aspectj

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.*

class Ajc extends DefaultTask {

    @InputFiles
    FileCollection classpath

    @InputFiles
    FileCollection aspectPath

    @InputFiles
    FileCollection ajInPath

    @InputFiles
    Set<File> sourceDirectories

    @OutputDirectory
    File destinationDir

    // ignore or warning
    @Input
    String xlint = 'ignore'

    @Optional
    @Input
    String maxmem

    @Optional
    @Input
    Map<String, String> additionalAjcArgs = [] as Map<String, String>

    @Input
    boolean parameters = false

    Iajc iajc = new Iajc()

    Ajc() {
        logging.captureStandardOutput(LogLevel.INFO)
    }

    @TaskAction
    void compile() {
        verifyArguments()

        iajc.execute(project, sourceDirectories, getIajcArguments())
    }

    private Map<String, ?> getIajcArguments() {

        final JavaPluginConvention javaPluginConvention = project
                .convention
                .getPlugin(JavaPluginConvention)

        final Map<String, ?> iajcArgs = [classpath           : classpath.asPath,
                                         destDir             : destinationDir.absolutePath,
                                         s                   : destinationDir.absolutePath,
                                         source              : javaPluginConvention.sourceCompatibility,
                                         target              : javaPluginConvention.targetCompatibility,
                                         inpath              : ajInPath.asPath,
                                         xlint               : xlint,
                                         fork                : true,
                                         aspectPath          : aspectPath.asPath,
                                         sourceRootCopyFilter: '**/*.java,**/*.aj',
                                         showWeaveInfo       : true]

        if (parameters) {
            iajcArgs['parameters'] = parameters
        }

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

        if (classpath == null || sourceDirectories == null || destinationDir == null || aspectPath == null || ajInPath == null) {
            throw new IllegalArgumentException()
        }
    }
}
