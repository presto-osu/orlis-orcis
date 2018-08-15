package info.staticfree.SuperGenPass.hashes;

/*
 * Copyright (C) 2010-2013 Steve Pomeroy
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

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import info.staticfree.SuperGenPass.IllegalDomainException;
import info.staticfree.SuperGenPass.PasswordGenerationException;
import info.staticfree.SuperGenPass.R;

/**
 * A password hash that takes a password and a domain. Domains are optionally checked against a
 * database of known TLDs in order to generate domain-specific passwords. For example,
 * "www.example.org" and "www2.example.org" will generate the same password.
 *
 * @author Steve Pomeroy
 */
public abstract class DomainBasedHash {
    private static final Pattern PATTERN_IP_ADDRESS =
            Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    private boolean checkDomain;
    private ArrayList<String> domains;
    @NonNull
    private final Context mContext;

    public DomainBasedHash(@NonNull final Context context) throws IOException {
        mContext = context;
        loadDomains();
    }

    /**
     * This list should remain the same and in sync with the canonical SGP, so that passwords
     * generated in one place are the same as others.
     *
     * @throws IOException on disk errors
     */
    public void loadDomains() throws IOException {
        final InputStream is = mContext.getResources().openRawResource(R.raw.domains);

        final StringBuilder jsonString = new StringBuilder();
        try {

            for (final BufferedReader isReader =
                 new BufferedReader(new InputStreamReader(is), 16000); isReader.ready(); ) {
                jsonString.append(isReader.readLine());
            }

            final JSONArray domainJson = new JSONArray(jsonString.toString());
            domains = new ArrayList<>(domainJson.length());
            for (int i = 0; i < domainJson.length(); i++) {
                domains.add(domainJson.getString(i));
            }
        } catch (@NonNull IOException | JSONException e) {
            final IOException ioe = new IOException("Unable to load domains");
            ioe.initCause(e);
        }

        Assert.assertTrue("Domains did not seem to load", domains.size() > 100);
    }

    /**
     * Computes the site's domain, based on the provided hostname. This takes into account things
     * like "co.uk" and other such multi-level TLDs.
     *
     * @param hostname the full hostname
     * @return the domain of the URI
     * @throws PasswordGenerationException if there is an error generating the password
     */
    @NonNull
    public String getDomain(@NonNull String hostname) throws PasswordGenerationException {
        hostname = hostname.toLowerCase();

        if (!checkDomain) {
            return hostname;
        }

        // IP addresses should be composed based on the full address.
        if (PATTERN_IP_ADDRESS.matcher(hostname).matches()) {
            return hostname;
        }

        // for single-level TLDs, we only want the TLD and the 2nd level domain
        final String[] hostParts = hostname.split("\\.");
        if (hostParts.length < 2) {
            throw new IllegalDomainException("Invalid domain: '" + hostname + '\'');
        }
        String domain = hostParts[hostParts.length - 2] + '.' + hostParts[hostParts.length - 1];

        // do a slow search of all the possible multi-level TLDs and
        // see if we need to pull in one level deeper.
        for (final String tld : domains) {
            if (domain.equals(tld)) {
                if (hostParts.length < 3) {
                    throw new IllegalDomainException(
                            "Invalid domain. '" + domain + "' seems to be a TLD.");
                }
                domain = hostParts[hostParts.length - 3] + '.' + domain;
                break;
            }
        }
        return domain;
    }

    /**
     * @param checkDomain if true, sub-domains will be stripped from the hashing
     */
    public void setCheckDomain(final boolean checkDomain) {
        this.checkDomain = checkDomain;
    }

    /**
     * Generates a password based on the given domain and a master password. Each time the method is
     * passed a given master password / domain, it will output the same password for that pair.
     *
     * @param masterPass master password
     * @param domain un-filtered domain (eg. www.example.org)
     * @param length generated password length
     * @return generated password based on the master password and the domain
     * @throws PasswordGenerationException if the criteria for generating the password are not met.
     * Often a length or domain issue.
     */
    @NonNull
    public String generate(@NonNull final String masterPass, @NonNull final String domain,
            final int length) throws PasswordGenerationException {
        return generateWithFilteredDomain(masterPass, getDomain(domain), length);
    }

    /**
     * Generates a password based on the given domain and a master password. Each time the method is
     * passed a given master password / domain, it will output the same password for that pair.
     *
     * @param masterPass master password
     * @param domain filtered domain (eg. example.org)
     * @param length generated password length
     * @return generated password based on the master password and the domain
     * @throws PasswordGenerationException if the criteria for generating the password are not met.
     * Often a length or domain issue.
     */
    @NonNull
    protected abstract String generateWithFilteredDomain(@NonNull String masterPass,
            @NonNull String domain, int length) throws PasswordGenerationException;
}