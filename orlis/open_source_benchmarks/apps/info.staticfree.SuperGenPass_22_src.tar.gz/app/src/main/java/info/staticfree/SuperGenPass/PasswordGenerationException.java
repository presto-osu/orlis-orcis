package info.staticfree.SuperGenPass;

import android.support.annotation.NonNull;

/**
 * An exception raised if there was a problem generating a password with the given criteria.
 *
 * @author steve
 */
public class PasswordGenerationException extends Exception {

    public PasswordGenerationException(@NonNull final String string,
            @NonNull final Throwable source) {
        super(string, source);
    }

    public PasswordGenerationException(final String string) {
        super(string);
    }

    /**
     *
     */
    private static final long serialVersionUID = 6491091736643793303L;
}
