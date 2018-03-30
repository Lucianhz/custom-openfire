package com.doowal.offlinepush.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushNotificationManager;

import org.dom4j.Element;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.disco.IQDiscoInfoHandler;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntity;
import org.jivesoftware.openfire.plugin.rest.manager.UserManager;
import org.jivesoftware.openfire.roster.RosterItem;
import org.jivesoftware.openfire.roster.RosterItemProvider;
import org.jivesoftware.openfire.roster.RosterManager;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.PacketError.Condition;
import org.xmpp.resultsetmanagement.ResultSet;

import com.doowal.offlinepush.entity.MsgPush;
import com.doowal.offlinepush.entity.MsgPushToken;
import com.doowal.offlinepush.manager.MsgPushManager;
import com.doowal.offlinepush.utils.IOSPushMsg;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Sender;

public class OfflinePushPlugin implements Component, Plugin, PropertyEventListener, PacketInterceptor {
	private static final OfflinePushPlugin OFF_LINE_PUSH_PLUGIN = new OfflinePushPlugin();
	public static OfflinePushPlugin getInstance() {
        return OFF_LINE_PUSH_PLUGIN;
    }
	private static final Logger Log = LoggerFactory.getLogger(OfflinePushPlugin.class);

	public static final String NAMESPACE_JABBER_IQ_TOKEN_BIND = "jabber:iq:token:bind";
	public static final String NAMESPACE_JABBER_IQ_TOKEN_UNBUND = "jabber:iq:token:unbund";

	public static final String SERVICENAME = "plugin.offlinepush.serviceName";
	public static final String SERVICEENABLED = "plugin.offlinepush.serviceEnabled";
	
	//小米推送
	private static String  packageName = JiveGlobals.getProperty("plugin.offlinepush.xiaomi.packagename");;
	private static String appSecretKey = JiveGlobals.getProperty("plugin.offlinepush.xiaomi.app_secret_key");
	
	
	private MsgPushManager msgPushManager =  MsgPushManager.getInstance();
	private UserManager userManager = UserManager.getInstance();
	private MUCRoomController roomController = MUCRoomController.getInstance();
	private ComponentManager componentManager;
	private PluginManager pluginManager;
	private String serviceName;
	private boolean serviceEnabled;
	

	// 证书安装的目录
	private static String dcpath = System.getProperty("openfireHome") + "/conf/";
	private String dcName;
	private String dcPassword;
	private boolean enabled;

	private static Cache<String, Integer> count = null;
	private static Map<String, String> map = new ConcurrentHashMap<String, String>(20);
	private static List<String> ids = new ArrayList<String>();
	private static AppleNotificationServer appleServer = null;
	//private static List<PayloadPerDevice> list;
	public String getDcName() {
		return dcName;
	}
	private static PushNotificationManager pushManager = null;
	private static AppleNotificationServerBasicImpl ans = null;
	static{
		/*pushManager = new PushNotificationManager();
		try {
			ans = new AppleNotificationServerBasicImpl(
					dcpath+JiveGlobals.getProperty("plugin.offlinepush.dcName"),
					JiveGlobals.getProperty("plugin.offlinepush.password"), 
					JiveGlobals.getBooleanProperty("plugin.offlinepush.enabled"));
		} catch (KeystoreException e) {
			e.printStackTrace();
			Log.error("aps faile: "+e.getMessage());
		}*/
		Cache[] caches = CacheFactory.getAllCaches();
	    Cache[] arrayOfCache1 = caches;
	    int j = caches.length;
	    for (int i = 0; i < j; i++)
	    {
	      Cache c = arrayOfCache1[i];
	      if ("msgCount".equals(c.getName())) {
	        count = c;
	      }
	    }
	    if (count == null) {
	      count = CacheFactory.createCache("msgCount");
	      count.setMaxLifetime(100000000);
	      count.setMaxCacheSize(500);
	    }
	}
	public void setDcName(String dcName) {
		JiveGlobals.setProperty("plugin.offlinepush.dcName", dcName);
		this.dcName = dcName;
	}

	public String getDcPassword() {
		return dcPassword;
	}

	public void setDcPassword(String dcPassword) {
		JiveGlobals.setProperty("plugin.offlinepush.password", dcPassword);
		this.dcPassword = dcPassword;
	}

	public boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		JiveGlobals.setProperty("plugin.offlinepush.enabled", enabled ? "true" : "false");
	}
	
	public OfflinePushPlugin() {
		serviceName = JiveGlobals.getProperty(SERVICENAME, "offlinepush");
		serviceEnabled = JiveGlobals.getBooleanProperty(SERVICEENABLED, true);
	}

	@Override
	public void xmlPropertySet(String property, Map<String, Object> params) {

	}

	@Override
	public void xmlPropertyDeleted(String property, Map<String, Object> params) {
	}

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		System.out.println(" offlinePush plugin started");
		dcName = JiveGlobals.getProperty("plugin.offlinepush.dcName", "");
		// If no secret key has been assigned to the user service yet, assign a
		// random one.
		if (dcName.equals("")) {
			dcName = "delementtest.p12";
			setDcName(dcName);
		}
		Log.info("dcpath: " + dcpath);
		Log.info("dcName: " + dcName);
		dcpath += dcName;
		dcPassword = JiveGlobals.getProperty("plugin.offlinepush.password", "");
		if (dcPassword.equals("")) {
			dcPassword = "doowalF302f302";
			setDcPassword(dcPassword);
		}

		enabled = JiveGlobals.getBooleanProperty("plugin.offlinepush.enabled");
		setEnabled(enabled);

		Log.info("dcpath: " + dcpath);
		Log.info("dcPassword: " + dcPassword);
		Log.info("enabled: " + enabled);

	/*	try {
			appleServer = new AppleNotificationServerBasicImpl(dcpath, dcPassword, enabled);
			if (list == null) {
				list = new ArrayList<PayloadPerDevice>();
			}

		} catch (KeystoreException e1) {
			Log.error("KeystoreException: " + e1.getMessage());
		}
*/
		pluginManager = manager;

		componentManager = ComponentManagerFactory.getComponentManager();
		try {
			componentManager.addComponent(serviceName, this);
		} catch (ComponentException e) {
			Log.error(e.getMessage(), e);
		}

		InterceptorManager.getInstance().addInterceptor(this);
		PropertyEventDispatcher.addListener(this);

	}

	@Override
	public void destroyPlugin() {
		InterceptorManager.getInstance().removeInterceptor(this);
		PropertyEventDispatcher.removeListener(this);
		pluginManager = null;
		try {
			componentManager.removeComponent(serviceName);
			componentManager = null;
		} catch (Exception e) {
			if (componentManager != null) {
				Log.error(e.getMessage(), e);
			}
		}
		serviceName = null;
	}

	@Override
	public String getName() {
		return pluginManager.getName(this);
	}

	@Override
	public String getDescription() {
		return pluginManager.getDescription(this);
	}

	@Override
	public void processPacket(Packet p) {
		if (!(p instanceof IQ)) {
			return;
		}
		final IQ packet = (IQ) p;

		if (packet.getType().equals(IQ.Type.error) || packet.getType().equals(IQ.Type.result)) {
			return;
		}
		final IQ replyPacket = handleIQRequest(packet);

		try {
			componentManager.sendPacket(this, replyPacket);
		} catch (ComponentException e) {
			Log.error(e.getMessage(), e);
		}
	}

	private IQ handleIQRequest(IQ iq) {
		final IQ replyPacket; // 'final' to ensure that it is set.

		if (iq == null) {
			throw new IllegalArgumentException("Argument 'iq' cannot be null.");
		}

		final IQ.Type type = iq.getType();
		if (type != IQ.Type.get && type != IQ.Type.set) {
			throw new IllegalArgumentException("Argument 'iq' must be of type 'get' or 'set'");
		}

		final Element childElement = iq.getChildElement();
		if (childElement == null) {
			replyPacket = IQ.createResultIQ(iq);
			replyPacket.setError(new PacketError(Condition.bad_request, org.xmpp.packet.PacketError.Type.modify,
					"IQ stanzas of type 'get' and 'set' MUST contain one and only one child element (RFC 3920 section 9.2.3)."));
			return replyPacket;
		}

		final String namespace = childElement.getNamespaceURI();
		if (namespace == null) {
			replyPacket = IQ.createResultIQ(iq);
			replyPacket.setError(Condition.feature_not_implemented);
			return replyPacket;
		}

		if (namespace.equals(NAMESPACE_JABBER_IQ_TOKEN_BIND)) {
			replyPacket = processSetUUID(iq, true);
		} else if (namespace.equals(NAMESPACE_JABBER_IQ_TOKEN_UNBUND)) {
			replyPacket = processSetUUID(iq, false);
		} else if (namespace.equals(IQDiscoInfoHandler.NAMESPACE_DISCO_INFO)) {
			replyPacket = handleDiscoInfo(iq);
		} else {
			// don't known what to do with this.
			replyPacket = IQ.createResultIQ(iq);
			replyPacket.setError(Condition.feature_not_implemented);
		}

		return replyPacket;
	}

	private static IQ handleDiscoInfo(IQ iq) {
		if (iq == null) {
			throw new IllegalArgumentException("Argument 'iq' cannot be null.");
		}

		if (!iq.getChildElement().getNamespaceURI().equals(IQDiscoInfoHandler.NAMESPACE_DISCO_INFO)
				|| iq.getType() != Type.get) {
			throw new IllegalArgumentException("This is not a valid disco#info request.");
		}

		final IQ replyPacket = IQ.createResultIQ(iq);

		final Element responseElement = replyPacket.setChildElement("query", IQDiscoInfoHandler.NAMESPACE_DISCO_INFO);
		responseElement.addElement("identity").addAttribute("category", "directory").addAttribute("type", "user")
				.addAttribute("name", "Offline Push");
		responseElement.addElement("feature").addAttribute("var", NAMESPACE_JABBER_IQ_TOKEN_BIND);
		responseElement.addElement("feature").addAttribute("var", IQDiscoInfoHandler.NAMESPACE_DISCO_INFO);
		responseElement.addElement("feature").addAttribute("var", ResultSet.NAMESPACE_RESULT_SET_MANAGEMENT);

		return replyPacket;
	}

	private IQ processSetUUID(IQ packet, boolean isSet) {
		Element rsmElement = null;
		if (!packet.getType().equals(IQ.Type.set)) {
			throw new IllegalArgumentException("This method only accepts 'set' typed IQ stanzas as an argument.");
		}

		final IQ resultIQ;

		final Element incomingForm = packet.getChildElement();
		String uri = incomingForm.getNamespaceURI();
		if (NAMESPACE_JABBER_IQ_TOKEN_BIND.equals(uri)) {
			rsmElement = incomingForm.element("child");
		}
		resultIQ = IQ.createResultIQ(packet);
		if (rsmElement != null) {
			String osElement = rsmElement.attributeValue("os");
			String jidElement = rsmElement.attributeValue("jid");

			String username = new JID(jidElement).getNode();

			if (osElement == null || jidElement == null) {
				resultIQ.setError(Condition.bad_request);
				return resultIQ;
			}
			if (isSet) {
				String tokenElement = rsmElement.attributeValue("token");
				MsgPushToken mpt = msgPushManager.selectPushToken(username);
				 if (mpt != null)
			        {
			          if((tokenElement != null)&&(!tokenElement.equals(mpt.getToken()))){
			            msgPushManager.updatePushToken(username, tokenElement);
			            Log.info("updatePushToken :"+tokenElement);
			          }
			        }
			        else
			        {
			          msgPushManager.savePushToken(username, tokenElement);
			          Log.info("savePushToken :"+tokenElement);
			        }
				count.put(username, 0);
			} else {
				map.remove(username);
				count.remove(username);
			}
		} else {
			resultIQ.setError(Condition.bad_request);
		}

		return resultIQ;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String name) {
		JiveGlobals.setProperty(SERVICENAME, name);
	}

	public boolean getServiceEnabled() {
		return serviceEnabled;
	}

	public void setServiceEnabled(boolean enabled) {
		serviceEnabled = enabled;
		JiveGlobals.setProperty(SERVICEENABLED, enabled ? "true" : "false");
	}

	public void propertySet(String property, Map<String, Object> params) {
		if (property.equals(SERVICEENABLED)) {
			this.serviceEnabled = Boolean.parseBoolean((String) params.get("value"));
		}
		if (property.equals("plugin.offlinepush.dcName")) {
			this.dcName = (String) params.get("value");
		} else if (property.equals("plugin.offlinepush.enabled")) {
			this.enabled = Boolean.parseBoolean((String) params.get("value"));
		} else if (property.equals("plugin.offlinepush.password")) {
			this.dcPassword = (String) params.get("value");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jivesoftware.util.PropertyEventListener#propertyDeleted(java.lang.
	 * String, java.util.Map)
	 */
	public void propertyDeleted(String property, Map<String, Object> params) {
		if (property.equals(SERVICEENABLED)) {
			this.serviceEnabled = true;
		}
		if (property.equals("plugin.offlinepush.dcName")) {
			this.dcName = "delementtest.p12";
		} else if (property.equals("plugin.offlinepush.enabled")) {
			this.enabled = false;
		} else if (property.equals("plugin.offlinepush.password")) {
			this.dcPassword = "doowalF302f302";
		}
	}

	@Override
	public void initialize(JID jid, ComponentManager componentManager) throws ComponentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}
	
	//推送添加好友通知 
	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
		if(packet instanceof Message){
			Message message = (Message) packet; 
			if(message.getBody()==null||"".equals(message.getBody())){
				return;
			}
		}
		JID recipient = packet.getTo();
		if(ids.contains(packet.getID())){
			return;
		}
        if (recipient != null) {  
            String username = recipient.getNode();  
            // if broadcast message or user is not exist  
            if (username == null  
                    || !UserManager.getInstance().isExists(username)) {  
                return;  
            } else if (!XMPPServer.getInstance().getServerInfo()  
                    .getXMPPDomain().equals(recipient.getDomain())) {  
                // not from the same domain  
                return;  
            } else if ("".equals(recipient.getResource())) {
            	return;
            }  
        }  
		if(packet instanceof IQ){
			if(IQ.Type.set == ((IQ)packet).getType()){
				JID from = packet.getTo();
				if(from!=null){
					Element root = packet.getElement();
					if(root.asXML().contains("subscription")){
						Element item = root.element("query").element("item");
						String toStr = item.attributeValue("jid");
						String sub = item.attributeValue("subscription");
						if("none".equals(sub)){
							JID to = new JID(toStr);
							if(to!=null){
								if(!checkUserOnline(to.getNode())){
									MsgPushToken mpt = msgPushManager.selectPushToken(to.getNode());
									String uuid = mpt != null ? mpt.getToken() : null;
									String fromNickName = userManager.findUserNickName(from.getNode());
									if(fromNickName==null||fromNickName.isEmpty()){
										fromNickName = userManager.findUserNickName(from.getNode());
									}
									if(fromNickName==null||fromNickName.isEmpty()){
										fromNickName = from.getNode();
									}
									if(uuid==null){
										sendMessageToAlias(to.getNode(),fromNickName,"请求加你为好友");
										ids.add(packet.getID());
										return;
									}
									//pushOfflineMsg2(uuid, fromNickName+"请求加你为好友", from);
									IOSPushMsg.pushMsg(uuid, fromNickName+"请求加你为好友", from, count,userManager);
									ids.add(packet.getID());
									return;
								}
							}
							
						}
					}
				}
			}
		}
		doAction(packet,incoming,processed);
	}
	private void doAction(Packet packet, boolean incoming, boolean processed){
		//ios推送离线消息
		if (processed && incoming) {
			if (packet instanceof Message) {
				 Message message = (Message) packet; 
				 if(message.getType()!= Message.Type.chat&&message.getType()!= Message.Type.groupchat){
					 return;
				 }
				//当收到的是群离线消息时
				if(message.getType()== Message.Type.groupchat){
					String roomName = message.getFrom().getNode();
					MUCRoomEntity room = roomController.findRoomDetail(roomName);
					if(!"0".equals(room.getType())){
						return;
					}
					//ios推送
					pushOfflineMsgByMuc(message);
					ids.add(packet.getID());
					try {
						//安卓小米推送
						sendMessageToAliases(packet);
						ids.add(packet.getID());
					} catch (Exception e) {
						e.printStackTrace();
						Log.error("Exception: "+e.getMessage());
					}
					return;
				}
				//当收到的是好友离线消息时
				JID jid = packet.getTo();
				String jidFrom = null;
				if(message.getFrom()!=null){
					jidFrom = message.getFrom().toBareJID();
				}
				String userjid = jid.toBareJID();
				msgPushManager = MsgPushManager.getInstance();
				MsgPush mp = msgPushManager.findMsgPush(userjid, jidFrom, null);
				//判断是否设置免打扰   1为开启
				if(mp!=null&&mp.getMsgpush().equals("1")){
					return;
				}
				// 获取用户的设备标志id
				MsgPushToken mpt = msgPushManager.selectPushToken(jid.getNode());
			    String uuid = mpt != null ? mpt.getToken() : null;
				if (uuid != null && !"".equals(uuid)){
					if (!checkUserOnline(jid.getNode())) {
						Element root = packet.getElement();
						Element msgtype = root.element("msgtype");
						String type = null;
						if(msgtype!=null){
							type = msgtype.element("type").getText().trim();
						}
						JID fromJid = packet.getFrom();
						String fromUsername = fromJid.getNode();
						//获取好友备注
						java.util.Iterator<RosterItem> iter = RosterManager.getRosterItemProvider().getItems(jid.getNode());
						String beiZhu = null;
						while(iter.hasNext()){
							RosterItem  ri = iter.next();
							if(ri.getJid().getNode().equals(fromUsername)){
								beiZhu = ri.getNickname();
							}
						} 
						
						if(beiZhu==null||"".equals(beiZhu)){
							beiZhu =userManager.findUserNickName(fromUsername);
						}
						if(beiZhu==null||"".equals(beiZhu)){
							beiZhu = fromUsername;
						}
						String body = ((Message) packet).getBody();
						String content = null;
						if("alt_r".equals(type)){
							return;
						}else if("txt".equals(type)){
							content = beiZhu+":"+body;
						}else if(type!=null&&type.contains("voc")){
							content = beiZhu+"发来一段语音";
						}else if("img".equals(type)){
							content = beiZhu+"发来一张图片";
						}else{
							content = "你收到一条消息";
						}
						
						//pushOfflineMsg2(uuid, content, jid);
						IOSPushMsg.pushMsg(uuid, content, jid, count,userManager);
						ids.add(packet.getID());
					}
				}else{
					if (!checkUserOnline(jid.getNode())) {
						Element root = packet.getElement();
						Element msgtype = root.element("msgtype");
						String type = null;
						if(msgtype!=null){
							type = msgtype.element("type").getText().trim();
						}
						JID fromJid = packet.getFrom();
						String fromUsername = fromJid.getNode();
						String beiZhu = null;
						RosterItemProvider rp = RosterManager.getRosterItemProvider();
						if(rp!=null){
							java.util.Iterator<RosterItem> iter = rp.getItems(jid.getNode());
							while(iter.hasNext()){
								RosterItem  ri = iter.next();
								if(ri.getJid().getNode().equals(fromUsername)){
									beiZhu = ri.getNickname();
								}
							}
						}
						if(beiZhu==null||"".equals(beiZhu)){
							beiZhu =userManager.findUserNickName(fromUsername);
						}
						if(beiZhu==null||"".equals(beiZhu)){
							beiZhu = fromUsername;
						}
						String body = ((Message) packet).getBody();
						String content = null;
						if("alt_r".equals(type)){
							return;
						}else if("txt".equals(type)){
							content = body;
						}else if(type!=null&&type.contains("voc")){
							content = "发来一段语音";
						}else if("img".equals(type)){
							content = "发来一张图片";
						}else{
							content = "你收到一条消息";
						}
						String status = userManager.findMsgDetail(jid.getNode());
						if("0".equals(status)){
							content = "你收到一条消息";
						}
						sendMessageToAlias(jid.getNode(),beiZhu, content);
						ids.add(packet.getID());
					}
				}
			}
		}
	}
	//ios开始推送群离线消息
	public void pushOfflineMsgByMuc(final Message message) {
				Log.info("开始推送++++++++++");
				String roomName = message.getFrom().getNode();
				List<Map<String, Object>> members = roomController.findRoomUsers(roomName);
				if(members!=null&&members.size()!=0){
					Element root = message.getElement();
					Element msgtype = root.element("msgtype");
					String type = msgtype.element("type").getText().trim();
					//过滤红包推送
					Element redPackSender =  msgtype.element("redPackSender");
					String body =  message.getBody();
					String domain = JiveGlobals.getProperty("xmpp.domain");
					JID fromJid = new JID(message.getFrom().getResource()+"@"+domain);
					String fromusername = fromJid.getNode();
					String roomRemark =roomController.findRoomRemark(fromJid.toBareJID(),roomName);
					String beiZhu = roomRemark;
					if(beiZhu==null||"".equals(beiZhu)){
						beiZhu = userManager.findUserNickName(fromusername);
					}
					if(beiZhu==null||"".equals(beiZhu)){
						beiZhu = fromusername;
					}
					String content = null;
					if("cmd".equals(type)){
						return;
					}
					if("alt_r".equals(type)){
						return;
					}else if("txt".equals(type)){
						content = beiZhu+":"+body;
					}else if(type.contains("voc")){
						content = beiZhu+"发来一段语音";
					}else if("img".equals(type)){
						content = beiZhu+"发来一张图片";
					}else{
						content = "你收到一条消息";
					}
					for(Map mm:members){
						// 获取用户的设备标志id
						JID jid = new JID((String)mm.get("jid"));
						MsgPushToken mpt = msgPushManager.selectPushToken(jid.getNode());
					    String uuid = mpt != null ? mpt.getToken() : null;
						if (uuid != null && !"".equals(uuid)) {
							if (!checkUserOnline(jid.getNode())) {
								String toJid = (String)mm.get("jid");
								MsgPush mp = msgPushManager.findMsgPush(toJid, null, roomName);
								if(mp!=null&&mp.getMsgpush().equals("1")){
									continue;
								}
								if(redPackSender!=null){
									String rps = redPackSender.getText();
									if(!jid.getNode().equals(rps)){
										continue;
									}
								}
								//pushOfflineMsg2(uuid, content, jid);
								IOSPushMsg.pushMsg(uuid, content, jid, count,userManager);
								ids.add(message.getID());
							}
						}
					}
				}
	}
	/*private void pushOfflineMsgByMuc(Message message){
		String roomName = message.getFrom().getNode();
		List<Map> members = MUCRoomController.getInstance().findRoomUsers(roomName);
		if(members!=null&&members.size()!=0){
			for(Map mm:members){
				// 获取用户的设备标志id
				JID jid = new JID((String)mm.get("jid"));
				MsgPushToken mpt = msgPushManager.selectPushToken(jid.getNode());
			    String uuid = mpt != null ? mpt.getToken() : null;
				if (uuid != null && !"".equals(uuid)) {
					if (!checkUserOnline(jid.getNode())) {
						String toJid = (String)mm.get("jid");
						MsgPush mp = msgPushManager.findMsgPush(toJid, null, roomName);
						if(mp!=null&&mp.getMsgpush().equals("1")){
							continue;
						}
						Element root = message.getElement();
						Element msgtype = root.element("msgtype");
						String type = msgtype.element("type").getText().trim();
						//过滤红包推送
						Element redPackSender =  msgtype.element("redPackSender");
						if(redPackSender!=null){
							String rps = redPackSender.getText();
							if(!jid.getNode().equals(rps)){
								continue;
							}
						}
						JID fromJid = new JID(message.getFrom().getResource()+"@"+JiveGlobals.getProperty("xmpp.domain"));
						String fromusername = fromJid.getNode();
						String roomRemark = MUCRoomController.getInstance().findRoomRemark(fromJid.toBareJID(),roomName);
						String beiZhu = roomRemark;
						if(beiZhu==null||"".equals(beiZhu)){
							beiZhu = userManager.findUserNickName(fromusername);
						}
						if(beiZhu==null||"".equals(beiZhu)){
							beiZhu = fromusername;
						}
						String body = ((Message) message).getBody();
						String content = null;
						if("cmd".equals(type)){
							continue;
						}
						if("alt_r".equals(type)){
							continue;
						}else if("txt".equals(type)){
							content = beiZhu+":"+body;
						}else if(type.contains("voc")){
							content = beiZhu+"发来一段语音";
						}else if("img".equals(type)){
							content = beiZhu+"发来一张图片";
						}else{
							content = "你收到一条消息";
						}
						pushOfflineMsg(uuid, content, jid);
						
					}
				}
			}
		}
	}*/
	//ios开始推送个人消息
	/*private void pushOfflineMsg(String token, String pushCont, JID jid) {
		String status = userManager.findMsgDetail(jid.getNode());
		if("0".equals(status)){
			pushCont = "你收到一条消息";
		}
		NotificationThreads work = null;
		try {
			Integer size = 1;
			if(count.containsKey(jid.getNode())){
				size = count.get(jid.getNode()) + 1;
			}
			if (size <= 1000)
				count.put(jid.getNode(), size);
			List<PayloadPerDevice> list = new ArrayList<PayloadPerDevice>();
			PushNotificationPayload payload = new PushNotificationPayload();
			payload.addAlert(pushCont);
			payload.addSound("default");
			payload.addBadge(size);
			payload.addCustomDictionary("jid", jid.toString());
			PayloadPerDevice pay = new PayloadPerDevice(payload, token);
			list.add(pay);
			work = new NotificationThreads(appleServer, list, 1);
			list = null;
			work.start();
			work.waitForAllThreads();
			Log.info("IOS PUSH SUCCESS!~!~!~");
		} catch (JSONException e) {
			Log.error("JSONException:" + e.getMessage());
		} catch (InvalidDeviceTokenFormatException e) {
			Log.error("InvalidDeviceTokenFormatException:" + e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//IOS推送个人消息  （固定线程）
	private void pushOfflineMsg2(String deviceToken, String pushCont, JID jid){
		try{
			List<String> tokens = new ArrayList<String>();
        	tokens.add(deviceToken);
         
        	Integer size = 1;
        	if(count.containsKey(jid.getNode())){
        		size = count.get(jid.getNode()) + 1;
        	}
        	if (size <= 1000)
        		count.put(jid.getNode(), size);
			
	         PushNotificationPayload payload = new PushNotificationPayload();
	         payload.addAlert(pushCont); // 消息内容
	         payload.addBadge(size);//消息个数角标
	         payload.addCustomDictionary("jid", jid.toString());
	         payload.addSound("default");//默认铃音
	
	         // true：表示的是产品测试推送服务 false：表示的是产品发布推送服务
	        
	         // 开始推送消息
	         Device device = new BasicDevice();
	         device.setToken(deviceToken);
	         pushManager.initializeConnection(ans);
	         pushManager.setSslSocketTimeout(10000);
	         pushManager.sendNotification(device, payload, true);
	         Log.info("pushCont:"+pushCont+"   deviceToken:"+deviceToken);
	         Log.info("MSG推送成功！！！！！！！！！！！！！！！！！！！！！");
	         pushManager.stopConnection();
		}catch(Exception e){
			Log.error("ios push failed : "+e.getMessage());
		}
	}
*/
	/*public static final NotificationProgressListener DEBUGGING_PROGRESS_LISTENER = new NotificationProgressListener() {
		public void eventThreadStarted(NotificationThread notificationThread) {
			Log.debug("   [EVENT]: thread #" + notificationThread.getThreadNumber() + " started with "
					+ " devices beginning at message id #" + notificationThread.getFirstMessageIdentifier());
		}

		public void eventThreadFinished(NotificationThread thread) {
			Log.debug("   [EVENT]: thread #" + thread.getThreadNumber() + " finished: pushed messages #"
					+ thread.getFirstMessageIdentifier() + " to " + thread.getLastMessageIdentifier() + " toward "
					+ " devices");
		}

		public void eventConnectionRestarted(NotificationThread thread) {
			Log.debug(
					"   [EVENT]: connection restarted in thread #" + thread.getThreadNumber() + " because it reached "
							+ thread.getMaxNotificationsPerConnection() + " notifications per connection");
		}

		public void eventAllThreadsStarted(NotificationThreads notificationThreads) {
			Log.debug("   [EVENT]: all threads started: " + notificationThreads.getThreads().size());
		}

		public void eventAllThreadsFinished(NotificationThreads notificationThreads) {
			Log.debug("   [EVENT]: all threads finished: " + notificationThreads.getThreads().size());
		}

		public void eventCriticalException(NotificationThread notificationThread, Exception exception) {
			Log.debug("   [EVENT]: critical exception occurred: " + exception);
		}
	};*/
	//小米推送个人消息
	private void sendMessageToAlias(String alias,String title,String description){
		  	Constants.useOfficial();
		    Sender sender = new Sender(appSecretKey);
		    //alias非空白, 不能包含逗号, 长度小于128
		    com.xiaomi.xmpush.server.Message message = 
		    		new com.xiaomi.xmpush.server.Message.Builder()
		                .title(title)
		                .description(description)
		                .restrictedPackageName(packageName)
		                .notifyType(1)     // 使用默认提示音提示
		                .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_LAUNCHER_ACTIVITY) //调起APP
		                .build();
		    try {
		    	//根据alias, 发送消息到指定设备上
				sender.sendToAlias(message, alias, 3);
			} catch (IOException e) {
				e.printStackTrace();
				Log.error("IOException: "+e.getMessage());
			} catch (ParseException e) {
				e.printStackTrace();
				Log.error("ParseException: "+e.getMessage());
			} 
	}
	//小米推送群消息
	//多个alias消息(推荐使用)
	private void sendMessageToAliases(Packet packet) throws Exception {
		List<Map<String, Object>> members = roomController.findRoomUsers(packet.getFrom().getNode());
		String roomName = packet.getFrom().getNode();
		Element root = packet.getElement();
		Element msgtype = root.element("msgtype");
		String type = msgtype.element("type").getText().trim();
		JID fromJid = packet.getFrom();
		String fromusername = fromJid.getResource();
		String roomRemark = roomController.findRoomRemark(fromusername+"@"+packet.getTo().getDomain(),fromJid.getNode());
		String beiZhu = roomRemark;
		if(beiZhu==null||beiZhu.isEmpty()){
			String nickName = userManager.findUserNickName(fromusername);
			if(nickName!=null&&!"".equals(nickName)){ 
				beiZhu = nickName;
			}else{
				beiZhu = fromusername;
			}
		}
		String body = ((Message) packet).getBody();
		String content = null;
		if("cmd".equals(type)){
			return;
		}
		if("alt_r".equals(type)){
			return;
		}else if("txt".equals(type)){
			content = body;
		}else if(type.contains("voc")){
			content = "发来一段语音";
		}else if("img".equals(type)){
			content = "发来一张图片";
		}else{
			content = "你收到一条消息";
		}
		//过滤红包推送
		Element redPackSender =  msgtype.element("redPackSender");
		
		//遍历群成员
		for(Map mm:members){
			// 获取用户的设备标志id
			JID jid = new JID((String)mm.get("jid"));
			MsgPushToken mpt = msgPushManager.selectPushToken(jid.getNode());
		    String uuid = mpt != null ? mpt.getToken() : null;
			//uuid为空的为安卓设备
			if (uuid == null || "".equals(uuid)) {
				String toJid = (String)mm.get("jid");
				MsgPush mp = msgPushManager.findMsgPush(toJid, null, roomName);
				//开启免打扰的不予推送
				if(mp==null||!mp.getMsgpush().equals("1")){
					if (!checkUserOnline(jid.getNode())) {
						//过滤红包推送
						if(redPackSender!=null){
							String rps = redPackSender.getText();
							if(!jid.getNode().equals(rps)){
								continue;
							}
						}
						String status = userManager.findMsgDetail(jid.getNode());
						//判断是否开启消息详情
						if("0".equals(status)){
							content = "你收到一条消息";
						}
						sendMessageToAlias(jid.getNode(),beiZhu,content);
					}
				}
			}
		}
	}
	//检测用户是否在线
	public boolean checkUserOnline(String username){
		boolean b = userManager.isExists(username);
		if(!b){
			return false; 
		}
		User user = null;
		try {
			user = XMPPServer.getInstance().getUserManager().getUser(username);
		} catch (UserNotFoundException e2) {
			e2.printStackTrace();
			Log.error("UserNotFoundException: "+e2.getMessage());
		}
		org.xmpp.packet.Presence presence = null;
		if(user!=null){
			PresenceManager presenceManager = XMPPServer.getInstance().getPresenceManager();
			presence = presenceManager.getPresence(user);
		}
		if(presence==null){
			return false;
		}
		return true;
	}
}