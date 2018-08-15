package Entity;

import org.junit.Assert;
import org.junit.Test;

public class Entity_FeatureTest {
    Entity_Feature feature = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);

    @Test
    public void testSetGetId() throws Exception {
        Integer Id = feature.getId();
        Assert.assertEquals(0, Id, 0);

        feature.setId(125);
        Id = feature.getId();
        Assert.assertEquals(125, Id, 0);
    }

    /*Not used
        @Test
        public void testSetGetDevice() throws Exception {
            Entity_Feature client = new Entity_Feature(null, null, null, null, 0, 0, null, null, null, null, null, null, null, null);
            client.setDevice(new JSONObject(""));
            JSONObject device = client.getDevice();
            //// TODO: 06/06/2016
            Assert.assertEquals(null, device);
        }
    */
    @Test
    public void testSetGetNormalDescription() throws Exception {
        String description = feature.getDescription();
        Assert.assertEquals(null, description);

        feature.setName("Name");
        description = feature.getDescription();
        Assert.assertEquals("Name", description);

        feature.setDescription("Description");
        description = feature.getDescription();
        Assert.assertEquals("Description", description);

        feature.setName("Name");
        feature.setDescription("");
        description = feature.getDescription();
        Assert.assertEquals("Name", description);

        feature.setDescription("Description");
        feature.setId(125);
        feature.Develop = true;
        description = feature.getDescription();
        Assert.assertEquals("Description (125)", description);

        feature.setName("Name");
        feature.setDescription("");
        feature.setId(125);
        feature.Develop = true;
        description = feature.getDescription();
        Assert.assertEquals("Name (125)", description);
    }


    @Test
    public void testSetGetDevice_usage_id() throws Exception {
        String device_usage_id = feature.getDevice_usage_id();
        Assert.assertEquals(null, device_usage_id);

        feature.setDevice_usage_id("Device_usage_id");
        device_usage_id = feature.getDevice_usage_id();
        Assert.assertEquals("Device_usage_id", device_usage_id);
    }

    @Test
    public void testGetAddress() throws Exception {
        String address = feature.getAddress();
        Assert.assertEquals(null, address);

        feature.setAddress("Address");
        address = feature.getAddress();
        Assert.assertEquals("Address", address);
    }

    @Test
    public void testSetGetDevId() throws Exception {
        Integer DevId = feature.getDevId();
        Assert.assertEquals(0, DevId, 0);

        feature.setDevId(125);
        DevId = feature.getDevId();
        Assert.assertEquals(125, DevId, 0);
    }

    @Test
    public void testSetGetName() throws Exception {
        String name = feature.getName();
        Assert.assertEquals(null, name);

        feature.setName("Name");
        name = feature.getName();
        Assert.assertEquals("Name", name);
    }

    @Test
    public void testGetDevice_feature_model_id() throws Exception {
        String device_feature_model_id = feature.getDevice_feature_model_id();
        Assert.assertEquals(null, device_feature_model_id);

        feature.setDevice_feature_model_id("Device_feature_model_id");
        device_feature_model_id = feature.getDevice_feature_model_id();
        Assert.assertEquals("Device_feature_model_id", device_feature_model_id);
    }

    @Test
    public void testGetState_key() throws Exception {
        String state_key = feature.getState_key();
        Assert.assertEquals(null, state_key);

        feature.setState_key("State_key");
        state_key = feature.getState_key();
        Assert.assertEquals("State_key", state_key);
    }

    @Test
    public void testGetParameters() throws Exception {
        String parameters = feature.getParameters();
        Assert.assertEquals(null, parameters);

        feature.setParameters("Parameters");
        parameters = feature.getParameters();
        Assert.assertEquals("Parameters", parameters);
    }

    @Test
    public void testGetValue_type() throws Exception {
        String value_type = feature.getValue_type();
        Assert.assertEquals(null, value_type);

        feature.setValue_type("Value_Type");
        value_type = feature.getValue_type();
        Assert.assertEquals("Value_Type", value_type);
    }

    @Test
    public void testGeRessources() throws Exception {
        Integer resources = feature.getRessources();
        Assert.assertEquals(2.130837653E9, resources, 0);

        feature.setDevice_usage_id("door");
        feature.setState(0);
        resources = feature.getRessources();
        Assert.assertEquals(2.130837668E9, resources, 0);

        feature.setState(1);
        resources = feature.getRessources();
        Assert.assertEquals(2.130837669E9, resources, 0);
    }


    @Test
    public void testGetDevice_type() throws Exception {
        String device_type = feature.getDevice_type();
        Assert.assertEquals(null, device_type);

        feature.setDevice_type_id("device_type");
        device_type = feature.getDevice_type();
        Assert.assertEquals("device_type", device_type);

        feature.setDevice_type_id("device_type.devicepart2");
        device_type = feature.getDevice_type();
        Assert.assertEquals("devicepart2", device_type);
    }

    @Test
    public void testGetDevice_type_id() throws Exception {
        String device_type_id = feature.getDevice_type_id();
        Assert.assertEquals(null, device_type_id);

        feature.setDevice_type_id("Device_Type_ID");
        device_type_id = feature.getDevice_type_id();
        Assert.assertEquals("Device_Type_ID", device_type_id);
    }

    @Test
    public void testGetIcon_name() throws Exception {
        //// TODO: 06/06/2016
    }
}