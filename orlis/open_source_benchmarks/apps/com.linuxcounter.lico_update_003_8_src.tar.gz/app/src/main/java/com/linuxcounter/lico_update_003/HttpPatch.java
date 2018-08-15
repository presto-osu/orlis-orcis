package com.linuxcounter.lico_update_003;

import org.apache.http.client.methods.HttpPost;

/**
 * Created by alex on 08.05.15.
 */
public class HttpPatch extends HttpPost {
    public static final String METHOD_PATCH = "PATCH";

    public HttpPatch(final String url) {
        super(url);
    }

    @Override
    public String getMethod() {
        return METHOD_PATCH;
    }
}
