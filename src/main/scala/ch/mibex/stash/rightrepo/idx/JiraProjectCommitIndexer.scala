package ch.mibex.stash.rightrepo.idx

import ch.mibex.stash.rightrepo.{Logging, Utils}
import com.atlassian.bitbucket.commit.Commit
import com.atlassian.bitbucket.idx.CommitIndex
import com.atlassian.integration.jira.JiraKeyScanner

import scala.collection.JavaConverters._

class JiraProjectCommitIndexer(commitIndex: CommitIndex,
                               jiraKeyScanner: JiraKeyScanner) extends Logging {

  def index(commit: Commit): Unit = Option(commit.getRepository) match {
    case Some(repo) if repo.isFork => // do not index forks
    case _ =>
      try {
        findJiraProjectKeysIn(commit).foreach { projectKey =>
          debug(s"RIGHTREPO: found JIRA project mapping: ${commit.getId} => $projectKey")
          commitIndex.addProperty(commit.getId, JiraProjectIndexKeyField, projectKey)
        }
      } catch {
        case e: Exception =>
          log.error(s"RIGHTREPO: Failed to index commit ${commit.getId}: ${e.getMessage}", e)
      }
  }

  def remove(commit: Commit): Unit = {
    findJiraProjectKeysIn(commit).foreach {
      commitIndex.removeProperty(commit.getId, JiraProjectIndexKeyField, _)
    }
  }

  private def findJiraProjectKeysIn(commit: Commit) = Option(commit.getMessage) match {
    case Some(msg) =>
      val issueKeys = jiraKeyScanner.findAll(msg).asScala
      issueKeys.flatMap(s => Utils.jiraKeyToJiraProject(s)).toSet
    case None => Set()
  }

}
