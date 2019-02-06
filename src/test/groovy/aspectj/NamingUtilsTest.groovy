package aspectj

import org.gradle.api.tasks.SourceSet
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Brian Connolly
 */
class NamingUtilsTest extends Specification {

    @Unroll
    def """GetAspectPathConfigurationName
        sourceSetName=#sourceSetName
        configurationName=#configurationName"""() {

        given:
        final SourceSet sourceSet = Mock(SourceSet)
        sourceSet.name >> sourceSetName

        expect:
        configurationName == NamingUtils.getAspectPathConfigurationName(sourceSet)

        where:
        sourceSetName                  || configurationName
        SourceSet.MAIN_SOURCE_SET_NAME || 'aspectpath'
        SourceSet.TEST_SOURCE_SET_NAME || 'testAspectpath'
        'integTest'                    || 'integTestAspectpath'
        'acceptanceIntegTest'          || 'acceptanceIntegTestAspectpath'
    }

    def "GetAspectPathConfigurationName Null SourceSet"() {

        when:
        NamingUtils.getAspectPathConfigurationName(null)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def """GetAspectInPathConfigurationName
        sourceSetName=#sourceSetName
        configurationName=#configurationName"""() {

        given:
        final SourceSet sourceSet = Mock(SourceSet)
        sourceSet.name >> sourceSetName

        expect:
        configurationName == NamingUtils.getAspectInPathConfigurationName(sourceSet)

        where:
        sourceSetName                  || configurationName
        SourceSet.MAIN_SOURCE_SET_NAME || 'ajInpath'
        SourceSet.TEST_SOURCE_SET_NAME || 'testAjInpath'
        'integTest'                    || 'integTestAjInpath'
        'acceptanceIntegTest'          || 'acceptanceIntegTestAjInpath'
    }

    def "GetAspectInPathConfigurationName Null SourceSet"() {

        when:
        NamingUtils.getAspectInPathConfigurationName(null)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def """GetAspectCompileTaskName
        sourceSetName=#sourceSetName
        configurationName=#configurationName"""() {

        given:
        final SourceSet sourceSet = Mock(SourceSet)
        sourceSet.name >> sourceSetName

        expect:
        configurationName == NamingUtils.getAspectCompileTaskName(sourceSet)

        where:
        sourceSetName                  || configurationName
        SourceSet.MAIN_SOURCE_SET_NAME || 'compileAspect'
        SourceSet.TEST_SOURCE_SET_NAME || 'compileTestAspect'
        'integTest'                    || 'compileIntegTestAspect'
        'acceptanceIntegTest'          || 'compileAcceptanceIntegTestAspect'
    }

    def "GetAspectCompileTaskName Null SourceSet"() {

        when:
        NamingUtils.getAspectCompileTaskName(null)

        then:
        thrown(IllegalArgumentException)
    }
}
