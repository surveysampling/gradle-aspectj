package aspectj

import org.gradle.api.tasks.SourceSet

/**
 * @author Brian Connolly
 */
class MainNamingConventions implements NamingConventions {

    @Override
    String getJavaCompileTaskName(final SourceSet sourceSet) {
        return "compileJava"
    }

    @Override
    String getAspectCompileTaskName(final SourceSet sourceSet) {
        return "compileAspect"
    }

    @Override
    String getAspectPathConfigurationName(final SourceSet sourceSet) {
        return "aspectpath"
    }

    @Override
    String getAspectInpathConfigurationName(final SourceSet sourceSet) {
        return "ajInpath"
    }
}
