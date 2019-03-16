package com.mossle.core.export;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.util.DateUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wanghan on 2017\11\27 0027.
 */

/**
 * 利用开源组件POI3.0.2动态导出EXCEL文档
 * <p>
 * 转载时请保留以下信息，注明出处！
 *
 * @param  *            <p>
 *            注意这里为了简单起见，boolean型的属性xxx的get器方式为getXxx(),而不是isXxx()
 *            <p>
 *            byte[]表jpg格式的图片数据
 * @author leno
 * @version v1.0
 */

public class ExcelExport{
    public static void exportExcel(Collection dataset, OutputStream out) {
        exportExcel("导出EXCEL文档", null, null, dataset, out, "yyyy-MM-dd HH:mm:ss");
    }

    public static void exportExcel(String[] headers, String[] fields, Collection dataset,
                            OutputStream out) {
        exportExcel("导出EXCEL文档", headers, fields, dataset, out, "yyyy-MM-dd HH:mm:ss");
    }

    public static void exportExcel(String[] headers, String[] fields, Collection dataset,
                            OutputStream out, String pattern) {
        exportExcel("导出EXCEL文档", headers, fields, dataset, out, pattern);
    }
    
    /**
     * sjx
     * @throws Exception 
     */
    public static void exportExcel(HttpServletRequest request,HttpServletResponse response,
    								List dataset, String fileName,String year,String month) throws Exception {
    	exportExcel(request,response,fileName, dataset,year,month);
    }
    /**
     * @param request
     * @param response
     * @param dataset
     * @throws Exception
     * 工作中心--汇报
     */
    public static void exportExcel(String fileName,String []headers,String []fieldNames,List dataset,HttpServletResponse response,UserConnector userConnector,JdbcTemplate jdbcTemplate) throws Exception {
    	exportReportExcel(fileName,headers,fieldNames,dataset,response,userConnector,jdbcTemplate);
    }
    /**
     * @param dataList
     * @param out
     * 工作中心--汇报的导出实现
     */
    public static void exportReportExcel(String fileName,String []headers,String []fieldNames,List<Map<String,Object>> dataList,HttpServletResponse response,UserConnector userConnector,JdbcTemplate jdbcTemplate){
    	//声明工作簿
    	HSSFWorkbook workBook = new HSSFWorkbook();
    	HSSFSheet sheet = workBook.createSheet("导出EXCEL文档");
    	sheet.setDefaultColumnWidth(20);
    	//产生表格标题行
        HSSFRow row = sheet.createRow(0);
        for (short i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(text);
        }
        //遍历集合数据，产生数据行
        Iterator it = dataList.iterator();
        int index = 0;
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            HashMap map =  (HashMap) it.next();
            //翻译汇报类型
            if(map.get("type") != null){
            	switch(map.get("type").toString()){
            		case "1":
            			map.put("type", "周报");
            			break;
            		case "2":
            			map.put("type", "月报");
            			break;
            		case "3":
            			map.put("type", "年报");
            			break;
            		default:
            			map.put("type", "专项");
            			
            	}
            }
            //翻译状态
            if(map.get("status") != null){
            	switch(map.get("status").toString()){
            	case "0":
            		map.put("status", "未读");
            		break;
            	case "1":
            		map.put("status", "已读");
            		break;
            	default:
            		map.put("status", "已反馈");
            		
            	}
            }
            //获取接收人的姓名
            if(map.get("sendee") != null){
            	UserDTO user = userConnector.findByIdAll(String.valueOf(map.get("sendee")));
            	String disName = "";
            	if(user != null)
            	  disName = user.getDisplayName();
            	map.put("sendee", disName);
            }
            //获取汇报人的姓名
            if(map.get("user_id") != null){
            	UserDTO user = userConnector.findByIdAll(String.valueOf(map.get("user_id")));
            	String disName = "";
            	if(user != null)
            		disName = user.getDisplayName();
            	map.put("user_id", disName);
            }
            //获取汇报的附件名称
            String pkId = map.get("id").toString();
            String sqlStore = "select s.name from store_info s where s.model='OA/report' and s.pk_id="+pkId;
            List<Map<String,Object>> storeNameList = jdbcTemplate.queryForList(sqlStore);
            String storeName = "";
            for(Map<String,Object> storeNameMap : storeNameList){
            	storeName += storeNameMap.get("name")+",";
            }
            map.put("storeName", storeName);
            //获取汇报的抄送人并翻译姓名
            String sqlCc = "select ccno from work_report_cc wrc where wrc.INFO_ID="+pkId;
            List<Map<String,Object>> ccNameList = jdbcTemplate.queryForList(sqlCc);
            String ccNames = "";
            for(Map<String,Object> ccNameMap : ccNameList){
            	UserDTO user = userConnector.findById(String.valueOf(ccNameMap.get("ccno")));
            	if(user != null)
            		ccNames += user.getDisplayName()+",";
            	
                	if(null==ccNameMap.get("ccno"))
                		continue;
                	
                String strSql="select * from party_entity where id=%s";
                
                List<Map<String,Object>> partyMapList=jdbcTemplate.queryForList(String.format(strSql, ccNameMap.get("ccno").toString()));
                if(partyMapList.size()>0){
                	ccNames +=partyMapList.get(0).get("name")+",";
                }
            }
            map.put("ccNames", ccNames);
            for (short i = 0; i < fieldNames.length; i++) {
                HSSFCell cell = row.createCell(i);
                String fieldName = fieldNames[i];
                try {
					writeValue(workBook, cell,map, fieldName);//新增重载的方法
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
            }
        }
        try {
        	response.setContentType("application/vnd.ms-excel;charset=utf-8");
		    response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("gbk"), "iso8859-1"));
        	OutputStream out = response.getOutputStream();
            workBook.write(out);
            out.flush();
	        out.close();
        } catch (IOException e) {
            // TODO 如果报错需要抛出去
            e.printStackTrace();
        }
    }
    /**
     * 这是一个通用的方法，利用了JAVA的反射机制，可以将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上
     *
     * @param title   表格标题名
     * @param headers 表格属性列名数组
     * @param dataset 需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
     *                <p>
     *                javabean属性的数据类型有基本数据类型及String,Date,byte[](图片数据)
     * @param out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern 如果有时间数据，设定输出格式。默认为"yyyy-MM-dd hh:mm:ss"
     */

    @SuppressWarnings("unchecked")
    public static void exportExcel(String title, String[] headers, String[] fieldNames,
                            Collection dataset, OutputStream out, String pattern) {
        // 声明一个工作薄
        HSSFWorkbook workbook= new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(title);
        // 设置表格默认列宽度为20个字节
        sheet.setDefaultColumnWidth((short) 20);
        //产生表格标题行
        HSSFRow row = sheet.createRow(0);
        for (short i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(text);
        }
        //遍历集合数据，产生数据行
        Iterator it = dataset.iterator();
        int index = 0;
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            Object obj =  it.next();
            //如果没有传fileNames，则利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            if (fieldNames == null) {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (short i = 0; i < fields.length; i++) {

                    HSSFCell cell = row.createCell(i);
                    Field field = fields[i];
                    String fieldName = field.getName();
                    writeValue(workbook, cell, obj, fieldName, pattern);
                }
            } else {
                //遍历fieldNams，写到cell里
                for (short i = 0; i < fieldNames.length; i++) {
                    HSSFCell cell = row.createCell(i);
                    String fieldName = fieldNames[i];
                    writeValue(workbook, cell,obj, fieldName, pattern);
                }
            }
        }
        
        try {
            workbook.write(out);
        } catch (IOException e) {
            // TODO 如果报错需要抛出去
            e.printStackTrace();
        }
    }

    /**
     *获取实体的属性值，进行处理，然后写到cell里
     * @param workbook
     * @param cell
     * @param obj
     * @param fieldName
     * @param pattern
     */
    private static void writeValue(Workbook workbook, Cell cell, Object obj, String fieldName, String pattern) {
        try {
            //1、获取字段的get方法
            String getMethodName = "get"
                    + fieldName.substring(0, 1).toUpperCase()
                    + fieldName.substring(1);
            Class tCls = obj.getClass();
            Method getMethod = tCls.getMethod(getMethodName, new Class[]{});
//            2、获取到字段值
            Object value = getMethod.invoke(obj, new Object[]{});
            if (value == null) {
                return;
            }
            //3、以下是对值进行处理 然后写到cell里
            //判断值的类型后进行强制类型转换
            String textValue = null;
            if (value instanceof Boolean) {
                boolean bValue = (Boolean) value;
                textValue = "男";
                if (!bValue) {
                    textValue = "女";
                }
            } else if (value instanceof Date) {
                Date date = (Date) value;
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                textValue = sdf.format(date);
            } else {
                //其它数据类型都当作字符串简单处理
                textValue = value.toString();
            }
            //如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成
            if (textValue != null) {
                Pattern p = Pattern.compile("^//d+(//.//d+)?$");
                Matcher matcher = p.matcher(textValue);
                if (matcher.matches()) {
                    //是数字当作double处理
                    cell.setCellValue(Double.parseDouble(textValue));
                } else {
                    HSSFRichTextString richString = new HSSFRichTextString(textValue);
                    cell.setCellValue(richString);
                }
            }
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            //清理资源
        }
    }
    private static void writeValue(Workbook workbook, Cell cell, Map map, String fieldName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try {
      	  Set keys = map.keySet();
      	  Object value = null;
      	  if(keys != null){
      		  Iterator it = keys.iterator();
      		  while(it.hasNext()){
                    Object key=it.next();
                    if(key.toString().equals(fieldName)){
              		  value=map.get(key);
                  	  break;
                    }
                }
      	  }
      	  if (value == null) {
                return;
            }
            //3、以下是对值进行处理 然后写到cell里
            //判断值的类型后进行强制类型转换
            String textValue = null;
            if (value instanceof Boolean) {
                boolean bValue = (Boolean) value;
                textValue = "男";
                if (!bValue) {
                    textValue = "女";
                }
            } else if (value instanceof Date) {
                Date date = (Date) value;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                textValue = sdf.format(date);
            } else {
                //其它数据类型都当作字符串简单处理
                textValue = value.toString();
            }
            //如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成
            if (textValue != null) {
                Pattern p = Pattern.compile("^//d+(//.//d+)?$");
                Matcher matcher = p.matcher(textValue);
                if (matcher.matches()) {
                    //是数字当作double处理
                    cell.setCellValue(Double.parseDouble(textValue));
                } else {
                    HSSFRichTextString richString = new HSSFRichTextString(textValue);
                    cell.setCellValue(richString);
                }
            }
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            //清理资源
        }
    }
    /**
     * sjx
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static void exportExcel(HttpServletRequest request,HttpServletResponse response,
    			String fileName,List dataset,String year,String month) throws Exception{ 
  	  OutputStream out = response.getOutputStream();
  	  if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } else {
            fileName = new String(fileName.getBytes(), "ISO8859-1");
        }
        // 设置response参数，可以打开下载页面
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
  	  
        String title = "导出EXCEL文档";
  	    // 声明一个工作薄
        HSSFWorkbook workbook= new HSSFWorkbook();
        //创建单元格格式
        HSSFCellStyle style =workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中  
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(title);
        // 设置表格默认列宽度为10个字节
        sheet.setDefaultColumnWidth(10);
        sheet.setDefaultRowHeight((short)400);
        sheet.setColumnWidth(1, 5000);//工号列数据较长
        //产生表格标题行(标题行列数部确定，由当前月天数确定)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,Integer.parseInt(year));
        calendar.set(Calendar.MONTH,Integer.parseInt(month)-1);
        calendar.set(Calendar.DATE, 1);//把日期设置为当月第一天  
        calendar.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天  
	    int maxDate = calendar.get(Calendar.DATE);
        HSSFRow row = sheet.createRow(0);
        row.setHeight((short)400);
        int index = 1;
        for (int i = 0; i < maxDate*2+maxDate+4;) {
            HSSFCell cell = row.createCell(i);
            cell.setCellStyle(style);//使用样式
            HSSFRichTextString text;
            if(i==0){
            	text = new HSSFRichTextString("机构");
            }else if(i==1){
            	text = new HSSFRichTextString("工号");
            }else if(i==2){
            	text = new HSSFRichTextString("姓名");
            }else if(i==3){
            	text = new HSSFRichTextString("月份");
            }else{
            	//获取周几
            	String dateStr = DateUtil.formatDateStrToStr(year+"-"+month+"-"+index, "yyyy-MM-dd");
            	String weekCSStart = DateUtil.getWeekCS(dateStr);
            	text = new HSSFRichTextString(String.valueOf(index)+" ("+weekCSStart+")");
            	index++;
            }
            if(i >= 4){
            	sheet.addMergedRegion(new CellRangeAddress(0,0,i,i+2));//表头日期号合并
            	sheet.setColumnWidth(i, 2000);
            	sheet.setColumnWidth(i+1, 2000);
            	sheet.setColumnWidth(i+2, 2000);
            	i=i+3;
            }else{
            	i++;
            }
            cell.setCellValue(text);
            
        }
        int orgBeginIndex = 1;//机构合并的开始索引并初始化为1
    	for(int j=1;j<dataset.size();j++){//索引从1开始遍历，取消遍历表头
    		int col = 0;
    		row = sheet.createRow(j);
    		Map<String,String> m = (Map<String, String>) dataset.get(j);
    		Map<String,String> afterm = new HashMap<String,String>();
    		if(j < dataset.size()-1){
    			afterm = (Map<String, String>) dataset.get(j+1);
    		}
    		int afterj = j+1;
    		/*if(m.get(j+"_0").equals(afterm.get(afterj+"_0"))){此合并机构部支持上下居中显示
    			sheet.addMergedRegion(new CellRangeAddress(j,j+1,0,0));//合并机构
    		}*/
    		if(!m.get(j+"_0").equals(afterm.get(afterj+"_0"))){
    			sheet.addMergedRegion(new CellRangeAddress(orgBeginIndex,j,0,0));//合并机构
    			orgBeginIndex = j+1;//下一个机构合并开始索引赋值
    		}
    		
    		if(j%2!=0){//奇数行
    			for(Map.Entry entry : m.entrySet()){
        			HSSFCell cell = row.createCell(col);
        			String text = m.get(j+"_"+col);
        			sheet.addMergedRegion(new CellRangeAddress(j,j+1,1,1));//合并工号
        			sheet.addMergedRegion(new CellRangeAddress(j,j+1,2,2));//合并姓名
        			sheet.addMergedRegion(new CellRangeAddress(j,j+1,3,3));//合并月份
        			if(col%3==0&&col!=0&&col!=3){
        				sheet.addMergedRegion(new CellRangeAddress(j,j+1,col,col));//合并请假的单元格
        			}
        			cell.setCellStyle(style);
            		cell.setCellValue(text);
            		col++;
        		}
    		}else{
    			for(int k=0;k<maxDate*3+3;k++){
        			HSSFCell cell = row.createCell(k);
        			if(k%3==0&&k!=3&&k!=0){
        				continue;
        			}
        			String text = m.get(j+"_"+col);
        			cell.setCellStyle(style);
            		cell.setCellValue(text);
            		col++;
        		}
    		}
    	}
        try {
            workbook.write(out);
            out.flush();
            out.close();
        } catch (IOException e) {
            // TODO 如果报错需要抛出去
            e.printStackTrace();
        }
    }
}