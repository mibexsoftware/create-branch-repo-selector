package ch.mibex.stash.rightrepo.idx

import com.atlassian.bitbucket.commit.Commit
import com.atlassian.bitbucket.idx.CommitIndex
import com.atlassian.bitbucket.repository.Repository
import com.atlassian.integration.jira.JiraKeyScanner
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.collection.JavaConverters._


class JiraProjectCommitIndexerTest extends Specification with Mockito {

  "commit with a JIRA issue in its message" should {
    val commitIndex = mock[CommitIndex]
    val jiraKeyScanner = mock[JiraKeyScanner]
    val jiraProjectCommitIndexer = new JiraProjectCommitIndexer(commitIndex, jiraKeyScanner)
    val commit1 = mock[Commit]
    val noFork = mock[Repository]
    noFork.isFork returns false
    commit1.getRepository returns noFork
    commit1.getId returns "310169e9d1d8fadb489f2158fe32597c6460e333"
    commit1.getMessage returns "This commit fixes GUGUS-1 and does this and that"
    jiraKeyScanner.findAll(commit1.getMessage) returns List("GUGUS-1").asJava

    "get an attribute for the one JIRA project when being index" in {
      jiraProjectCommitIndexer.index(commit1)
      there was one(commitIndex).addProperty(commit1.getId, JiraProjectIndexKeyField,  "GUGUS")
    }

    "get the attribute removed if commit got removed" in {
      jiraProjectCommitIndexer.remove(commit1)
      there was one(commitIndex).removeProperty(commit1.getId, JiraProjectIndexKeyField,  "GUGUS")
    }
  }

  "commit with JIRA issue but from fork repository" should {
    val commitIndex = mock[CommitIndex]
    val jiraKeyScanner = mock[JiraKeyScanner]
    val jiraProjectCommitIndexer = new JiraProjectCommitIndexer(commitIndex, jiraKeyScanner)
    val commit1 = mock[Commit]
    val fork = mock[Repository]
    fork.isFork returns true
    commit1.getRepository returns fork
    commit1.getId returns "310169e9d1d8fadb489f2158fe32597c6460e333"
    commit1.getMessage returns "This commit fixes GUGUS-1 and does this and that"
    jiraKeyScanner.findAll(commit1.getMessage) returns List("GUGUS-1").asJava

    "should not get indexed" in {
      jiraProjectCommitIndexer.index(commit1)
      there was no(commitIndex).addProperty(commit1.getId, JiraProjectIndexKeyField,  "GUGUS")
    }

    "get the attribute removed if commit got removed" in {
      jiraProjectCommitIndexer.remove(commit1)
      there was one(commitIndex).removeProperty(commit1.getId, JiraProjectIndexKeyField,  "GUGUS")
    }

  }

  "commits with multiple JIRA issues of the same project in their message" should {
    val commitIndex = mock[CommitIndex]
    val jiraKeyScanner = mock[JiraKeyScanner]
    val jiraProjectCommitIndexer = new JiraProjectCommitIndexer(commitIndex, jiraKeyScanner)
    val commit1 = mock[Commit]
    commit1.getId returns "310169e9d1d8fadb489f2158fe32597c6460e333"
    commit1.getMessage returns "This commit fixes GUGUS-1, GUGUS-2 and GUGUS-3"
    jiraKeyScanner.findAll(commit1.getMessage) returns List("GUGUS-1", "GUGUS-2", "GUGUS-3").asJava

    "get an attribute for the one JIRA project when being index" in {
      jiraProjectCommitIndexer.index(commit1)
      there was one(commitIndex).addProperty(commit1.getId, JiraProjectIndexKeyField,  "GUGUS")
    }

    "get the attribute removed if commit got removed" in {
      jiraProjectCommitIndexer.remove(commit1)
      there was one(commitIndex).removeProperty(commit1.getId, JiraProjectIndexKeyField,  "GUGUS")
    }
  }

  "commits with multiple JIRA issues of different projects in their message" should {
    val commitIndex = mock[CommitIndex]
    val jiraKeyScanner = mock[JiraKeyScanner]
    val jiraProjectCommitIndexer = new JiraProjectCommitIndexer(commitIndex, jiraKeyScanner)
    val commit1 = mock[Commit]
    commit1.getId returns "310169e9d1d8fadb489f2158fe32597c6460e333"
    commit1.getMessage returns "This commit fixes GUGUS-1, TEST-2 and SUPERDUPER-3"
    jiraKeyScanner.findAll(commit1.getMessage) returns List("GUGUS-1", "TEST-2", "SUPERDUPER-3").asJava

    "get attributes for all JIRA projects if index" in {
      jiraProjectCommitIndexer.index(commit1)
      there was one(commitIndex).addProperty(commit1.getId, JiraProjectIndexKeyField,  "GUGUS")
      there was one(commitIndex).addProperty(commit1.getId, JiraProjectIndexKeyField,  "TEST")
      there was one(commitIndex).addProperty(commit1.getId, JiraProjectIndexKeyField,  "SUPERDUPER")
    }

    "get the attributes removed if commit got removed" in {
      jiraProjectCommitIndexer.remove(commit1)
      there was one(commitIndex).removeProperty(commit1.getId, JiraProjectIndexKeyField,  "GUGUS")
      there was one(commitIndex).removeProperty(commit1.getId, JiraProjectIndexKeyField,  "TEST")
      there was one(commitIndex).removeProperty(commit1.getId, JiraProjectIndexKeyField,  "SUPERDUPER")
    }
  }

}