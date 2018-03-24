package com.doowal.offlinepush.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import org.jivesoftware.openfire.plugin.rest.entity.MUCMemberEntities;
import org.jivesoftware.openfire.plugin.rest.entity.MUCMemberEntity;
import org.jivesoftware.openfire.roster.RosterItem;
import org.jivesoftware.openfire.roster.RosterManager;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.openfire.vcard.VCardManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.json.JSONException;
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

import javapns.communication.exceptions.KeystoreException;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.transmission.NotificationProgressListener;
import javapns.notification.transmission.NotificationThread;
import javapns.notification.transmission.NotificationThreads;

public class OfflinePushPlugin implements Component, Plugin, PropertyEventListener, PacketInterceptor {

	private static final Logger Log = LoggerFactory.getLogger(OfflinePushPlugin.class);

	public static final String NAMESPACE_JABBER_IQ_TOKEN_BIND = "jabber:iq:token:bind";
	public static final String NAMESPACE_JABBER_IQ_TOKEN_UNBUND = "jabber:iq:token:unbund";

	public static final String SERVICENAME = "plugin.offlinepush.serviceName";
	public static final String SERVICEENABLED = "plugin.offlinepush.serviceEnabled";

	private ComponentManager componentManager;
	private PluginManager pluginManager;
	private String serviceName;
	private boolean serviceEnabled;
	// 证书安装的目录
	private static String dcpath = System.getProperty("openfireHome") + "/conf/";
	private String dcName;
	private String dcPassword;
	private boolean enabled;

	private static Map<String, String> map = new ConcurrentHashMap<String, String>(20);
	private static Map<String, Integer> count = new ConcurrentHashMap<String, Integer>(20);

	private static AppleNotificationServer appleServer = null;
	private static List<PayloadPerDevice> list;

	public String getDcName() {
		return dcName;
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

		try {
			appleServer = new AppleNotificationServerBasicImpl(dcpath, dcPassword, enabled);
			if (list == null) {
				list = new ArrayList<PayloadPerDevice>();
			}

		} catch (KeystoreException e1) {
			Log.error("KeystoreException: " + e1.getMessage());
		}

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
				map.put(username, tokenElement);
				count.put(username, 0);
				Log.info("set token,username:" + username + " ,token:" + tokenElement);
			} else {
				map.remove(username);
				count.remove(username);
				Log.info("remove token,username:" + username);
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

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
		if (processed && incoming) {
			if (packet instanceof Message) {
				if (((Message) packet).getBody() == null) {
					return;
				}
				if("groupchat".equals(((Message) packet).getType().name())){
					pushOfflineMsgByMuc(packet);
					return;
				}
				JID jid = packet.getTo();
				// 获取用户的设备标志id
				String uuid = map.get(jid.getNode());
				if (uuid != null && !"".equals(uuid)) {
					User user = null;
					try {
						user = XMPPServer.getInstance().getUserManager().getUser(jid.getNode());
					} catch (UserNotFoundException e2) {
						e2.printStackTrace();
					}
					PresenceManager presenceManager = XMPPServer.getInstance().getPresenceManager();
					org.xmpp.packet.Presence presence = presenceManager.getPresence(user);
					if (presence == null) {
						Element root = packet.getElement();
						Element msgtype = root.element("msgtype");
						String type = msgtype.element("type").getText().trim();
						JID fromJid = packet.getFrom();
						Element vacard = VCardManager.getInstance().getVCard(fromJid.getNode());
						java.util.Iterator<RosterItem> iter = RosterManager.getRosterItemProvider().getItems(jid.getNode());
						String beiZhu = null;
						while(iter.hasNext()){
							RosterItem  ri = iter.next();
							if(ri.getJid().getNode().equals(fromJid.getNode())){
								beiZhu = ri.getNickname();
							}
						}
						if(beiZhu==null){
							if(vacard==null){
								beiZhu = fromJid.getNode();
							}else{
								Element nickName = vacard.element("NICKNAME");
								if(nickName!=null){
									beiZhu = nickName.getText();
								}else{
									beiZhu = fromJid.getNode();
								}
							}
						}
						String body = ((Message) packet).getBody();
						String content = null;
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
						pushOfflineMsg(uuid, content, jid);
					}
				}
			}
		}
	}
	private void pushOfflineMsgByMuc(Packet packet){
		MUCMemberEntities members = MUCRoomController.getInstance().findMUCRoomUsers(packet.getFrom().getNode());
		for(MUCMemberEntity mm:members.getMucMembers()){
			// 获取用户的设备标志id
			JID jid = new JID(mm.getJid());
			String uuid = map.get(jid.getNode());
			if (uuid != null && !"".equals(uuid)) {
				User user = null;
				try {
					user = XMPPServer.getInstance().getUserManager().getUser(jid.getNode());
				} catch (UserNotFoundException e2) {
					e2.printStackTrace();
				}
				PresenceManager presenceManager = XMPPServer.getInstance().getPresenceManager();
				org.xmpp.packet.Presence presence = presenceManager.getPresence(user);
				if (presence == null) {
					Element root = packet.getElement();
					Element msgtype = root.element("msgtype");
					String type = msgtype.element("type").getText().trim();
					JID fromJid = packet.getFrom();
					String fromusername = fromJid.getResource();
					Element vacard = VCardManager.getInstance().getVCard(fromusername);
					String roomRemark = MUCRoomController.getInstance().findRoomRemark(fromusername+"@"+packet.getTo().getDomain(),fromJid.getNode());
					String beiZhu = roomRemark;
					if(beiZhu==null||beiZhu.isEmpty()){
						if(vacard==null){
							beiZhu = fromusername;
						}else{
							Element nickName = vacard.element("NICKNAME");
							if(nickName!=null){
								beiZhu = nickName.getText();
							}else{
								beiZhu = fromusername;
							}
						}
					}
					String body = ((Message) packet).getBody();
					String content = null;
					if("cmd".equals(type)){
						return;
					}
					if("txt".equals(type)){
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
	private void pushOfflineMsg(String token, String pushCont, JID jid) {
		NotificationThreads work = null;
		try {
			Integer size = count.get(jid.getNode()) + 1;
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
			work.setListener(DEBUGGING_PROGRESS_LISTENER);
			work.start();
		} catch (JSONException e) {
			Log.error("JSONException:" + e.getMessage());
		} catch (InvalidDeviceTokenFormatException e) {
			Log.error("InvalidDeviceTokenFormatException:" + e.getMessage());
		} finally {
			Log.info("push to apple: username: " + jid.getNode() + " ,context" + pushCont);
		}
	}
/*	*//** 
     * send offline msg from this function 
     *//*  
    private void doAction(Packet packet, boolean incoming, boolean processed,  
            Session session) {  
        Packet copyPacket = packet.createCopy();  
        if (packet instanceof Message) {  
            Message message = (Message) copyPacket;  
            if (message.getType() == Message.Type.chat) {  
                if (processed || !incoming) {  
                    return;  
                }  
                Message sendmessage = (Message) packet;  
                String content = sendmessage.getBody();  
                JID recipient = sendmessage.getTo();  
                // get message  
                try {  
                    if (recipient.getNode() == null  
                            || !UserManager.getInstance().isRegisteredUser(  
                                    recipient.getNode())) {  
                        // Sender is requesting presence information of an  
                        // anonymous user  
                        throw new UserNotFoundException("Username is null");  
                    }  
                    org.xmpp.packet.Presence status = presenceManager.getPresence(userManager.getInstance()  
                            .getUser(recipient.getNode()));  
                    if (status != null) {  
                    	Log.info(recipient.getNode() + " online111"  
                                + ",message: " + content);  
                    } else {  
                    	Log.info(recipient.getNode() + " offline111"  
                                + ",message: " + content);  
                         
                         * add your code here to send offline msg 
                         * recipient.getNode() : receive's id,for example,if 
                         * receive's jid is "23@localhost", receive's id is "23" 
                         * content: message content 
                           
                    }// end if  
                } catch (UserNotFoundException e) {  
                	Log.info("exceptoin " + recipient.getNode() + " not find"  
                            + ",full jid: " + recipient.toFullJID());  
                }  
            } else if (message.getType() == Message.Type.groupchat) {  
                List<?> els = message.getElement().elements("x");  
                if (els != null && !els.isEmpty()) {  
                } else {  
                }  
            } else {  
            }  
        } else if (packet instanceof IQ) {  
            IQ iq = (IQ) copyPacket;  
            if (iq.getType() == IQ.Type.set && iq.getChildElement() != null  
                    && "session".equals(iq.getChildElement().getName())) {  
            }  
        } else if (packet instanceof Presence) {  
            Presence presence = (Presence) copyPacket;  
            if (presence.getType() == Presence.Type.unavailable) {  
            }  
        }  
    }  */
	public Runnable createTask(final String token, final String msgType, final JID jid) {
		return new Runnable() {
			@Override
			public void run() {
				PushNotificationPayload payload = new PushNotificationPayload();
				try {
					String pushCont = LocaleUtils.getLocalizedString(msgType, "offlinepush");
					List<PayloadPerDevice> list = new ArrayList<PayloadPerDevice>();
					payload.addAlert(pushCont);
					payload.addSound("default");
					payload.addBadge(1);
					payload.addCustomDictionary("jid", jid.toString());
					PayloadPerDevice pay = new PayloadPerDevice(payload, token);
					list.add(pay);
					NotificationThreads work = new NotificationThreads(appleServer, list, 1);
					work.setListener(DEBUGGING_PROGRESS_LISTENER);
					work.start();
				} catch (JSONException e) {
					Log.error("JSONException:" + e.getMessage());
				} catch (InvalidDeviceTokenFormatException e) {
					Log.error("InvalidDeviceTokenFormatException:" + e.getMessage());
				}
			}
		};

	}

	public static final NotificationProgressListener DEBUGGING_PROGRESS_LISTENER = new NotificationProgressListener() {
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
	};
}