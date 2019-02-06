package aspectj

import org.gradle.api.tasks.SourceSet

/**
 * @author Brian Connolly
 */
class NamingUtils {

    private static final String EXCEPTON_MESSAGE = "sourceSet cannot be null!"

    private static final String ASPECTPATH = 'aspectpath'
    private static final String AJ_INPATH = 'ajInpath'

    static String getAspectCompileTaskName(final SourceSet sourceSet) {

        if (sourceSet == null) {
            throw new IllegalArgumentException(EXCEPTON_MESSAGE)
        }

        "compile${isMainSourceSet(sourceSet) ? '' : sourceSet.name.capitalize()}Aspect"
    }

    static String getAspectPathConfigurationName(final SourceSet sourceSet) {

        if (sourceSet == null) {
            throw new IllegalArgumentException(EXCEPTON_MESSAGE)
        }

        isMainSourceSet(sourceSet) ? ASPECTPATH : "${sourceSet.name}${ASPECTPATH.capitalize()}"
    }

    static String getAspectInPathConfigurationName(final SourceSet sourceSet) {

        if (sourceSet == null) {
            throw new IllegalArgumentException(EXCEPTON_MESSAGE)
        }

        isMainSourceSet(sourceSet) ? AJ_INPATH : "${sourceSet.name}${AJ_INPATH.capitalize()}"
    }

    private static boolean isMainSourceSet(final SourceSet sourceSet) {
        sourceSet.name == SourceSet.MAIN_SOURCE_SET_NAME
    }
}
