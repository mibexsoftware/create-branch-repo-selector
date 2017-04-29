package ch.mibex.stash.rightrepo.idx

import java.util.Date

import ch.mibex.stash.rightrepo._
import com.atlassian.sal.api.lifecycle.LifecycleAware
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory
import com.atlassian.sal.api.transaction.{TransactionCallback, TransactionTemplate}
import com.atlassian.scheduler.SchedulerService
import com.atlassian.scheduler.config.{JobConfig, RunMode, Schedule}


// see https://bitbucket.org/cfuller/atlassian-scheduler-jira-example
class PluginLifecycleHandler(schedulerService: SchedulerService,
                             transactionTemplate: TransactionTemplate,
                             pluginSettingsFactory: PluginSettingsFactory,
                             initialCommitAnalysisJobRunner: InitialCommitAnalysisJobRunner)
  extends LifecycleAware with Logging {

  // we cannot use PluginStartedEvent here because in that case our plug-in might start before the Git plugin which
  // we use in the initial analysis of the repositories; this problem occurs with Bitbucket EAP >= 4.0.0-EAP2
  override def onStart(): Unit = {
    launch()
  }

  override def onStop(): Unit = {
    unregisterJobRunner()
  }

  private def launch() {
    log.info("RIGHTREPO: Got the plug-in framework started event... Time to get started!")
    try {
      if (isAlreadyIndexed) {
        log.info("RIGHTREPO: Already indexed, will not run the analysis again.")
      } else {
        registerJobRunner()
        scheduleIndexingJob()
      }
    } catch {
      case e: Exception =>
        log.error("RIGHTREPO: Unexpected error during launch", e)
    }
  }

  private def scheduleIndexingJob() {
    val jobConfig = JobConfig
      .forJobRunnerKey(InitialCommitAnalysisJobRunnerKey)
      .withSchedule(Schedule.runOnce(new Date))
      .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER)
    debug("RIGHTREPO: Schedule commits indexing job")
    schedulerService.scheduleJobWithGeneratedId(jobConfig)
  }

  private def isAlreadyIndexed =
    transactionTemplate.execute(new TransactionCallback[Boolean] {
      override def doInTransaction(): Boolean = {
        val settings = pluginSettingsFactory.createGlobalSettings()
        Option(settings.get(InitialRunDoneSetting)) match {
          case Some(setting: String) => java.lang.Boolean.parseBoolean(setting)
          case _ => false
        }
      }
    })

  private def registerJobRunner() {
    debug("RIGHTREPO: register job runners")
    schedulerService.registerJobRunner(InitialCommitAnalysisJobRunnerKey, initialCommitAnalysisJobRunner)
  }

  private def unregisterJobRunner() {
    debug("RIGHTREPO: unregister job runners")
    schedulerService.unregisterJobRunner(InitialCommitAnalysisJobRunnerKey)
  }

}
