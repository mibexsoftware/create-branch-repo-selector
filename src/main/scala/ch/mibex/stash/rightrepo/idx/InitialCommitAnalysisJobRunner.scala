package ch.mibex.stash.rightrepo.idx

import ch.mibex.stash.rightrepo.Logging
import ch.mibex.stash.rightrepo.Utils.page
import com.atlassian.bitbucket.commit._
import com.atlassian.bitbucket.permission.Permission
import com.atlassian.bitbucket.repository._
import com.atlassian.bitbucket.user.SecurityService
import com.atlassian.bitbucket.util._
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory
import com.atlassian.sal.api.transaction.{TransactionCallback, TransactionTemplate}
import com.atlassian.scheduler.{JobRunner, JobRunnerRequest, JobRunnerResponse}

import scala.collection.JavaConverters._
import scala.collection.immutable.HashSet


class InitialCommitAnalysisJobRunner(repositoryService: RepositoryService,
                                     refService: RefService,
                                     commitService: CommitService,
                                     pluginSettingsFactory: PluginSettingsFactory,
                                     transactionTemplate: TransactionTemplate,
                                     jiraProjectCommitIndexer: JiraProjectCommitIndexer,
                                     securityService: SecurityService) extends JobRunner with Logging {
  private val changesetAnalysis = new LatestCommitAnalysis

  override def runJob(jobRunnerRequest: JobRunnerRequest): JobRunnerResponse = triggerCommitIndexing

  private def triggerCommitIndexing = {
    val jobResult = securityService
      .withPermission(Permission.REPO_WRITE,
        "Determines the JIRA project key for commits with messages containing JIRA issue key(s)")
      .call(new UncheckedOperation[JobRunnerResponse]() {

        override def perform(): JobRunnerResponse = {
          try {
            debug("RIGHTREPO: STARTING with analysis of existing commits...")

            findAllRepos foreach { r =>
              try {
                debug(s"RIGHTREPO: ---> Initial analysis of repository '${r.getName}' STARTED...")

                findBranches(r) foreach { b =>
                  val commits = collectCommits(r, b)
                  commits foreach { c =>
                    jiraProjectCommitIndexer.index(c)
                  }
                  changesetAnalysis.storeLatestCommit(r, b)
                }

                debug(s"RIGHTREPO: ---> Initial analysis of repository '${r.getName}' FINISHED.")
              } catch {
                case e: Exception =>
                  log.error(s"RIGHTREPO: failed to index repository ${r.getName}: ${e.getMessage}", e)
              }
            }

            debug("RIGHTREPO: Analysis of existing commits FINISHED.")
            saveSuccessfulAnalysisState()

            JobRunnerResponse.success("Analysis of existing commits for JIRA project keys finished")
          } catch {
            case e: Exception =>
              log.error(s"RIGHTREPO: An error occurred during the initial analysis: ${e.getMessage}", e)
              JobRunnerResponse.failed(e)
          }
        }
      })

    jobResult
  }

  private def saveSuccessfulAnalysisState() {
    transactionTemplate.execute(new TransactionCallback[Void] {
      override def doInTransaction(): Void = {
        val settings = pluginSettingsFactory.createGlobalSettings()
        settings.put(InitialRunDoneSetting, true.toString) // SAL plugin settings does not support booleans
        null
      }
    })
  }

  private def collectCommits(repo: Repository, ref: Ref) = {
    val request = new CommitsBetweenRequest.Builder(repo)
      .include(ref.getLatestCommit)
      .exclude(changesetAnalysis.getLatestCommit(repo, ref).orNull)
      .build()
    val commitsCollector = new CommitsCollector
    commitService.streamCommitsBetween(request, commitsCollector)
    commitsCollector.getResult
  }

  private class CommitsCollector extends CommitCallback {
    private var commits = HashSet[Commit]()

    override def onCommit(commit: Commit): Boolean = {
      commits += commit
      true
    }

    override def onEnd(summary: CommitSummary): Unit = {}

    override def onStart(context: CommitContext): Unit = {}

    def getResult = commits

  }

  private def findAllRepos() = page(500) {
    repositoryService.findAll
  }

  private def findBranches(repo: Repository) = page(500) {
    val branchesRequest = new RepositoryBranchesRequest.Builder(repo).build()
    refService.getBranches(branchesRequest, _)
  }

}
