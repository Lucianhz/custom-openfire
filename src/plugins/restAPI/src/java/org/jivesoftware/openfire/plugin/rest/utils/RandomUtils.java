package org.jivesoftware.openfire.plugin.rest.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class RandomUtils {
	public static String random(){
		StringBuffer num = new StringBuffer(); 
		Random rm = new Random(); 
		num.append(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
		double pross = (1 + rm.nextDouble()) * Math.pow(10, 3);
		String str = String.valueOf(pross);
		num.append(str.substring(0, 3));
		return num.toString();
	}
	public static String random(int param){
		StringBuffer num = new StringBuffer(); 
		Random rm = new Random(); 
		num.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		double pross = (1 + rm.nextDouble()) * Math.pow(10, param);
		String str = String.valueOf(pross);
		num.append(str.substring(0, param));
		return num.toString();
	}
	public static String getRandNum(int length) {
		String str = "";  
        Random rand = new Random();  
        for(int i=0;i<length;i++){  
            int num = rand.nextInt(1);  
            switch(num){  
                case 0:  
                    str += rand.nextInt(10);//生成随机数字  
            }  
        }  
        return str;
	}
	public static String randomBytransfer(){
		StringBuffer num = new StringBuffer();
		num.append(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
		int randNum = 1 + (int)(Math.random() * ((999999 - 1) + 1));
		return num.append(randNum).toString();
		
	}
	public static String randomByUsername(int x){
		StringBuffer num = new StringBuffer(); 
		Random rm = new Random(); 
		double pross = (1 + rm.nextDouble()) * Math.pow(10, 6);
		String str = String.valueOf(pross);
		num.append(str.split("\\.")[1].substring(0, x));
		if(num.toString().startsWith("0")){
			num = new StringBuffer(randomByUsername(x));
		}
		return num.toString();
	}
	//生成5位推荐码
	public static String randomByReferralCode(){
		String str = "";  
        Random rand = new Random();  
        for(int i=0;i<5;i++){  
            int num = rand.nextInt(2);  
            switch(num){  
                case 0:  
                    char c1 = (char)(rand.nextInt(26)+'a');//生成随机小写字母   
                    str += c1;  
                    break;  
                case 1:  
                    str += rand.nextInt(10);//生成随机数字  
            }  
        }  
        return str;
	}
	//生成用户名
	public static String getRandNums() {
		String str = "";  
        Random rand = new Random();  
        int X = rand.nextInt(4);  
        int length = 0;
        switch(X){  
            case 0:  
            	length = 6; 
                break;  
            case 1:  
            	length = 7;
            	break; 
            case 2:  
            	length = 8;
           	 	break; 
            case 3:  
            	length = 9;
           	 
        }  
        for(int i=0;i<length;i++){  
            int num = rand.nextInt(1);  
            switch(num){  
                case 0:  
                    str += rand.nextInt(10);//生成随机数字  
            }  
        }
        if(str.startsWith("0")){
        	str = getRandNums();
        }
        return str;
	}
	public static void main(String[] args) {
		while(true){
			System.out.println(getRandNums());
		}
	}
}
