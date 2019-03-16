<%@tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@tag import="com.mossle.api.party.PartyConnector"%>
<%@attribute name="departmentId" type="java.lang.Object" required="true"%>
<%
  Object departmentId = jspContext.getAttribute("departmentId");
  if (departmentId == null) {
    out.print("");
  } else {

    ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
    PartyConnector partyConnector = ctx.getBean(PartyConnector.class);
    try {
      out.print(partyConnector.findCompanyById(departmentId.toString()).getName());
    } catch(Exception ex) {
      out.print(departmentId);
      System.out.println("cannot find company : " + departmentId);
    }
  }
%>
