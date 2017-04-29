package ch.mibex.stash.rightrepo

import com.atlassian.scheduler.config.JobRunnerKey

package object idx {

  val PluginKey = "ch.mibex.stash.rightrepo"
  val RightRepoIndexerId = PluginKey + ".jira-project-cs-indexer"
  val InitialRunDoneSetting = PluginKey + ".initial-run-done"
  val JiraProjectIndexKeyField = "rightrepo4stash-jira-project"
  val InitialCommitAnalysisJobRunnerKey = JobRunnerKey.of(s"$PluginKey.INITIAL-COMMIT-ANALYSIS-RUNNER")

}
