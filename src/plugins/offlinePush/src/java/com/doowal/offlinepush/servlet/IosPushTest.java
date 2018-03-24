package com.doowal.offlinepush.servlet;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Message.IOSBuilder;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;

public class IosPushTest {

	public static void main(String[] args) throws IOException, ParseException, Exception {
		// TODO Auto-generated method stub
		/*Sender sender = new Sender("grOzDe0v+tUUxzidSXXe7Q==");  
        Result result = sender.sendToAlias(buildMessage(), "123", 3); 
        System.out.println(result);*/
		Constants.useOfficial();
		//Constants.useSandbox();
        Sender sender = new Sender("grOzDe0v+tUUxzidSXXe7Q==");
       /* String messagePayload = "This is a message";
        String title = "notification title";
        String description = "notification description";
        String alias = "testAlias";    //alias非空白, 不能包含逗号, 长度小于128
        Message message = new Message.Builder()
                    .title(title)
                    .description(description).payload(messagePayload)
                    .restrictedPackageName("com.rainbow.im.test")
                    .notifyType(1)     // 使用默认提示音提示
                    .build();*/
        Result result = sender.sendToAlias(buildMessage(), "111222", 3); //根据alias, 发送消息到指定设备上
        System.out.println(result);
	}
	private static Message buildMessage() throws Exception {
	    String description = "notification description";
	    Message message = new Message.IOSBuilder()
	            .description(description)
	            .soundURL("default")    // 消息铃声
	            .badge(1)               // 数字角标
	            .category("action")     // 快速回复类别
	            .extra("key", "value")  // 自定义键值对
	            .build();
	    return message;
	}
	
}
