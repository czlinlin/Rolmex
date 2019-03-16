<%@tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@tag import="com.mossle.base.service.DetailPostService"%>
<%@attribute name="bpmProcessId" type="java.lang.Object" required="true"%>
<%
  Object bpmProcessId = jspContext.getAttribute("bpmProcessId");
  if (bpmProcessId == null) {
    out.print("");
  } else {

    ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
    DetailPostService detailPostService = ctx.getBean(DetailPostService.class);
    try {
      out.print(detailPostService.getBpmProcessID(Long.valueOf(String.valueOf(bpmProcessId)).longValue()));
      
    } catch(Exception ex) {
      out.print(bpmProcessId);
      System.out.println("cannot find bpmProcessId : " + bpmProcessId);
    }
  }
%>
