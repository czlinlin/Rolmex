<%@tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@tag import="com.mossle.api.user.UserConnector"%>
<%@attribute name="userIds" type="java.lang.Object" required="true"%>
<%@attribute name="showType" type="java.lang.Object" required="true"%>
<%
  Object userIds = jspContext.getAttribute("userIds");
  if (userIds == null) {
    out.print("");
  } else {

    ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
    UserConnector userConnector = ctx.getBean(UserConnector.class);
    
    try {
      String strNames=userConnector.findNamesByIds(userIds.toString());
      if(showType.equals("1"))
      	out.print(strNames);
      else{
    	  if(!strNames.equals("")){
    		  String[] nameArray=strNames.split(",");
    		  String strReturn="<div style='line-height:24px;'>";
    		  for(int i=0;i<nameArray.length;i++){
    			  strReturn+="<span style=''>"+(i+1)+"."+nameArray[i]+"&emsp;</span>";
    		  }
    		  out.print(strReturn+"</div>");
    	  }
    	  else
    		  out.print(strNames);
      }
    } catch(Exception ex) {
      out.print(userIds);
      System.out.println("cannot find user : " + userIds);
    }
  }
%>
