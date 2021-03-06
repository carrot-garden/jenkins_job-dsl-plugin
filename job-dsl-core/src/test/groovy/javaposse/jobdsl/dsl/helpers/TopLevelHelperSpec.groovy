package javaposse.jobdsl.dsl.helpers

import javaposse.jobdsl.dsl.WithXmlAction
import javaposse.jobdsl.dsl.WithXmlActionSpec
import spock.lang.Specification

public class TopLevelHelperSpec extends Specification {

    List<WithXmlAction> mockActions = Mock()
    TopLevelHelper helper = new TopLevelHelper(mockActions)
    Node root = new XmlParser().parse(new StringReader(WithXmlActionSpec.xml))

    def 'add description'() {
        when:
        def action = helper.description('Description')
        action.execute(root)

        then:
        root.description[0].value() == 'Description'

        when:
        def action2 = helper.description('Description2')
        action2.execute(root)

        then:
        root.description.size() == 1
        root.description[0].value() == 'Description2'

    }

    def 'can run cordell walker'() {
        when:
        helper.chucknorris()

        then:
        1 * mockActions.add(_)
    }

    def 'cordell walker constructs xml'() {
        when:
        def action = helper.chucknorris()
        action.execute(root)

        then:
        root.'hudson.plugins.chucknorris.CordellWalkerRecorder'[0].factGenerator[0] != null
    }

    def 'can run timeout'() {
        when:
        helper.timeout(15)

        then:
        1 * mockActions.add(_)
    }

    def 'timeout constructs xml'() {
        when:
        def action = helper.timeout(15)
        action.execute(root)

        then:
        root.buildWrappers[0].'hudson.plugins.build__timeout.BuildTimeoutWrapper'[0].timeoutMinutes[0].value() == '15'
        root.buildWrappers[0].'hudson.plugins.build__timeout.BuildTimeoutWrapper'[0].failBuild[0].value() == 'true'
    }

    def 'can run label'() {
        when:
        helper.label('RPM')

        then:
        1 * mockActions.add(_)
    }

    def 'disable defaults to true'() {
        when:
        helper.disabled().execute(root)

        then:
        root.disabled.size() == 1
        root.disabled[0].value() == 'true'

        when:
        helper.disabled(false).execute(root)

        then:
        root.disabled.size() == 1
        root.disabled[0].value() == 'false'
    }

    def 'label constructs xml'() {
        when:
        def action = helper.label('FullTools')
        action.execute(root)

        then:
        root.assignedNode[0].value() == 'FullTools'
        root.canRoam[0].value() == 'false'
    }

    def 'without label leaves canRoam as true'() {
        when:
        when:
        def action = helper.label()
        action.execute(root)

        then:
        root.assignedNode[0].value() == ''
        root.canRoam[0].value() == 'true'
    }

    def 'log rotate xml'() {
        when:
        def action = helper.logRotator(14,50)
        action.execute(root)

        then:
        root.logRotator[0].daysToKeep[0].value() == '14'
        root.logRotator[0].numToKeep[0].value() == '50'
    }

    def 'build blocker xml'() {
        when:
        def action = helper.blockOn("MyProject")
        action.execute(root)

        then:
        root.'hudson.plugins.buildblocker.BuildBlockerProperty'[0].useBuildBlocker[0].value() == 'true'
        root.'hudson.plugins.buildblocker.BuildBlockerProperty'[0].blockingJobs[0].value() == 'MyProject'
    }
}
