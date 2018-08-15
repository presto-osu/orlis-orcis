package android.widget;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;

import java.util.Arrays;

import de.naturalnet.mirwtfapp.MainActivity;

/**
 * Created by mirabilos on 26.04.16.
 */
public class WtfArrayAdapter<T> extends ArrayAdapter<T> {
    private MainActivity sParent;
    private WtfArrayFilter sFilter;
    private Filter pFilter;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public WtfArrayAdapter(Context context, @LayoutRes int resource, @NonNull T[] objects, MainActivity parent) {
        super(context, resource, 0, Arrays.asList(objects));
        sParent = parent;
        sFilter = new WtfArrayFilter();
        pFilter = super.getFilter();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Filter getFilter() {
        return sFilter;
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class WtfArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            return pFilter.performFiltering(prefix == null ? null : sParent.normaliseAcronym(prefix.toString()));
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            pFilter.publishResults(constraint, results);
        }
    }
}
