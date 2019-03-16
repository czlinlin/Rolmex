<%@tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag
	import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@tag import="com.mossle.common.utils.DeEnCode;"%>
<%@attribute name="parameters" type="java.lang.String" required="true"%>
<%
  String parameters = (String)jspContext.getAttribute("parameters");
  if (parameters == null) {
    out.print("");
  } else {

	  String encryParameter = DeEnCode.encode(parameters);

    try {
      out.print(encryParameter);
      
    } catch(Exception ex) {
      out.print(encryParameter);
      System.out.println("cannot find parameters : " + parameters);
    }
  }
%>
