package ch.mibex.stash.rightrepo

import org.slf4j.LoggerFactory

trait Logging {
  self =>

  protected val log = LoggerFactory.getLogger(getClass)

  def debug[T](msg: => T): Unit = { // lazy function parameters to only evaluate msg when debug logging is enabled
    if (log.isDebugEnabled) {
      log.debug(msg.toString)
    }
  }

}