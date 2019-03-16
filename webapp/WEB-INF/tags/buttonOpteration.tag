<%@tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@tag import="com.mossle.api.menu.MenuConnector"%>
<%@tag import="com.mossle.api.menu.MenuDTO"%>
<%@tag import="com.mossle.auth.persistence.domain.Menu"%>
<%@tag import="java.util.List" %>
<%@tag import="com.mossle.core.auth.CurrentUserHolder" %>
<%@attribute name="opterNames" type="java.lang.Object" required="true"%>
<%@attribute name="opterTypes" type="java.lang.Object" required="true"%>
<%@attribute name="buttonTypes" type="java.lang.Object" required="true"%>
<%@attribute name="opterParams" type="java.lang.Object" required="true"%>
<%
	//根据当前uri获取菜单数据
	String request_uri = request.getAttribute("javax.servlet.forward.request_uri").toString();
	String contextPath = request.getContextPath();
	request_uri = request_uri.replaceAll(contextPath, "");
	
	
	// 请求参数
			String queryurl=request.getQueryString();  
		    /* if(null!=queryurl){  
		    	request_uri+="?"+queryurl;  
		    }  */
		    // out.println("===" + request_uri);
			ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
			MenuConnector menuConnector = ctx.getBean(MenuConnector.class);
			/* Menu menu = menuConnector.findMenuByUrl(request_uri, "opteration");
			//out.println("========================" + request_uri);
			//out.println("========================" + menu.getType());
	
			//根据当前菜单数据获取父级菜单数据
			Menu pageMenu = null;
			if (menu.getType().equals("page"))
				pageMenu = menu; */
			
			//CurrentUserHolder currentUserHolderTag = ctx.getBean(CurrentUserHolder.class);
			
			///String userId=currentUserHolderTag.getUserId();
			
			//获取所有操作菜单
			List<Menu> opterationMenus = menuConnector.findOpterationMenus(request_uri);
			if(opterationMenus!=null&&opterationMenus.size()>0){
				StringBuffer realOpterNames=new StringBuffer("|");
				for(Menu m:opterationMenus){
					realOpterNames.append(m.getTitle()+"|");
					//out.println(m.getTitle());
				}
				
				String strSplitChar="\\|";
				String[] opterNameArray=opterNames.toString().split(strSplitChar);
				String[] opterTypeArray=opterTypes.toString().split(strSplitChar);
				String[] buttonTypeArray=buttonTypes.toString().split(strSplitChar);
				String[] opterParamArray=opterParams.toString().split(strSplitChar);
				String strTempleHref="<a href=\"%s\" class=\"a-update\">%s</a>";
				String strTempleClick="<a href=\"javascript:\" onclick=\"%s\" class=\"a-update\">%s</a>";
				/* <button class="btn btn-default a-insert"
                        onclick="location.href='person-info-input.do?partyEntityId=${partyEntityId}'">新建
                </button> */
                String strTempleButtonHref="<button class=\"btn btn-default a-insert\" onclick=\"location.href='%s'\">%s</button>";
                String strTempleButtonClick="<button class=\"btn btn-default a-insert\" onclick=\"%s\">%s</button>";
                String selectTemple=strTempleHref;
				//for(int i=0;i<opterNameArray.length;i++){
				int i=0;
				for(String opterName:opterNameArray){
					//if(i==0)
					//out.println(opterName+"X");
					if(realOpterNames.toString().contains("|"+opterName+"|")){
						if(buttonTypeArray[i].equals("a"))
						{
							if(opterTypeArray[i].equals("href"))
								selectTemple=strTempleHref;
							else
								selectTemple=strTempleClick;
						}
						else
						{
							if(opterTypeArray[i].equals("href"))
								selectTemple=strTempleButtonHref;
							else
								selectTemple=strTempleButtonClick;
						}
						out.println(String.format(selectTemple,opterParamArray[i],opterName));
					}
					i++;
				}
			}
%>


