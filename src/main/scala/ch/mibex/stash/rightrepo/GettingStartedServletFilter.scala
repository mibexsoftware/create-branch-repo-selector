package ch.mibex.stash.rightrepo

import javax.servlet._
import javax.servlet.http.HttpServletResponse

import com.atlassian.webresource.api.assembler.PageBuilderService


class GettingStartedServletFilter(pageBuilderService: PageBuilderService) extends Filter {
  var filterConfig: FilterConfig = _

  override def init(config: FilterConfig): Unit = {
    filterConfig = config
  }

  override def doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain): Unit = {
    val documentationUrl = filterConfig.getInitParameter("documentationUrl")
    response.asInstanceOf[HttpServletResponse].sendRedirect(documentationUrl)
  }

  override def destroy(): Unit = {}

}