package com.doowal.im.plugin;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.doowal.im.plugin.entity.ChatRoomLogs;
import com.doowal.im.plugin.manager.ChatRoomLogsManager;
/*import com.hoo.openfire.chat.logs.entity.ChatLogs;
import com.hoo.openfire.chat.logs.entity.ChatLogs.ChatLogsConstants;
*/
public class ChatLogsPlugin implements PacketInterceptor, Plugin {
 
    private static final Logger log = LoggerFactory.getLogger(ChatLogsPlugin.class);
    public static String jid = null ;
    private ChatRoomLogsManager chatRoomLogsManager =  ChatRoomLogsManager.getInstance();
    public static void setJid(String jidNew){
    	jid  = jidNew;
    }
    private static PluginManager pluginManager;
   // private static DbChatLogsManager logsManager;
    
    public ChatLogsPlugin() {
        interceptorManager = InterceptorManager.getInstance();
       // logsManager = DbChatLogsManager.getInstance();
    }
 
    //Hook for intercpetorn
    private InterceptorManager interceptorManager;
    
    /**
     * <b>function:</b> 拦截消息核心方法，Packet就是拦截消息对象
     * @author lhp
     */
    @Override
    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {
        if (session != null) {
            //debug(packet, incoming, processed, session);
        }
        
        JID recipient = packet.getTo();
        if (recipient != null&&recipient.equals("")) {
            String username = recipient.getNode();
            // 广播消息或是不存在/没注册的用户.
            if (username == null || !UserManager.getInstance().isRegisteredUser(recipient)) {
                return;
            } else if (!XMPPServer.getInstance().getServerInfo().getXMPPDomain().equals(recipient.getDomain())) {
                // 非当前openfire服务器信息
                return;
            } else if ("".equals(recipient.getResource())) {
            }
        }
        this.doAction(packet, incoming, processed, session);
    }
 
    /**
     * <b>function:</b> 执行保存/分析聊天记录动作
     * @author lhp
     * @param packet 数据包
     * @param incoming true表示发送方
     * @param session 当前用户session
     */
    private void doAction(Packet packet, boolean incoming, boolean processed, Session session) {
        Packet copyPacket = packet.createCopy();
        if (packet instanceof Message) {
            Message message = (Message) copyPacket;
            // 群聊天，多人模式
            if (message.getType() ==  Message.Type.groupchat) {
            	
                List<?> els = message.getElement().elements("x");
                if (els != null && !els.isEmpty()) {
                    //log.info("群聊天信息：{}", message.toXML());
                    //debug("群聊天信息：" + message.toXML());
                    debug("群聊天信息1：" + message.getBody());
                    debug("信息发送人1：" + message.getTo().toBareJID());
                    debug("信息发出人1：" + message.getFrom().toBareJID());
                    debug("群聊天xml1：" + message.toXML());
                    debug("群聊天ID1：" + message.getID());
                    ChatRoomLogs crl =  chatRoomLogsManager.findChatRoomLogsByMessageId(message.getID());
                    if(crl==null){
                    	ChatRoomLogs c = new ChatRoomLogs();
                    	c.setMessageId(message.getID());
                    	c.setSender(message.getFrom().toBareJID());
                    	c.setReceiver(message.getTo().toBareJID());
                    	c.setContent(message.getBody());
                    	c.setDetail(message.toXML());
                    	c.setCreateDate(new Date().getTime()+"");
                    	chatRoomLogsManager.save(c);
                    }
                }
            }
            if(jid==null){
            	return ;
            }
            if(packet.getTo().equals(new JID(jid))){
	            if (message.getType() ==  Message.Type.groupchat) {
	            	
	                List<?> els = message.getElement().elements("x");
	                if (els != null && !els.isEmpty()) {
	                    //log.info("群聊天信息：{}", message.toXML());
	                    //debug("群聊天信息：" + message.toXML());
	                    debug("群聊天信息：" + message.getBody());
	                    debug("信息发送人：" + message.getTo().toBareJID());
	                    debug("信息发出人：" + message.getFrom().toBareJID());
	                    debug("群聊天xml：" + message.toXML());
	                } else {
	                    //log.info("群系统信息：{}", message.toXML());
	                    debug("群系统信息：" + message.toXML());
	                }
	                
	            // 其他信息
	            } else {
	                log.info("其他信息：{}", message.toXML());
	                debug("其他信息：" + message.toXML());
	            }
            }
        } else if (packet instanceof IQ) {
            IQ iq = (IQ) copyPacket;
            if (iq.getType() == IQ.Type.set && iq.getChildElement() != null && "session".equals(iq.getChildElement().getName())) {
                log.info("用户登录成功：{}", iq.toXML());
                debug("用户登录成功：" + iq.toXML());
            }
        } else if (packet instanceof Presence) {
            Presence presence = (Presence) copyPacket;
            if (presence.getType() == Presence.Type.unavailable) {
                log.info("用户退出服务器成功：{}", presence.toXML());
                debug("用户退出服务器成功：" + presence.toXML());
            }
        } 
    }
    
    /**
     * <b>function:</b> 创建一个聊天记录实体对象，并设置相关数据
     * @author lhp
     * @param packet 数据包
     * @param incoming 如果为ture就表明是发送者
     * @param session 当前用户session
     * @return 聊天实体
     */
  /*  private ChatLogs get(Packet packet, boolean incoming, Session session) {
        Message message = (Message) packet;
       ChatLogs logs = new ChatLogs();
        
        JID jid = session.getAddress();
        if (incoming) {        // 发送者
            logs.setSender(jid.getNode());
            JID recipient = message.getTo();
            logs.setReceiver(recipient.getNode());
        } 
        logs.setContent(message.getBody());
        logs.setCreateDate(new Timestamp(new Date().getTime()));
        logs.setDetail(message.toXML());
        logs.setLength(logs.getContent().length());
        logs.setState(0);
        logs.setSessionJID(jid.toString());
        // 生成主键id，利用序列生成器
        long messageID = SequenceManager.nextID(ChatLogsConstants.CHAT_LOGS);
        logs.setMessageId(messageID);
        
        return logs;
    }*/
    
    /**
     * <b>function:</b> 调试信息
     * @author lhp
     * @param packet 数据包
     * @param incoming 如果为ture就表明是发送者
     * @param processed 执行
     * @param session 当前用户session
     */
    private void debug(Packet packet, boolean incoming, boolean processed, Session session) {
        String info = "[ packetID: " + packet.getID() + ", to: " + packet.getTo() + ", from: " + packet.getFrom() + ", incoming: " + incoming + ", processed: " + processed + " ]";
        
        long timed = System.currentTimeMillis();
        debug("################### start ###################" + timed);
        debug("id:" + session.getStreamID() + ", address: " + session.getAddress());
        debug("info: " + info);
        debug("xml: " + packet.toXML());
        debug("################### end #####################" + timed);
        
        log.info("id:" + session.getStreamID() + ", address: " + session.getAddress());
        log.info("info: {}", info);
        log.info("plugin Name: " + pluginManager.getName(this) + ", xml: " + packet.toXML());
    }
    
    private void debug(Object message) {
        if (true) {
            System.out.println(message);

        }
    }
    
    @Override
    public void destroyPlugin() {
        interceptorManager.removeInterceptor(this);
        debug("销毁聊天记录插件成功！");
    }
 
    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
         interceptorManager.addInterceptor(this);
         pluginManager = manager;
         debug("chatLogs plugin started");
    }
}