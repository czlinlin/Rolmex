<%@tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@tag import="com.mossle.base.service.DetailPostService"%>
<%@attribute name="detailID" type="java.lang.Object" required="true"%>
<%
  Object detailID = jspContext.getAttribute("detailID");
  if (detailID == null) {
    out.print("");
  } else {

    ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
    DetailPostService detailPostService = ctx.getBean(DetailPostService.class);
    try {
      out.print(detailPostService.getEntity(Long.valueOf(String.valueOf(detailID)).longValue()));
      
    } catch(Exception ex) {
      out.print(detailID);
      System.out.println("cannot find detailID : " + detailID);
    }
  }
%>
