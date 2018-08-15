package net.sf.content;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;

/**
 * Context wrapper that also wraps resources.
 */
public class ContextResourcesWrapper extends ContextWrapper {

    private final Resources resources;

    public ContextResourcesWrapper(Context base, Resources resources) {
        super(base);
        this.resources = resources;
    }

    @Override
    public Resources getResources() {
        return resources;
    }
}
