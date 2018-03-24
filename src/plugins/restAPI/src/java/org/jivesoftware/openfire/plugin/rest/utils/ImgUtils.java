package org.jivesoftware.openfire.plugin.rest.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.jivesoftware.util.Base64;


public class ImgUtils {
	/** 
	 * @Title           getImgeHexString 
	 * @Description     网络图片转换成二进制字符串 
	 * @param URLName   网络图片地址 
	 * @param type      图片类型 
	 * @return  String  转换结果 
	 * @throws 
	 */  
	public static String getImgeHexString(String URLName,String type) {  
	    String res = null;  
	    String path = System.getProperty("openfireHome") + "/temp/";
	    try {  
	        int HttpResult = 0; // 服务器返回的状态  
	        URL url = new URL(URLName); // 创建URL  
	        URLConnection urlconn = url.openConnection(); // 试图连接并取得返回状态码  
	        urlconn.connect();  
	        HttpURLConnection httpconn = (HttpURLConnection) urlconn;  
	        HttpResult = httpconn.getResponseCode();  
	        if (HttpResult != HttpURLConnection.HTTP_OK) // 不等于HTTP_OK则连接不成功  
	            System.out.print("fail");  
	        else {  
	            BufferedInputStream bis = new BufferedInputStream(urlconn.getInputStream());  
	            Image img = ImageIO.read(bis);
	            String name = UUID.randomUUID().toString()+".jpg";
	            String filePath = path+name;
	            saveMinPhoto(img, filePath, 400,1);
	            bis.close();
	            File file = new File(filePath);// 读入文件 
	            BufferedImage bri = ImageIO.read(file);      // 构造Image对象 
	            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
	            ImageIO.write(bri, type, byteOutputStream); 
	            byte[] bytes = byteOutputStream.toByteArray();
	            res = Base64.encodeBytes(bytes);
	        }  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	    return res;  
	}  
	  
	/** 
	 * @title           根据二进制字符串生成图片 
	 * @param data      生成图片的二进制字符串 
	 * @param fileName  图片名称(完整路径) 
	 * @param type      图片类型 
	 * @return 
	 */  
	public static void saveImage(String data, String fileName,String type) {  
	  
	    BufferedImage image = new BufferedImage(300, 300,BufferedImage.TYPE_BYTE_BINARY);  
	    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();  
	    try {  
	        ImageIO.write(image, type, byteOutputStream);  
	        // byte[] date = byteOutputStream.toByteArray();  
	        byte[] bytes = Base64.decode(data);
	        System.out.println("path:" + fileName);  
	        RandomAccessFile file = new RandomAccessFile(fileName, "rw");  
	        file.write(bytes);  
	        file.close();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  
	}  
	  
	/** 
	 * 反格式化byte 
	 *  
	 * @param s 
	 * @return 
	 */  
	public static byte[] hex2byte(String s) {  
	    byte[] src = s.toLowerCase().getBytes();  
	    byte[] ret = new byte[src.length / 2];  
	    for (int i = 0; i < src.length; i += 2) {  
	        byte hi = src[i];  
	        byte low = src[i + 1];  
	        hi = (byte) ((hi >= 'a' && hi <= 'f') ? 0x0a + (hi - 'a')  
	                : hi - '0');  
	        low = (byte) ((low >= 'a' && low <= 'f') ? 0x0a + (low - 'a')  
	                : low - '0');  
	        ret[i / 2] = (byte) (hi << 4 | low);  
	    }  
	    return ret;  
	}  
	  
	/** 
	 * 格式化byte 
	 *  
	 * @param b 
	 * @return 
	 */  
	public static String byte2hex(byte[] b) {  
	    char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',  
	            'B', 'C', 'D', 'E', 'F' };  
	    char[] out = new char[b.length * 2];  
	    for (int i = 0; i < b.length; i++) {  
	        byte c = b[i];  
	        out[i * 2] = Digit[(c >>> 4) & 0X0F];  
	        out[i * 2 + 1] = Digit[c & 0X0F];  
	    }  
	  
	    return new String(out);  
	} 
	
	
	/**
	 * 等比例压缩算法： 
	 * 算法思想：根据压缩基数和压缩比来压缩原图，生产一张图片效果最接近原图的缩略图
	 * @param srcURL 原图地址
	 * @param deskURL 缩略图地址
	 * @param comBase 压缩基数
	 * @param scale 压缩限制(宽/高)比例  一般用1：
	 * 当scale>=1,缩略图height=comBase,width按原图宽高比例;若scale<1,缩略图width=comBase,height按原图宽高比例
	 * @throws Exception
	 * @author shenbin
	 * @createTime 2014-12-16
	 * @lastModifyTime 2014-12-16
	 */
	public static void saveMinPhoto(Image img, String deskURL, double comBase,
			double scale) throws Exception {
		int srcHeight = img.getHeight(null);
		int srcWidth = img.getWidth(null);
		int deskHeight = 0;// 缩略图高
		int deskWidth = 0;// 缩略图宽
		double srcScale = (double) srcHeight / srcWidth;
		/**缩略图宽高算法*/
		if ((double) srcHeight > comBase || (double) srcWidth > comBase) {
			if (srcScale >= scale || 1 / srcScale > scale) {
				if (srcScale >= scale) {
					deskHeight = (int) comBase;
					deskWidth = srcWidth * deskHeight / srcHeight;
				} else {
					deskWidth = (int) comBase;
					deskHeight = srcHeight * deskWidth / srcWidth;
				}
			} else {
				if ((double) srcHeight > comBase) {
					deskHeight = (int) comBase;
					deskWidth = srcWidth * deskHeight / srcHeight;
				} else {
					deskWidth = (int) comBase;
					deskHeight = srcHeight * deskWidth / srcWidth;
				}
			}
		} else {
			deskHeight = srcHeight;
			deskWidth = srcWidth;
		}
		System.out.println(deskWidth+"----"+deskHeight);
		BufferedImage tag = new BufferedImage(deskWidth, deskHeight, BufferedImage.TYPE_3BYTE_BGR);
		tag.getGraphics().drawImage(img, 0, 0, deskWidth, deskHeight, null); //绘制缩小后的图
		FileOutputStream deskImage = new FileOutputStream(deskURL); //输出到文件流
		ImageIO.write(tag,"jpeg",deskImage);
		deskImage.close();
	}

	public static void main(String[] args) throws Exception{
		String data = getImgeHexString("http://wx.qlogo.cn/mmopen/AeV123NxiaXAzHbbEy6oojsaaUfsbJOSicibKxtiaK5VTibz6wiarHEqSrehD15egT2qbEichQREEI5MHzp0VWFCsRMx5HaupTkRibzO/0","png");
		saveImage(data, "789.png", "png");
		//saveMinPhoto("123.png", "456.png", 800, 1);
	}
}
