package aspectj

import org.gradle.api.tasks.SourceSet

interface NamingConventions {

    String getAspectCompileTaskName(SourceSet sourceSet);

    String getAspectPathConfigurationName(SourceSet sourceSet);

    String getAspectInpathConfigurationName(SourceSet sourceSet);
}
