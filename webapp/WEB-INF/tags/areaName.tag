<%@tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@tag import="com.mossle.base.service.DetailPostService"%>
<%@attribute name="userId" type="java.lang.Object" required="true"%>
<%
  Object userId = jspContext.getAttribute("userId");
  if (userId == null) {
    out.print("");
  } else {

    ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
    DetailPostService detailPostService = ctx.getBean(DetailPostService.class);
    try {
      out.print(detailPostService.areaName(String.valueOf(userId)));
      
    } catch(Exception ex) {
      out.print(userId);
      System.out.println("cannot find bpmProcessId : " + userId);
    }
  }
%>
