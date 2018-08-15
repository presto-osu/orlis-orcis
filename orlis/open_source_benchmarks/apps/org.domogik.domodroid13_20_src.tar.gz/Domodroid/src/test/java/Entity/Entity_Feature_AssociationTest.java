package Entity;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
//import org.robolectric.RobolectricGradleTestRunner;
//import org.robolectric.annotation.Config;

/**
 * Created by tiki on 09/06/2016.
 */
//@RunWith(RobolectricGradleTestRunner.class)
//@Config(constants = BuildConfig.class, sdk = 16)

public class Entity_Feature_AssociationTest {

    @Test
    public void testGetPlace_id() throws Exception {
        Entity_Feature_Association feature_association = new Entity_Feature_Association(0, null, 0, 0, null);
        int placeid = feature_association.getPlace_id();
        Assert.assertEquals(0, placeid);

        feature_association.setPlace_id(125);
        placeid = feature_association.getPlace_id();
        Assert.assertEquals(125, placeid);
    }

    @Test
    public void testGetPlace_type() throws Exception {
        Entity_Feature_Association feature_association = new Entity_Feature_Association(0, null, 0, 0, null);
    }

    @Test
    public void testGetDevice_feature_id() throws Exception {
        Entity_Feature_Association feature_association = new Entity_Feature_Association(0, null, 0, 0, null);
        int Device_feature_id = feature_association.getDevice_feature_id();
        Assert.assertEquals(0, Device_feature_id);

        feature_association.setDevice_feature_id(125);
        Device_feature_id = feature_association.getDevice_feature_id();
        Assert.assertEquals(125, Device_feature_id);
    }

    @Test
    public void testGetId() throws Exception {
        Entity_Feature_Association feature_association = new Entity_Feature_Association(0, null, 0, 0, null);
        int id = feature_association.getId();
        Assert.assertEquals(0, id);

        feature_association.setId(125);
        id = feature_association.getId();
        Assert.assertEquals(125, id);
    }

    @Test
    public void testGetDevice_feature() throws Exception {
        Entity_Feature_Association feature_association = new Entity_Feature_Association(0, null, 0, 0, null);
        JSONObject device_feature = feature_association.getDevice_feature();
//todo handle json in test
//        Assert.assertEquals(null, device_feature);

        feature_association.setDevice_feature(new JSONObject(""));
        device_feature = feature_association.getDevice_feature();
//todo handle json in test
//        Assert.assertEquals("Name", device_feature);
    }

    @Test
    public void testGetFeat_model_id() throws Exception {
        Entity_Feature_Association feature_association = new Entity_Feature_Association(0, null, 0, 0, null);
        String feature_model_id = feature_association.getFeat_model_id();
        Assert.assertEquals(null, feature_model_id);

        JSONObject device_feature_model_id = new JSONObject();
        device_feature_model_id.put("device_feature_model_id", "125");
        feature_association.setDevice_feature(device_feature_model_id);
        feature_model_id = feature_association.getFeat_model_id();
//todo handle json in test
//        Assert.assertEquals("125", feature_model_id);
    }

    @Test
    public void testGetFeat_id() throws Exception {
        Entity_Feature_Association feature_association = new Entity_Feature_Association(0, null, 0, 0, null);
        int feature_id = feature_association.getFeat_id();
        Assert.assertEquals(0, feature_id);

        JSONObject device_feature_model_id = new JSONObject();
        device_feature_model_id.put("id", "125");
        feature_association.setDevice_feature(device_feature_model_id);
        feature_id = feature_association.getFeat_id();
//todo handle json in test
//        Assert.assertEquals("125", feature_id);
    }

    @Test
    public void testGetFeat_device_id() throws Exception {
        Entity_Feature_Association feature_association = new Entity_Feature_Association(0, null, 0, 0, null);
        int feature_device_id = feature_association.getFeat_device_id();
        Assert.assertEquals(0, feature_device_id);

        JSONObject device_feature_model_id = new JSONObject();
        device_feature_model_id.put("device_id", "125");
//todo handle json in test
//        feature_association.setDevice_feature(device_feature_model_id);
        feature_device_id = feature_association.getFeat_device_id();
//        Assert.assertEquals("125", feature_device_id);
    }
}