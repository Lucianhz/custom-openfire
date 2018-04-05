package org.jivesoftware.openfire.plugin.rest.controller;

import org.jivesoftware.openfire.plugin.rest.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author CJ
 */
public class MUCRoomControllerTest extends BaseTest {

    @Test
    public void findRoomUsers() {
        List<Map<String, Object>> x = MUCRoomController.getInstance().findRoomUsers("1092759998_1519982515460");
        System.out.println(x);
        Assert.assertTrue("test00是群主", (x
                .stream()
                .filter(map -> "test00@im.deve.xiaopao69.com".equals(map.get("jid")))
                .map(map -> (Boolean) map.get("owner"))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("test00没有加入群"))
        ));

    }

    @Test
    public void isRoomOwner() {
        Assert.assertTrue("test00是群主"
                , MUCRoomController.getInstance().isRoomOwner("1092759998_1519982515460", "test00"));
        Assert.assertTrue("test00是群主，支持JID"
                , MUCRoomController.getInstance().isRoomOwner("1092759998_1519982515460", "test00@abcdefg"));
        Assert.assertFalse("test00不是群主"
                , MUCRoomController.getInstance().isRoomOwner("1115580_1518294918", "test00"));
    }

}