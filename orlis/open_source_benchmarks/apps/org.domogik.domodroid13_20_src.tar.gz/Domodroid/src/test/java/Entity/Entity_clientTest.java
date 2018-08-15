package Entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tiki on 09/06/2016.
 */
public class Entity_clientTest {
    Entity_client client = new Entity_client(0, null, null, null, 0);

    @Test
    public void testGetClientType() throws Exception {
        int type = client.getClientType();
        Assert.assertEquals(0, type);

        client.setClientType(125);
        type = client.getClientType();
        Assert.assertEquals(125, type);
    }

    @Test
    public void testGetClientId() throws Exception {
        int id = client.getClientId();
        Assert.assertEquals(-1, id);

        client.setClientId(125);
        id = client.getClientId();
        Assert.assertEquals(125, id);
    }

    @Test
    public void testGetcacheId() throws Exception {
        int cacheid = client.getcacheId();
        Assert.assertEquals(0, cacheid);

        client.setcacheId(125);
        cacheid = client.getcacheId();
        Assert.assertEquals(125, cacheid);
    }

    @Test
    public void testGetDevId() throws Exception {
        int DevId = client.getDevId();
        Assert.assertEquals(0, DevId);

        client.setDevId(125);
        DevId = client.getDevId();
        Assert.assertEquals(125, DevId);

    }

    @Test
    public void testGetskey() throws Exception {
        String skey = client.getskey();
        Assert.assertEquals(null, skey);

        client.setskey("Skey");
        skey = client.getskey();
        Assert.assertEquals("Skey", skey);
    }

    @Test
    public void testGetValue() throws Exception {
        String value = client.getValue();
        Assert.assertEquals(null, value);

        client.setValue("Value");
        value = client.getValue();
        Assert.assertEquals("Value", value);
    }

    @Test
    public void testGetName() throws Exception {
        String name = client.getName();
        Assert.assertEquals(null, name);

        client.setName("Name");
        name = client.getName();
        Assert.assertEquals("Name", name);
    }

    @Test
    public void testGetTimestamp() throws Exception {
        String timestamp = client.getTimestamp();
        Assert.assertEquals(null, timestamp);

        client.setTimestamp("Timestamp");
        timestamp = client.getTimestamp();
        Assert.assertEquals("Timestamp", timestamp);
    }

    @Test
    public void testIs_Miniwidget() throws Exception {
        Boolean type = client.is_Miniwidget();
        Assert.assertEquals(false, type);

        client.setType(true);
        type = client.is_Miniwidget();
        Assert.assertEquals(true, type);
    }

    @Test
    public void testGetClientHandler() throws Exception {
        //// TODO: 09/06/2016 handler in test
    }
}