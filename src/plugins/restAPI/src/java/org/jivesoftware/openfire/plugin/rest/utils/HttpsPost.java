package org.jivesoftware.openfire.plugin.rest.utils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;  
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;  
import java.net.URL;  
import java.security.GeneralSecurityException;  
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;  
import javax.net.ssl.HttpsURLConnection;  
import javax.net.ssl.KeyManagerFactory;  
import javax.net.ssl.SSLContext;  
import javax.net.ssl.TrustManagerFactory;  
public class HttpsPost {
	 /** 
     * 获得KeyStore. 
     * @param keyStorePath 
     *            密钥库路径 
     * @param password 
     *            密码 
     * @return 密钥库 
     * @throws Exception 
     */  
    public static KeyStore getKeyStore(String password, String keyStorePath)  
            throws Exception {  
        // 实例化密钥库  
        KeyStore ks = KeyStore.getInstance("JKS");  
        // 获得密钥库文件流  
        FileInputStream is = new FileInputStream(keyStorePath);  
        // 加载密钥库  
        ks.load(is, password.toCharArray());  
        // 关闭密钥库文件流  
        is.close();  
        return ks;  
    }  
  
    /** 
     * 获得SSLSocketFactory. 
     * @param password 
     *            密码 
     * @param keyStorePath 
     *            密钥库路径 
     * @param trustStorePath 
     *            信任库路径 
     * @return SSLSocketFactory 
     * @throws Exception 
     */  
    public static SSLContext getSSLContext(String password,  
            String keyStorePath, String trustStorePath) throws Exception {  
        // 实例化密钥库  
        KeyManagerFactory keyManagerFactory = KeyManagerFactory  
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());  
        // 获得密钥库  
        KeyStore keyStore = getKeyStore(password, keyStorePath);  
        // 初始化密钥工厂  
        keyManagerFactory.init(keyStore, password.toCharArray());  
  
        // 实例化信任库  
        TrustManagerFactory trustManagerFactory = TrustManagerFactory  
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());  
        // 获得信任库  
        KeyStore trustStore = getKeyStore(password, trustStorePath);  
        // 初始化信任库  
        trustManagerFactory.init(trustStore);  
        // 实例化SSL上下文  
        SSLContext ctx = SSLContext.getInstance("TLS");  
        // 初始化SSL上下文  
        ctx.init(keyManagerFactory.getKeyManagers(),  
                trustManagerFactory.getTrustManagers(), null);  
        // 获得SSLSocketFactory  
        return ctx;  
    }  
  
    /** 
     * 初始化HttpsURLConnection. 
     * @param password 
     *            密码 
     * @param keyStorePath 
     *            密钥库路径 
     * @param trustStorePath 
     *            信任库路径 
     * @throws Exception 
     */  
    public static void initHttpsURLConnection(String password,  
            String keyStorePath, String trustStorePath) throws Exception {  
        // 声明SSL上下文  
        SSLContext sslContext = null;  
        // 实例化主机名验证接口  
        HostnameVerifier hnv = new MyHostnameVerifier();  
        try {  
            sslContext = getSSLContext(password, keyStorePath, trustStorePath);  
        } catch (GeneralSecurityException e) {  
            e.printStackTrace();  
        }  
        if (sslContext != null) {  
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext  
                    .getSocketFactory());  
        }  
        HttpsURLConnection.setDefaultHostnameVerifier(hnv);  
    }  
  
    /** 
     * 发送请求. 
     * @param httpsUrl 
     *            请求的地址 
     * @param xmlStr 
     *            请求的数据 
     */  
    public static String post(String httpsUrl, String xmlStr) {  
        HttpsURLConnection urlCon = null;  
        try {  
            urlCon = (HttpsURLConnection) (new URL(httpsUrl)).openConnection();  
            urlCon.setDoInput(true);  
            urlCon.setDoOutput(true);  
            urlCon.setRequestMethod("POST"); 
            urlCon.setRequestProperty("Content-Length",  
                    String.valueOf(xmlStr.getBytes().length));  
            urlCon.setUseCaches(false);  
            //设置为gbk可以解决服务器接收时读取的数据中文乱码问题  
            urlCon.getOutputStream().write(xmlStr.getBytes("utf-8"));  
            urlCon.getOutputStream().flush();  
            urlCon.getOutputStream().close(); 
            InputStream is = urlCon.getInputStream();
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
        } catch (MalformedURLException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }
		return null;  
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
            outStream.close();
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
