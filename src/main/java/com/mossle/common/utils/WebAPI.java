package com.mossle.common.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.mossle.api.store.StoreConnector;
import com.mossle.internal.store.persistence.domain.StoreInfo;


public class WebAPI {
	
	private String uploadUrl;
	private String downloadUrl;
	private String viewUrl;
	private String  autostartup;
	
	public String upload2WWW(File file, String param) throws Exception{
		HttpURLConnection conn = null; 
		OutputStream out = null;
		DataInputStream in = null;
		try {
			String BOUNDARY = "---------7d4a6d158c9"; // 定义数据分隔线
			String _url = uploadUrl;
			URL url = new URL(_url + param);
			// System.out.println("================== 开始连接文件服务器 ==========================");
			conn = (HttpURLConnection) url.openConnection();
			System.out.println("================== 文件服务器连接成功 ==========================");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			
			conn.setRequestProperty("connection",  "Keep-Alive");
			conn.setRequestProperty("user-agent",  "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			conn.setRequestProperty("Charsert",    "UTF-8");
			conn.setRequestProperty("Accept",      "text/html, image/gif, image/jpeg, image/png, *; q=.2, */*; q=.2");
			conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + BOUNDARY);
			
			out = new DataOutputStream(conn.getOutputStream());
			
			// System.out.println("================== 读取文件成功  ==========================");
			
			byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线
			StringBuilder sb = new StringBuilder();
			sb.append("--");
			sb.append(BOUNDARY);
			sb.append("\r\n");
			sb.append("Content-Disposition: form-data; name=\"image" + "\"; filename=\"" + file.getName() + "\" \r\n");
			sb.append("Content-Type: image/jpeg \r\n\r\n");
			byte[] data = sb.toString().getBytes();
			
			out.write(data);
			
			//System.out.println("================== 输出文件头  ==========================");
			in = new DataInputStream(new FileInputStream(file));
			int bytes = 0;
			byte[] bufferOut = new byte[1024];
			while ((bytes = in.read(bufferOut)) != -1) {
				out.write(bufferOut, 0, bytes);
			}
			// out.write("\r\n".getBytes()); //多个文件时，二个文件之间加入这个
	
			in.close();
			out.write(end_data);
			out.flush();
			out.close();
			// System.out.println("================== 文件上传成功   ==========================");
			// 定义BufferedReader输入流来读取URL的响应
			StringBuffer ret = new StringBuffer("");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				ret.append(line);
			}
			
			// System.out.println("================== 接收返回参数成功   ==========================");
			
			conn.disconnect();  
			
			return ret.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		} finally{  
		
			try {  
		        if (out != null) {  
		            out.close();  
		        }  
		    } catch (Exception e) {  
		        e.printStackTrace();  
		    }  
			
			try {  
		        if (in != null) {  
		            in.close();  
		        }  
		    } catch (Exception e) {  
		        e.printStackTrace();  
		    } 
			
			conn.disconnect();  
        } 
	}

	public void setUploadUrl(String uploadUrl) {
    	this.uploadUrl = uploadUrl;
    }

	public String getUploadUrl() {
		return uploadUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getViewUrl() {
		return viewUrl;
	}

	public void setViewUrl(String viewUrl) {
		this.viewUrl = viewUrl;
	}
	/**
	 * @param autostartup 要设置的 autostartup
	 */
	public void setAutostartup(String autostartup) {
		this.autostartup = autostartup;
	}

	public String getAutostartup() {
		// TODO 自动生成的方法存根
		return autostartup;
	}

}
