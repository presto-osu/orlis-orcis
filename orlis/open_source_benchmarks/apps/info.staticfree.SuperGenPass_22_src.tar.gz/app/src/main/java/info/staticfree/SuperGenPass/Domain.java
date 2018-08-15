package info.staticfree.SuperGenPass;

import android.net.Uri;
import android.provider.BaseColumns;

public class Domain implements BaseColumns {
    public static final String DOMAIN = "domain";

    public static final String PATH = "domain";

    public static final Uri CONTENT_URI =
            Uri.parse("content://" + RememberedDomainProvider.AUTHORITY + '/' + PATH);

    public static final String SORT_ORDER = DOMAIN + " ASC";
}
