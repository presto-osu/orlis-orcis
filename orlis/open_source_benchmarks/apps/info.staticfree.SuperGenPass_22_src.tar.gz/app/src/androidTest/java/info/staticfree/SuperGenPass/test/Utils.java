package info.staticfree.SuperGenPass.test;

import android.support.annotation.NonNull;

import junit.framework.TestCase;

import java.io.IOException;

import info.staticfree.SuperGenPass.PasswordGenerationException;
import info.staticfree.SuperGenPass.hashes.DomainBasedHash;

public final class Utils {

    private Utils() {
        // This class cannot be instantiated.
    }

    public static void testATonOfPasswords(@NonNull final DomainBasedHash hash, final int minlen,
            final int maxlen) throws PasswordGenerationException, IOException {
        for (int len = minlen; len < maxlen; len++) {
            for (int i = 0; i < 1000; i += 10) {
                final String generated = hash.generate(String.valueOf(i), "example.org", len);
                TestCase.assertNotNull(generated);
                TestCase.assertEquals(len, generated.length());
            }
        }
    }
}
