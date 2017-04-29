package ch.mibex.stash.rightrepo

import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletRequestWrapper}

import com.atlassian.bitbucket.idx.CommitIndex
import com.atlassian.integration.jira.JiraKeyScanner


class RepoSelectorServletFilter(commitIndex: CommitIndex,
                                repoForJiraKeyFinder: RepoForJiraKeyFinder,
                                jiraKeyScanner: JiraKeyScanner) extends Filter with Logging {

  private class RepoServletRequestWrapper(request: HttpServletRequest) extends HttpServletRequestWrapper(request) {
    private val RepoIdQueryParamName = "repoId"
    private val IssueKeyQueryParamName = "issueKey"

    // in theory, we would also have to intercept ServletRequest#getParameterValues, but Stash is only using
    // ServletRequest#getParameter, so we only use that one
    override def getParameter(paramName: String): String = {
      def containsRequestParam(key: String) = request.getParameterMap.containsKey(key)

      debug(s"RIGHTREPO: servlet intercept for parameter $paramName")
      // Example of a request URL for branch creation from JIRA:
      // stash/plugins/servlet/create-branch?issueKey=REPOSUM-19&issueType=Bug&issueSummary=test
      if (containsRequestParam(IssueKeyQueryParamName)) {
        paramName match {
          case RepoIdQueryParamName if !containsRequestParam(RepoIdQueryParamName) =>
            val repoId = for {
              jiraIssue <- Option(request.getParameter(IssueKeyQueryParamName))
              repo <- repoForJiraKeyFinder.findRepoMostTaggedWith(jiraIssue)
            } yield repo.getId.toString
            repoId.getOrElse(super.getParameter(paramName))
          case _ => super.getParameter(paramName)
        }
      } else {
        // use the parameter values from the query string of the request...
        super.getParameter(paramName)
      }
    }
  }

  override def init(fc: FilterConfig): Unit = {}

  override def doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain): Unit = {
    filterChain.doFilter(new RepoServletRequestWrapper(request.asInstanceOf[HttpServletRequest]), response)
  }

  override def destroy(): Unit = {}

}