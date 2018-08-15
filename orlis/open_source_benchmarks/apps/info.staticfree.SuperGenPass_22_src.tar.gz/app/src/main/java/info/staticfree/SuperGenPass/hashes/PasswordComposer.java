package info.staticfree.SuperGenPass.hashes;
/*
 * Copyright (C) 2010 Steve Pomeroy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import info.staticfree.SuperGenPass.IllegalDomainException;
import info.staticfree.SuperGenPass.PasswordGenerationException;

public final class PasswordComposer extends DomainBasedHash {
    public static final String TYPE = "pwc";

    private final MessageDigest md5;

    public PasswordComposer(@NonNull final Context context)
            throws NoSuchAlgorithmException, IOException {
        super(context);
        md5 = MessageDigest.getInstance("MD5");
    }

    /**
     * Returns the standard hex-encoded string md5sum of the data.
     *
     * @return hex-encoded string of the md5sum of the data
     */
    private String md5hex(final byte[] data) {
        final byte[] md5data = md5.digest(data);
        final StringBuilder md5hex = new StringBuilder();
        for (final byte aMd5data : md5data) {
            md5hex.append(String.format("%02x", aMd5data));
        }
        return md5hex.toString();
    }

    /**
     * Generates a domain password based on the PasswordComposer algorithm.
     *
     * @param masterPass master password
     * @param domain un-filtered domain (eg. www.example.org)
     * @return generated password
     * @see http://www.xs4all.nl/~jlpoutre/BoT/Javascript/PasswordComposer/
     */
    @NonNull
    @Override
    public String generateWithFilteredDomain(@NonNull final String masterPass,
            @NonNull final String domain, final int length) throws PasswordGenerationException {
        if (domain.isEmpty()) {
            throw new IllegalDomainException("Missing domain");
        }

        if (masterPass.isEmpty()) {
            throw new PasswordGenerationException("empty password");
        }

        if (length < 1 || length > 31) {
            throw new PasswordGenerationException(
                    "Requested length out of range. Expecting value between 1 and 31 inclusive.");
        }

        return md5hex((masterPass + ':' + domain).getBytes()).substring(0, length);
    }
}
