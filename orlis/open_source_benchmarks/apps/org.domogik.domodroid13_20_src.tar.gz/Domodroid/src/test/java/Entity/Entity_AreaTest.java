package Entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tiki on 06/06/2016.
 */
public class Entity_AreaTest {
    Entity_Area area = new Entity_Area(null, null, null, null, 0, null);

    @Test
    public void testSetGetDescription() throws Exception {
        String description = area.getDescription();
        Assert.assertEquals(null, description);

        area.setDescription("Description");
        description = area.getDescription();
        Assert.assertEquals("Description", description);
    }

    @Test
    public void testSetGetId() throws Exception {
        int id = area.getId();
        Assert.assertEquals(0, id, 0);

        area.setId(125);
        id = area.getId();
        Assert.assertEquals(125, id, 0);
    }

    @Test
    public void testSetGetName() throws Exception {
        String name = area.getName();
        Assert.assertEquals(null, name);

        area.setName("Name");
        name = area.getName();
        Assert.assertEquals("Name", name);
    }

    @Test
    public void testGetIcon_name() throws Exception {
        //// TODO: 06/06/2016
    }
}