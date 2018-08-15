package info.staticfree.SuperGenPass.test;

import android.support.annotation.NonNull;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import java.io.IOException;

import info.staticfree.SuperGenPass.PasswordGenerationException;
import info.staticfree.SuperGenPass.hashes.HotpPin;

public class TestHotpPin extends AndroidTestCase {

    public void testHotpPin() throws PasswordGenerationException, IOException {
        final HotpPin pinGen = new HotpPin(mContext);

        // these are bad to give as an output

        assertTrue(pinGen.isNumericalRun("1111"));
        assertTrue(pinGen.isNumericalRun("1234"));
        assertTrue(pinGen.isNumericalRun("4321"));
        assertTrue(pinGen.isNumericalRun("2468"));
        assertTrue(pinGen.isNumericalRun("0000"));
        assertTrue(pinGen.isNumericalRun("9999"));
        assertTrue(pinGen.isNumericalRun("0369"));

        assertFalse(pinGen.isNumericalRun("0101"));
        assertFalse(pinGen.isNumericalRun("1235"));

        assertEquals("3097", pinGen.generate("foo", "example.org", 4));

    }

    public void testDomainFiltering() throws IOException, PasswordGenerationException {
        final HotpPin pinGen = new HotpPin(mContext);
        pinGen.setCheckDomain(true);
        assertEquals(pinGen.generate("foo", "foo.example.org", 4),
                pinGen.generate("foo", "example.org", 4));
    }

    public void testDomainFilteringOff() throws IOException, PasswordGenerationException {
        final HotpPin pinGen = new HotpPin(mContext);
        pinGen.setCheckDomain(false);
        assertFalse(pinGen.generate("foo", "foo.example.org", 4).equals(
                pinGen.generate("foo", "example.org", 4)));
    }

    public void testNumericRuns() throws IOException {
        final HotpPin pinGen = new HotpPin(mContext);

        // these are bad to give as an output

        assertTrue(pinGen.isNumericalRun("1111"));
        assertTrue(pinGen.isNumericalRun("1234"));
        assertTrue(pinGen.isNumericalRun("4321"));
        assertTrue(pinGen.isNumericalRun("2468"));
        assertTrue(pinGen.isNumericalRun("0000"));
        assertTrue(pinGen.isNumericalRun("9999"));
        assertTrue(pinGen.isNumericalRun("0369"));

        // these aren't runs

        assertFalse(pinGen.isNumericalRun("0101"));
        assertFalse(pinGen.isNumericalRun("1235"));
    }

    public void testIncompleteNumericRuns() throws IOException {
        final HotpPin pinGen = new HotpPin(mContext);

        // these are bad to give as an output

        assertTrue(pinGen.isIncompleteNumericalRun("1111"));
        assertTrue(pinGen.isIncompleteNumericalRun("1113"));
        assertTrue(pinGen.isIncompleteNumericalRun("3111"));
        assertTrue(pinGen.isIncompleteNumericalRun("10001"));
        assertTrue(pinGen.isIncompleteNumericalRun("011101"));

        // these aren't runs
        assertFalse(pinGen.isIncompleteNumericalRun("0010"));
        assertFalse(pinGen.isIncompleteNumericalRun("1234"));

    }

    public void testGeneratedLength() throws PasswordGenerationException, IOException {
        final HotpPin pinGen = new HotpPin(mContext);

        for (int i = 3; i <= 8; i++) {
            assertTrue(pinGen.generate("foo", "example.org", i).length() == i);
        }
    }

    public void testInvalidLengths() throws IOException {
        final HotpPin pinGen = new HotpPin(mContext);
        testInvalidLength(pinGen, -1);
        testInvalidLength(pinGen, 0);
        testInvalidLength(pinGen, 1);
        testInvalidLength(pinGen, 2);
        testInvalidLength(pinGen, 9);
        testInvalidLength(pinGen, 100);
    }

    private void testInvalidLength(@NonNull final HotpPin pinGen, final int len) {
        boolean thrown = false;
        try {
            pinGen.generate("foo", "example.org", len);
        } catch (@NonNull final PasswordGenerationException e) {
            thrown = true;
        }
        assertTrue("exception not thrown for length " + len, thrown);
    }

    public void testBadPins() throws IOException {
        final HotpPin pinGen = new HotpPin(mContext);
        final String[] badPins = new String[] { "0000", "1111", "1234", "1984", "2001", "1122",
                "553388", "1234567", "8844", "9876", "9753", "2000", "8000", "10001", "4111",
                "0007", "90210", "1004", "8068", "90210" };

        for (final String badPin : badPins) {
            assertTrue("bad PIN: " + badPin + " not detected to be bad", pinGen.isBadPin(badPin));
        }
    }

    public void testGoodPins() throws IOException {
        final HotpPin pinGen = new HotpPin(mContext);
        final String[] goodPins = new String[] { "1837", "7498", "8347", "7426", "7172", "9012",
                "8493", "400500", "4385719", "12349" };

        for (final String goodPin : goodPins) {
            assertFalse(pinGen.isBadPin(goodPin));
        }
    }

    @LargeTest
    public void testATonOfPasswords() throws PasswordGenerationException, IOException {
        final HotpPin pinGen = new HotpPin(mContext);
        Utils.testATonOfPasswords(pinGen, 3, 8);
    }
}
