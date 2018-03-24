package org.jivesoftware.openfire.plugin.rest.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsUtils {
	private static class TrustAnyTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}

	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	/**
	 * post方式请求服务器(https协议)
	 * 
	 * @param url
	 *            请求地址
	 * @param content
	 *            参数
	 * @param charset
	 *            编码
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @throws IOException
	 */
	public static String post(String url, String content)
			throws IOException, NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());

		URL console = new URL(url);
		HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
		conn.setSSLSocketFactory(sc.getSocketFactory());
		conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.connect();
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.write(content.getBytes("UTF-8"));
		// 刷新、关闭
		out.flush();
		out.close();
		InputStream is = conn.getInputStream();
		if (is != null) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = is.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			is.close();
			return new String(outStream.toByteArray(), "utf-8");
		}
		return null;
	}
	public static String get(String url,String data) throws IOException, NoSuchAlgorithmException, KeyManagementException{
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
		BufferedReader in = null;        
        StringBuilder result = new StringBuilder(); 
        String path = url + "?"+data;
        URL urls = new URL(path);
        HttpsURLConnection conn = (HttpsURLConnection)urls.openConnection();
        conn.setSSLSocketFactory(sc.getSocketFactory());
        conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
        conn.setRequestMethod("GET");
        //Get请求不需要DoOutPut 
        conn.setDoOutput(false);
        conn.setDoInput(true);
        //设置连接超时时间和读取超时时间
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //连接服务器
        conn.connect();  
        // 取得输入流，并使用Reader读取  
        in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String line;
        while ((line = in.readLine()) != null) {
            result.append(line);
        }
        //关闭输入流
        in.close();
        return result.toString();
	}
	 //以数据流的形式传参
    public static String postFile(String actionUrl, Map<String, byte[]> files)
            throws Exception
    {
        StringBuilder sb2 = null;
        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";
        URL uri = new URL(actionUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(6 * 1000); // 缓存的最长时间
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
        // 首先组拼文本类型的参数
       // StringBuilder sb = new StringBuilder();
       /* for (Map.Entry<String, String> entry : params.entrySet())
        {
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            sb.append(LINEND);
            sb.append(entry.getValue());
            sb.append(LINEND);
        }*/
        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
        //outStream.write(sb.toString().getBytes());
        InputStream in = null;
        // 发送文件数据
        if (files != null)
        {
            for (Map.Entry<String, byte[]> file : files.entrySet())
            {
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                sb1.append("Content-Disposition: form-data; name=\"pic\"; filename=\"" + file.getKey() + "\"" + LINEND);
                sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
                sb1.append(LINEND);
                outStream.write(sb1.toString().getBytes());
                // InputStream is = new FileInputStream(file.getValue());
                // byte[] buffer = new byte[1024];
                // int len = 0;
                // while ((len = is.read(buffer)) != -1)
                // {
                // outStream.write(buffer, 0, len);
                // }
                // is.close();
                outStream.write(file.getValue());
                outStream.write(LINEND.getBytes());
            }
            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();
            // 得到响应码
            int res = conn.getResponseCode();
            if (res == 200)
            {
                in = conn.getInputStream();
                int ch;
                sb2 = new StringBuilder();
                while ((ch = in.read()) != -1)
                {
                    sb2.append((char) ch);
                }
                System.out.println(sb2.toString());
            }
            outStream.flush();
            outStream.close();
            in.close();
            conn.disconnect();
            // 解析服务器返回来的数据
            return sb2.toString();
        }
        else
        {
            return "Update icon Fail";
        }
        // return in.toString();
    }
}
