package info.staticfree.SuperGenPass.test;

import android.support.annotation.NonNull;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import info.staticfree.SuperGenPass.PasswordGenerationException;
import info.staticfree.SuperGenPass.hashes.SuperGenPass;

public class SuperGenPassTest extends AndroidTestCase {

    public void testKnownGoods()
            throws NoSuchAlgorithmException, IOException, PasswordGenerationException {

        final SuperGenPass sgp = new SuperGenPass(mContext, "md5");
        sgp.setCheckDomain(true);

        // these were generated using SGP's javascript itself by hand.

        //@formatter:off
        final String[][] knownGoods = new String[][] {
                // basics
                { "a", "example.org", "10", "bieCWgE99X" },
                { "12345", "example.org", "10", "tHR8hvgs1D" },
                { "12345", "example.org", "24", "tHR8hvgs1DHOlfCScJlzsQAA" },
                { "12345", "example.org", "4", "tHR8" },

                // special characters in the password
                { "♥", "example.org", "10", "gd9hJAzinf" },
                { "flambé", "example.org", "10", "vqBh5a76L8" },
                { " ", "example.org", "10", "vKwe7kuLl8" },
                { "foo bar", "example.org", "10", "nZq3XcpQx2" },
                { " foo bar ", "example.org", "10", "qnyA2kcKz8" },

                // domain processing
                { "12345", "www.example.org", "10", "tHR8hvgs1D" },
                { "12345", "example.co.uk", "10", "fdQnYi75VT" },
                { "12345", "www.example.co.uk", "10", "fdQnYi75VT" },
        };
        //@formatter:on

        for (final String[] knownGood : knownGoods) {
            final String msg = "for secret '" + knownGood[0] + "' and domain '" + knownGood[1] +
                    "' of length " + knownGood[2];
            assertEquals(msg, knownGood[3],
                    sgp.generate(knownGood[0], knownGood[1], Integer.parseInt(knownGood[2])));
        }
    }

    public void testInvalidOptions_algorithm() throws Exception {
        boolean caught = false;
        try {
            //noinspection ResultOfObjectAllocationIgnored
            new SuperGenPass(mContext, "kittens");
        } catch (@NonNull final NoSuchAlgorithmException e) {
            caught = true;
        }

        assertTrue("exception thrown", caught);
    }

    public void testInvalidOptions_checkDomain() throws Exception {
        final SuperGenPass sgp = new SuperGenPass(mContext, "md5");
        sgp.setCheckDomain(true);

        // bad domain
        boolean caught = false;
        try {
            sgp.generate("12345", "bad domain", 10);
        } catch (@NonNull final PasswordGenerationException e) {
            caught = true;
        }
        assertTrue("exception thrown", caught);
    }

    public void testInvalidOptions_tooShort() throws Exception {
        final SuperGenPass sgp = new SuperGenPass(mContext, "md5");
        sgp.setCheckDomain(true);
        boolean caught = false;
        try {
            sgp.generate("12345", "example.org", 0);
        } catch (@NonNull final PasswordGenerationException e) {
            caught = true;
        }
        assertTrue("exception thrown", caught);
    }

    public void testInvalidOptions_tooLong() throws Exception {
        final SuperGenPass sgp = new SuperGenPass(mContext, "md5");
        sgp.setCheckDomain(true);
        // too long length
        boolean caught = false;
        try {
            sgp.generate("12345", "example.org", 100);
        } catch (@NonNull final PasswordGenerationException e) {
            caught = true;
        }
        assertTrue("exception thrown", caught);
    }

    public void testSha1() throws NoSuchAlgorithmException, IOException {
        final SuperGenPass sgp = new SuperGenPass(mContext, "sha1");
        sgp.setCheckDomain(true);
    }

    @LargeTest
    public void testATonOfPasswordsSha1()
            throws PasswordGenerationException, IOException, NoSuchAlgorithmException {
        final SuperGenPass sgp = new SuperGenPass(mContext, "sha1");
        Utils.testATonOfPasswords(sgp, 4, 10);
    }

    @LargeTest
    public void testATonOfPasswordsMd5()
            throws PasswordGenerationException, IOException, NoSuchAlgorithmException {
        final SuperGenPass sgp = new SuperGenPass(mContext, "md5");
        Utils.testATonOfPasswords(sgp, 4, 10);
    }
}
