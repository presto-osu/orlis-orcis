package Entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tiki on 06/06/2016.
 */
public class Entity_RoomTest {
    Entity_Room room = new Entity_Room(null, null, null, 0, null, 0, null);

    @Test
    public void testSetGetArea_id() throws Exception {
        Integer areaid = room.getArea_id();
        Assert.assertEquals(0, areaid, 0);

        room.setArea_id(125);
        areaid = room.getArea_id();
        Assert.assertEquals(125, areaid, 0);
    }

    @Test
    public void testSetGetDescription() throws Exception {
        String description = room.getDescription();
        Assert.assertEquals(null, description);

        room.setDescription("Description");
        description = room.getDescription();
        Assert.assertEquals("Description", description);
    }

    @Test
    public void testSetGetId() throws Exception {
        Integer id = room.getId();
        Assert.assertEquals(0, id, 0);

        room.setId(125);
        id = room.getId();
        Assert.assertEquals(125, id, 0);
    }

    @Test
    public void testSetGetName() throws Exception {
        String name = room.getName();
        Assert.assertEquals(null, name);

        room.setName("Name");
        name = room.getName();
        Assert.assertEquals("Name", name);
    }

    @Test
    public void testSetGetIcon_name() throws Exception {
        //// TODO: 06/06/2016
    }
}