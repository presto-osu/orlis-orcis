package Entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tiki on 08/06/2016.
 */
public class Entity_IconTest {
    Entity_Icon icon = new Entity_Icon(null, null, 0);

    @Test
    public void testSetGetReference() throws Exception {
        int reference = icon.getReference();
        Assert.assertEquals(0, reference);

        icon.setReference(125);
        reference=icon.getReference();
        Assert.assertEquals(125, reference);

    }

    @Test
    public void testSetGetName() throws Exception {
        String name = icon.getName();
        Assert.assertEquals(null, name);

        icon.setName("Name");
        name = icon.getName();
        Assert.assertEquals("Name", name);
    }

    @Test
    public void testSetGetValue() throws Exception {
        String value = icon.getValue();
        Assert.assertEquals(null, value);

        icon.setValue("Value");
        value = icon.getValue();
        Assert.assertEquals("Value", value);
    }
}