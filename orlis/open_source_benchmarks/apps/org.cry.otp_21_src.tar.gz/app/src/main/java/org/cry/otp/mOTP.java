package org.cry.otp;

import java.util.Date;
import java.util.TimeZone;

class mOTP {
    public static String gen(String PIN, String seed, String zone) {
        long time = new Date().getTime();
        String epoch = "" + (time + TimeZone.getTimeZone(zone).getOffset(time));
        String secret = new MD5(seed).asHex().substring(0, 16);
        epoch = epoch.substring(0, epoch.length() - 4);
        String otp = epoch + secret + PIN;
        MD5 hash = new MD5(otp);
        otp = hash.asHex().substring(0, 6);
        return otp;
    }
}
