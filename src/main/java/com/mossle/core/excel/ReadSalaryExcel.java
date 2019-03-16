package com.mossle.core.excel;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

/**
  * 
  * @author lilei
  * 
  */
 public class ReadSalaryExcel {
	 // 总行数
	 private int totalRows = 0;
	 // 总条数
	 private int totalCells = 0;
	 // 错误信息接收器
	 private String errorMsg;
	 // 构造方法
	 public ReadSalaryExcel() {
	 }
	 // 获取总行数
	 public int getTotalRows() {
		 return totalRows;
	 }
	 // 获取总列数
	 public int getTotalCells() {
		 return totalCells;
	 }
	 // 获取错误信息
	 public String getErrorInfo() {
		 return errorMsg;
	 }
	 /**
	  * 读EXCEL文件，获取信息集合
	  * 
	  * @param fielName
	  * @return
	  */
	 public List<Map<String, Object>> getExcelInfo(MultipartFile mFile,int startRow) {
		 String fileName = mFile.getOriginalFilename();// 获取文件名
 //        List<Map<String, Object>> userList = new LinkedList<Map<String, Object>>();
		 try {
			 if (!validateExcel(fileName)) {// 验证文件名是否合格
				 return null;
			 }
			 boolean isExcel2003 = true;// 根据文件名判断文件是2003版本还是2007版本
			 if (isExcel2007(fileName)) {
				 isExcel2003 = false;
			 }
			 return createExcel(mFile.getInputStream(), isExcel2003,startRow);
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return null;
	 }
	 
	 /**
	  * 根据excel里面的内容读取客户信息
	  * 
	  * @param is      输入流
	  * @param isExcel2003   excel是2003还是2007版本
	  * @return
	  * @throws IOException
	  */
	 public List<Map<String, Object>> createExcel(InputStream is, boolean isExcel2003,int startRow) {
		 try {
			 Workbook wb = null;
			 if (isExcel2003) {// 当excel是2003时,创建excel2003
				 wb = new HSSFWorkbook(is);
			 } else {// 当excel是2007时,创建excel2007
				 wb = new XSSFWorkbook(is);
			 }
			 return readExcelValue(wb,startRow);// 读取Excel里面客户的信息
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
		 return null;
	 }
	 
	 /**
	  * 读取Excel里面客户的信息
	  * 
	  * @param wb
	  * @return
	  */
	 private List<Map<String, Object>> readExcelValue(Workbook wb,int startRow) {
		 // 得到第一个shell
		 Sheet sheet = wb.getSheetAt(0);
		 // 得到Excel的行数
		 this.totalRows = sheet.getPhysicalNumberOfRows();
		 // 得到Excel的列数(前提是有行数)
		 if (totalRows > 1 && sheet.getRow(0) != null) {
			 this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
		 }
		 List<Map<String, Object>> excelInfoList = new ArrayList<Map<String, Object>>();
		 
		 // 循环Excel行数
		 for (int r = startRow; r < totalRows; r++) {
			 Row row = sheet.getRow(r);
			 if (row == null) {
				 continue;
			 }
			 // 循环Excel的列
			 Map<String, Object> map = new HashMap<String, Object>();
			 for (int c = 0; c < this.totalCells; c++) {
				 Cell cell = row.getCell(c);
				 if (null != cell) {
					
					 String columnNum=String.valueOf(c);
					 if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
						 if(HSSFDateUtil.isCellDateFormatted(cell))
						 {
							 short format = cell.getCellStyle().getDataFormat();
							 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm"); 
							 if(format == 14 || format == 31 || format == 57 || format == 58){  
						        //日期  
								 dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
						    }else if (format == 20 || format == 32) {  
						        //时间  
						    	dateFormat = new SimpleDateFormat("HH:mm");  
						    }
						    else if(format==22){
						    	//时间  
						    	dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm"); 
						    }
							 double value = cell.getNumericCellValue();  
						    Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);  
						    String dateValue=dateFormat.format(date);
						    map.put(columnNum,dateValue);
						 }
						 else {
							String name = String.valueOf(cell.getNumericCellValue());
							//String trim = cell.toString().trim();
							 //map.put(columnNum, name.substring(0, name.length() - 2 > 0 ? name.length() - 2 : 1));
							int length=name.length();
							if (length>=2) {  //这里大于等于2是防止有些列只有一个字符，到下面会报错
								if(name.contains(".")){
									int lastIndexOf = name.lastIndexOf(".");
									String substring = name.substring(lastIndexOf+1, length);
									if(substring.length()<2){
										map.put(columnNum, name+"0");
									}else{
										map.put(columnNum, name);
									}
								}else{
									map.put(columnNum, name+".00");
								}
								
								/*if (name.substring(length-2, length).equals(".0")){ //通过截取最后两个字符，如果等于.0 就去除最后两个字符
								    map.put(columnNum, name+"0");
								}else{
									map.put(columnNum, name);
								}*/
							}else { 
								map.put(columnNum, name);
							} 
							 
						}
/*=======
						// int cellType = cell.getCellType();
						 //ckx add 2018/08/14  判断日期格式
						 boolean b = HSSFDateUtil.isCellDateFormatted(cell);
						 //short dataFormat = cell.getCellStyle().getDataFormat();
						// System.out.println(cellType +"==="+dataFormat+"||||"+b);
						 if(b){
							 double d = cell.getNumericCellValue();
				               Date date = HSSFDateUtil.getJavaDate(d);
				               SimpleDateFormat dformat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
				               String val = dformat.format(date);
				               map.put(columnNum, val);
						 }else{
							 String name = String.valueOf(cell.getNumericCellValue());
							 map.put(columnNum, name.substring(0, name.length() - 2 > 0 ? name.length() - 2 : 1));
							 
						 }
>>>>>>> .r6086*/
					 } else{
						 cell.setCellType(Cell.CELL_TYPE_STRING);
						 map.put(columnNum, cell.getStringCellValue());
					 }
				 }
			 }
			 // 添加到list
			 excelInfoList.add(map);
		 }
		 return excelInfoList;
	 }
	 
	 /**
	  * 验证EXCEL文件
	  * 
	  * @param filePath
	  * @return
	  */
	 public boolean validateExcel(String filePath) {
		 if (filePath == null || !(isExcel2003(filePath) || isExcel2007(filePath))) {
			 errorMsg = "文件名不是excel格式";
			 return false;
		 }
		 return true;
	 }
	 // @描述：是否是2003的excel，返回true是2003
	 public static boolean isExcel2003(String filePath) {
		 return filePath.matches("^.+\\.(?i)(xls)$");
	 }
	 // @描述：是否是2007的excel，返回true是2007
	 public static boolean isExcel2007(String filePath) {
		 return filePath.matches("^.+\\.(?i)(xlsx)$");
	 }
 }