package com.wiiy.im;

import java.io.File;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountCenterPlugin implements Plugin {
	private static final Logger log = LoggerFactory.getLogger(AccountCenterPlugin.class);
	private XMPPServer server;
	public AccountCenterPlugin(){}

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		server = XMPPServer.getInstance();  
        System.out.println("AccountCenterPlugin start");
        log.warn("AccountCenterPlugin is running!");
	}

	@Override
	public void destroyPlugin() {
		System.out.println("AccountCenterPlugin destroy");  
	}

}
