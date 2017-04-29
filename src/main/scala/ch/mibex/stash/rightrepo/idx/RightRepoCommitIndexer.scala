package ch.mibex.stash.rightrepo.idx

import com.atlassian.bitbucket.commit.Commit
import com.atlassian.bitbucket.idx.{CommitIndexer, IndexingContext}
import com.atlassian.bitbucket.repository.Repository


class RightRepoCommitIndexer(jiraProjectCommitIndexer: JiraProjectCommitIndexer) extends CommitIndexer {

  override def getId: String = RightRepoIndexerId

  override def onCommitRemoved(commit: Commit, indexingContext: IndexingContext): Unit = {
    jiraProjectCommitIndexer.remove(commit)
  }

  override def onAfterIndexing(indexingContext: IndexingContext): Unit = {}

  override def onBeforeIndexing(indexingContext: IndexingContext): Unit = {}

  override def onCommitAdded(commit: Commit, context: IndexingContext): Unit = {
    jiraProjectCommitIndexer.index(commit)
  }

  override def isEnabledForRepository(repository: Repository): Boolean = true

}
