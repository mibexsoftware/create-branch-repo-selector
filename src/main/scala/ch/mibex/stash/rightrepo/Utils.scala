package ch.mibex.stash.rightrepo

import java.util.Locale

import com.atlassian.bitbucket.util._
import scala.collection.JavaConverters._

object Utils {

  def jiraKeyToJiraProject(jiraKey: String): Option[String] = Option(jiraKey) match {
    case Some(_) if jiraKey.contains("-") =>
      Option(jiraKey.substring(0, jiraKey.indexOf("-")).toUpperCase(Locale.US))
    case _ => None
  }

  def page[A](limit: Int, start: Int = 0)(f: (PageRequest) => Page[A]): Iterable[A] =
    new PagedIterable[A](new PageProvider[A] {
      override def get(pageRequest: PageRequest) = f(pageRequest)
    }, PageUtils.newRequest(start, limit)).asScala

}
