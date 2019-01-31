package aspectj

import org.gradle.api.Project

class AspectJExtension {

    String version

    AspectJExtension(final Project project) {
        this.version = project.findProperty('aspectjVersion') ?: '1.8.12'
    }
}
