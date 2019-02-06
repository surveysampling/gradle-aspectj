package aspectj

import org.gradle.api.tasks.SourceSet

/**
 * @author Brian Connolly
 */
class DefaultNamingConventions implements NamingConventions {

    @Override
    String getAspectCompileTaskName(final SourceSet sourceSet) {
        return "compile${sourceSet.name.capitalize()}Aspect"
    }

    @Override
    String getAspectPathConfigurationName(final SourceSet sourceSet) {
        return "${sourceSet.name}Aspectpath"
    }

    @Override
    String getAspectInpathConfigurationName(final SourceSet sourceSet) {
        return "${sourceSet.name}AjInpath"
    }
}
