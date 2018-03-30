package org.jivesoftware.openfire.plugin.rest.controller;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.DefaultConnectionProvider;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author CJ
 */
public class MUCRoomControllerTest {

    @BeforeClass
    public static void ready() {
        DefaultConnectionProvider provider = new DefaultConnectionProvider();
        provider.setDriver("com.mysql.jdbc.Driver");
        provider.setServerURL("jdbc:mysql://www.deve.xiaopao69.com:3406/red?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&tinyInt1isBit=false");
        provider.setUsername("red");
        provider.setPassword("red");
        DbConnectionManager.setConnectionProvider(provider);
    }

    @Test
    public void go() {
        List<Map<String, Object>> x = MUCRoomController.getInstance().findRoomUsers("1092759998_1519982515460");
        System.out.println(x);
        Assert.assertTrue("test00是群主", (x
                .stream()
                .filter(map -> "test00@im.deve.xiaopao69.com".equals(map.get("jid")))
                .map(map -> (Boolean) map.get("owner"))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("test 00没有加入群"))
        ));

    }

}