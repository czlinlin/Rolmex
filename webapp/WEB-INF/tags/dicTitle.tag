<%@tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@tag import="com.mossle.api.dict.DictConnector"%>
<%@attribute name="typeName" type="java.lang.String" required="true"%>
<%@attribute name="dicValue" type="java.lang.String" required="true"%>
<%
    ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
    DictConnector dictConnector = ctx.getBean(DictConnector.class);
    try {
      out.print(dictConnector.findDictNameByValue(typeName, dicValue));
    } catch(Exception ex) {
      System.out.println("");
    }
%>
