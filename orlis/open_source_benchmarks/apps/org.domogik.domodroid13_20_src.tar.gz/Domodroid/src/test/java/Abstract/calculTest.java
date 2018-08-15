package Abstract;

import org.junit.Assert;

/**
 * Created by tiki on 06/06/2016.
 */
public class calculTest {

    @org.junit.Test
    public void testRound_double() throws Exception {
        final double testRound_double = Abstract.calcul.Round_double(19.990000000000);
        Assert.assertEquals(19.99, testRound_double, 0);
    }

    @org.junit.Test
    public void testRound_float() throws Exception {
        final float testRound_double = Abstract.calcul.Round_float((float) 19.990000000000);
        Assert.assertEquals(19.99, testRound_double, 0.001);

    }
}