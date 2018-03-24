package org.jivesoftware.openfire.plugin.rest.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class ImageUtil {
	private static Logger Log = LoggerFactory.getLogger(ImageUtil.class);
//	static String Url = "http://120.78.12.196:8683/fileServer/upload/voice";
/**
     * 生成组合头像
     * 
     * @param paths
     *            用户图像
     * @throws IOException
     */
    public static String getCombinationOfhead(List<String> paths) {
    	BufferedImage outImage = null;
    	ByteArrayOutputStream out = null;
        try {
			List<BufferedImage> bufferedImages = new ArrayList<BufferedImage>();
			// 压缩图片所有的图片生成尺寸同意的 为 50x50
			int x = 50;
			int y = 50;
			for (int i = 0; i < paths.size(); i++) {
				//如果图片多于4个    图片尺寸为30px
				if(paths.size()>4){
					x = 30;
					y = 30;
				}
				bufferedImages.add(ImageUtil.resize2(paths.get(i), x, y, true));
			}

			int width = 112; // 这是画板的宽高

			int height = 112; // 这是画板的高度

			// BufferedImage.TYPE_INT_RGB可以自己定义可查看API

			outImage = new BufferedImage(width, height,
			        BufferedImage.TYPE_INT_RGB);

			// 生成画布
			Graphics g = outImage.getGraphics();
			
			Graphics2D g2d = (Graphics2D) g;
			
			// 设置背景色
			g2d.setBackground(new Color(231,231,231));
			
			// 通过使用当前绘图表面的背景色进行填充来清除指定的矩形。
			g2d.clearRect(0, 0, width, height);
			
			// 开始拼凑 根据图片的数量判断该生成那种样式的组合头像目前为4中
			int j = 1;
			for (int i = 1; i <= bufferedImages.size(); i++) {
				if (bufferedImages.size() == 9) {
			        if (i <= 3) {
			        	g2d.drawImage(bufferedImages.get(i - 1), 30 * i + 5 * i
			                    - 30 , 5 , null);
			        } else if(i<=6){
						g2d.drawImage(bufferedImages.get(i - 1), 30 * (i-3) + 5 * (i-3)
			                    - 30 , 40 , null);
			        } else{
			            g2d.drawImage(bufferedImages.get(i - 1), 30 * j + 5 * j
			                    - 30 ,  76, null);
			            j++;
			        }

			    } else if (bufferedImages.size() == 8) {
			        if (i <= 2) {
			            g2d.drawImage(bufferedImages.get(i - 1), 45 * i + 5 * i
			                    - 35, 5, null);

			        } else if(i<=5){
						g2d.drawImage(bufferedImages.get(i - 1), 30 * (i-2) + 5 * (i-2)
			                    - 30 , 40 , null);
			        } else{
			            g2d.drawImage(bufferedImages.get(i - 1), 30 * j + 5 * j
			                    - 30 ,  76, null);
			            j++;
			        }

			    } else if (bufferedImages.size() == 7) {
			        if (i <= 1) {
			            g2d.drawImage(bufferedImages.get(i - 1), 40, 5, null);

			        } else if(i<=4){
						g2d.drawImage(bufferedImages.get(i - 1), 30 * (i-1) + 5 * (i-1)
			                    - 30 , 40 , null);
			        } else{
			            g2d.drawImage(bufferedImages.get(i - 1), 30 * j + 5 * j
			                    - 30 ,  76, null);
			            j++;
			        }

			    } else if(bufferedImages.size()==6){
					if(i <= 3){
						g2d.drawImage(bufferedImages.get(i - 1), 30 * i + 5 * i
			                    - 30 , 22, null);
			        } else {
			            g2d.drawImage(bufferedImages.get(i - 1), 30 * j + 5 * j
			                    - 30, 60, null);
			            j++;
			        }
				}
				if(bufferedImages.size()==5){
					if(i <= 1){
						g2d.drawImage(bufferedImages.get(i - 1), 24 , 22, null);
					}else if (i <= 2) {
			            g2d.drawImage(bufferedImages.get(i - 1), 62 , 22, null);
			        } else {
			            g2d.drawImage(bufferedImages.get(i - 1), 30 * j + 5 * j
			                    - 30, 60, null);
			            j++;
			        }
				}else if (bufferedImages.size() == 4) {
			        if (i <= 2) {
			            g2d.drawImage(bufferedImages.get(i - 1), 50 * i + 4 * i
			                    - 50, 4, null);
			        } else {
			            g2d.drawImage(bufferedImages.get(i - 1), 50 * j + 4 * j
			                    - 50, 58, null);
			            j++;
			        }
			    } else if (bufferedImages.size() == 3) {
			        if (i <= 1) {

			            g2d.drawImage(bufferedImages.get(i - 1), 31, 4, null);

			        } else {

			            g2d.drawImage(bufferedImages.get(i - 1), 50 * j + 4 * j
			                    - 50, 58, null);

			            j++;
			        }

			    } else if (bufferedImages.size() == 2) {

			        g2d.drawImage(bufferedImages.get(i - 1), 50 * i + 4 * i - 50,
			                31, null);

			    } else if (bufferedImages.size() == 1) {

			        g2d.drawImage(bufferedImages.get(i - 1), 31, 31, null);

			    }

			    // 需要改变颜色的话在这里绘上颜色。可能会用到AlphaComposite类
			}

			out = new ByteArrayOutputStream();
			String result = null;
			try {
			ImageIO.write(outImage, "JPG", out);
			Map<String,byte[]> map = new HashMap<String,byte[]>();
			map.put("123.jpg", out.toByteArray());
				result = HttpsUtils.postFile(JiveGlobals.getProperty("plugin.restapi.fileserver"), map);
			} catch (Exception e) {
				Log.error("ImageUtil : " +e.getMessage());
				return null;
			}
			if(result!=null){
				JSONObject jsonStr = JSONObject.parseObject(result);
				Object code = jsonStr.get("code");
				if("0".equals(code)){
					Object url = jsonStr.get("url");
					return (String)url;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				outImage.flush();
			} catch (Exception e) {
				e.printStackTrace();
				Log.error("BufferedImage flush error");
			}
			try {
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
				Log.error("ByteArrayOutputStream flush error");
			}
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				Log.error("ByteArrayOutputStream close error");
			}
		}
		return null;
    }

    /**
     * 图片缩放
     * 
     * @param filePath
     *            图片路径
     * @param height
     *            高度
     * @param width
     *            宽度
     * @param bb
     *            比例不对时是否需要补白
     */
    public static BufferedImage resize2(String filePath, int height, int width,
            boolean bb) {
        try {
            double ratio = 0; // 缩放比例 
           // File f = new File(filePath);
            if(filePath==null||"".equals(filePath)){
            	filePath = "http://app.xiaopao69.com/fileServer/image/1111111111111111111111111111.png";
            }
            URL url = new URL(filePath);
            BufferedImage bi = ImageIO.read(url);
            Image itemp = bi.getScaledInstance(width, height,
                    Image.SCALE_SMOOTH);
            // 计算比例
            if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
                if (bi.getHeight() > bi.getWidth()) {
                    ratio = (new Integer(height)).doubleValue()
                            / bi.getHeight();
                } else {
                    ratio = (new Integer(width)).doubleValue() / bi.getWidth();
                }
                AffineTransformOp op = new AffineTransformOp(
                        AffineTransform.getScaleInstance(ratio, ratio), null);
                itemp = op.filter(bi, null);
            }
            if (bb) {
                // copyimg(filePath, "E:\\img");
                BufferedImage image = new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.white);
                g.fillRect(0, 0, width, height);
                if (width == itemp.getWidth(null))
                    g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2,
                            itemp.getWidth(null), itemp.getHeight(null),
                            Color.white, null);
                else
                    g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0,
                            itemp.getWidth(null), itemp.getHeight(null),
                            Color.white, null);
                g.dispose();
                itemp = image;
            }
            return (BufferedImage) itemp;
        } catch (IOException e) {
            Log.error("ImageUtil : " +e.getMessage());
        }
        return null;
    }
	public static void main(String[] args) throws Exception {
		//List<String> list = new ArrayList<String>();
		//list.add("http://120.78.12.196:8683/fileServer/voice/4c3594fa170749dfa653aab9ff51f9a21503740443043.png");
		//list.add("http://120.78.12.196:8683/fileServer/voice/353cdc1033ee43ea9bc9c0da006725f71504258899189.png");		
		//list.add("http://120.78.12.196:8683/fileServer/voice/62f20c3867504977b77ab9d4bfa0c8711504231283747.png");
//		list.add("http://120.78.12.196:8683/fileServer/voice/048191a11bec4725a4f40c68950389461503740549601.jpeg");
//		list.add("http://120.78.12.196:8683/fileServer/voice/531c84a5ad5c4d4096e2468e5c284f991503762182644.png");
//		list.add("http://120.78.12.196:8683/fileServer/voice/f4bda96b5efd4576a83fcf08c348601b1503743478335.png");
//		list.add("http://120.78.12.196:8683/fileServer/voice/8ea446fdad0446e1959c55e4acab910e1503740553954.png");
//		list.add("http://120.78.12.196:8683/fileServer/voice/a93e62741618442395aff80078959ab81503740577948.png");
//		list.add("http://120.78.12.196:8683/fileServer/voice/ca54e9cdf77040d8b508ace7b71156dd1503743778856.png");
		//getCombinationOfhead(list);
	}

}
