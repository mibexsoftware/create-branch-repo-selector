package ch.mibex.stash.rightrepo

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

@RunWith(classOf[JUnitRunner])
class UtilsTest extends FlatSpec with MockitoSugar {

  "valid JIRA key" should "yield the JIRA project key" in {
    assert(Utils.jiraKeyToJiraProject("CRA-1").contains("CRA"))
  }

  "invalid JIRA key" should "yield no JIRA project key" in {
    assert(Utils.jiraKeyToJiraProject("aBes1").isEmpty)
  }

  "null" should "yield no JIRA project key" in {
    assert(Utils.jiraKeyToJiraProject(null).isEmpty)
  }

  "empty string" should "yield no JIRA project key" in {
    assert(Utils.jiraKeyToJiraProject("").isEmpty)
  }

}
