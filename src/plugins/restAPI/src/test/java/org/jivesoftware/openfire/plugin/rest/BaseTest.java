package org.jivesoftware.openfire.plugin.rest;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.DefaultConnectionProvider;
import org.junit.BeforeClass;

/**
 * @author CJ
 */
public abstract class BaseTest {

    @BeforeClass
    public static void ready() {
        DefaultConnectionProvider provider = new DefaultConnectionProvider();
        provider.setDriver("com.mysql.jdbc.Driver");
        provider.setServerURL("jdbc:mysql://www.deve.xiaopao69.com:3406/red?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&tinyInt1isBit=false");
        provider.setUsername("red");
        provider.setPassword("red");
        DbConnectionManager.setConnectionProvider(provider);
    }

}
