package org.itishka.pointim.adapters;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.itishka.pointim.R;
import org.itishka.pointim.model.point.User;
import org.itishka.pointim.model.point.UserList;
import org.itishka.pointim.utils.Utils;

/**
 * Created by Tishka17 on 06.08.2015.
 */
public class UserCompletionAdapter implements ListAdapter, Filterable {
    private DataSetObservable mDataSetObservable = new DataSetObservable();
    private LayoutInflater mInflater;
    private UserList mFilteredUsers = new UserList();
    private UserList mUsers = new UserList();
    private UserFilter mFilter;

    public UserCompletionAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(UserList users) {
        mUsers = users;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public int getCount() {
        return (mFilteredUsers != null) ? mFilteredUsers.size() : 0;
    }

    @Override
    public User getItem(int position) {
        return mFilteredUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    private static class Holder {
        public final ImageView avatar;
        public final TextView login;
        public final TextView name;

        private Holder(View view) {
            this.avatar = (ImageView) view.findViewById(R.id.avatar);
            this.login = (TextView) view.findViewById(R.id.login);
            this.name = (TextView) view.findViewById(R.id.name);
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        Holder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.adapter_user_completion, viewGroup, false);

            holder = new Holder(view);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }
        User user = getItem(position);

        holder.login.setText(user.login);
        holder.name.setText(user.name);
        Utils.showAvatar(user.login, user.avatar, holder.avatar);
        return view;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new UserFilter();
        }
        return mFilter;
    }

    public void addIfAbsent(User user) {
        for (User u : mUsers) {
            if (u.id == user.id) return;
        }
        mUsers.add(user);
    }

    private class UserFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                filterResults.values = mUsers;
                filterResults.count = mUsers.size();
            } else {
                final String lastToken = constraint.toString().toLowerCase();
                final int count = mUsers.size();
                final UserList list = new UserList();
                User user;

                for (int i = 0; i < count; i++) {
                    user = mUsers.get(i);
                    if (user.login.toLowerCase().startsWith(lastToken)) {
                        list.add(user);
                    }
                }

                filterResults.values = list;
                filterResults.count = list.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredUsers = (UserList) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
