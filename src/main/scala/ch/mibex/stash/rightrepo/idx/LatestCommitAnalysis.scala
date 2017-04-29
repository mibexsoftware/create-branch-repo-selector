package ch.mibex.stash.rightrepo.idx

import com.atlassian.bitbucket.repository.{Ref, Repository}


case class RefKey(repo: Repository, ref: Ref)

class LatestCommitAnalysis {
  private var analysis = Map.empty[RefKey, String]

  def getLatestCommit(repo: Repository, ref: Ref): Option[String] =
    analysis.get(RefKey(repo, ref))

  def storeLatestCommit(repo: Repository, ref: Ref) {
    analysis += RefKey(repo, ref) -> ref.getLatestCommit
  }

}
