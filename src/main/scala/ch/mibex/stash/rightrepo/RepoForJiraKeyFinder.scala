package ch.mibex.stash.rightrepo

import ch.mibex.stash.rightrepo.idx.JiraProjectIndexKeyField
import com.atlassian.bitbucket.idx.{CommitIndex, IndexedCommit}
import com.atlassian.bitbucket.repository.{Repository, RepositoryService}
import com.atlassian.bitbucket.util.PageUtils
import com.atlassian.integration.jira.JiraKeyScanner

import scala.collection.JavaConverters._

class RepoForJiraKeyFinder(jiraKeyScanner: JiraKeyScanner,
                           repositoryService: RepositoryService,
                           commitIndex: CommitIndex) extends Logging {

  def findRepoMostTaggedWith(jiraIssueKey: String): Option[Repository] = {
    jiraKeyScanner.findAll(jiraIssueKey)
      .asScala
       .headOption
      .flatMap(jiraKey => Utils.jiraKeyToJiraProject(jiraKey))
      .flatMap(jiraProjectKey => {

      determineMostFrequentRepo(findFirst50CommitsTaggedWith(jiraProjectKey))

    }).flatMap(repoId => Option(repositoryService.getById(repoId)))
  }

  private def findFirst50CommitsTaggedWith(jiraProjectKey: String) = {
    // we only consider the first 50 commits, as this is probably quite a fair sample size and we are limited by the
    // Stash env variable "page.max.index.results" which is set to 50 in Stash by default and ChangeSetIndex
    // overrides the passed requested page size to this value which would make it incredibly slow to process large
    // repositories with 10000s of commits when doing that many requests to the database
    val caseSensitive = true
    val first50Results = PageUtils.newRequest(0, 50)
    val firstPage = commitIndex.findByProperty(JiraProjectIndexKeyField, jiraProjectKey, caseSensitive, first50Results)
    firstPage.getValues.asScala
  }

  private def determineMostFrequentRepo(commits: Iterable[IndexedCommit]) = {
    val repoIdsTaggedWithJiraProject = commits.flatMap {
      _.getRepositories.asScala.filterNot(_.isFork).map(_.getId)
    }
    repoIdsTaggedWithJiraProject match {
      case Nil => None
      case _ => Some(
        repoIdsTaggedWithJiraProject
          .groupBy(identity)
          .maxBy(_._2.size)
          ._1
      )
    }
  }

}
