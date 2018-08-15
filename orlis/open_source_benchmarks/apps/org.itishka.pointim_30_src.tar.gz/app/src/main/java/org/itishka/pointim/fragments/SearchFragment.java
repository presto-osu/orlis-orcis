package org.itishka.pointim.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.itishka.pointim.R;
import org.itishka.pointim.listeners.SimplePointClickListener;
import org.itishka.pointim.model.point.Tag;
import org.itishka.pointim.model.point.TagList;
import org.itishka.pointim.model.point.User;
import org.itishka.pointim.model.point.UserList;
import org.itishka.pointim.network.PointConnectionManager;
import org.itishka.pointim.network.requests.TagsRequest;
import org.itishka.pointim.network.requests.UserSubscriptionsRequest;
import org.itishka.pointim.utils.Utils;
import org.itishka.pointim.widgets.FlowLayout;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends SpicedFragment {
    FlowLayout mUsersLayout;
    FlowLayout mTagsLayout;
    TagList mTags;
    UserList mUsers;
    TagList mFilteredTags;
    UserList mFilteredUsers;
    private SearchView mSearchView;

    private SimplePointClickListener mSimplePointClickListener = new SimplePointClickListener(this);

    public SearchFragment() {
    }

    private View.OnClickListener mOnTagClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mSimplePointClickListener.onTagClicked(((TextView) view).getText().toString());
        }
    };
    private View.OnClickListener mOnUserClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mSimplePointClickListener.onUserClicked((String) view.getTag());
        }
    };

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        setHasOptionsMenu(true);

        mTagsLayout = (FlowLayout) rootView.findViewById(R.id.tags);
        mUsersLayout = (FlowLayout) rootView.findViewById(R.id.users);

        TagsRequest request = new TagsRequest(PointConnectionManager.getInstance().loginResult.login);
        getSpiceManager().getFromCacheAndLoadFromNetworkIfExpired(request, request.getCacheName(), DurationInMillis.ONE_DAY, mTagsRequestListener);
        UserSubscriptionsRequest request2 = new UserSubscriptionsRequest(PointConnectionManager.getInstance().loginResult.login);
        getSpiceManager().getFromCacheAndLoadFromNetworkIfExpired(request2, request2.getCacheName(), DurationInMillis.ONE_DAY, mUsersRequestListener);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getActivity().finish();//FIXME
                return false;
            }
        });
        searchMenuItem.expandActionView();
        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(mOnQueryTextListener);
        //searchMenuItem
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private RequestListener<TagList> mTagsRequestListener = new RequestListener<TagList>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //
        }

        @Override
        public void onRequestSuccess(TagList tags) {
            if (tags != null) {
                mTags = tags;
                mFilteredTags = filterTags(tags, mSearchView.getQuery());
                showTags(mFilteredTags);
            }
        }
    };

    private void showTags(TagList tags) {
        if (tags != null) {
            int n = 0;
            mTagsLayout.removeAllViews();
            for (Tag tag : tags) {
                LayoutInflater li;
                li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                final TextView v = (TextView) li.inflate(R.layout.tag, mTagsLayout, false);
                v.setText(tag.tag);
                mTagsLayout.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                v.setOnClickListener(mOnTagClickListener);
            }
        }
    }


    private void showUsers(UserList users) {
        if (users != null) {
            int n = 0;
            mUsersLayout.removeAllViews();
            for (User user : users) {
                LayoutInflater li;
                li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                final View v = li.inflate(R.layout.user_chip, mUsersLayout, false);
                v.setTag(user.login);
                ((TextView) v.findViewById(R.id.login)).setText(user.login);
                mUsersLayout.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                Utils.showAvatar(user.login, user.avatar, (ImageView) v.findViewById(R.id.avatar));
                v.setOnClickListener(mOnUserClickListener);
            }
        }
    }

    private RequestListener<UserList> mUsersRequestListener = new RequestListener<UserList>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //
        }

        @Override
        public void onRequestSuccess(UserList users) {
            if (users != null) {
                mUsers = users;
                mFilteredUsers = filterUsers(mUsers, mSearchView.getQuery());
                showUsers(mFilteredUsers);
            }
        }
    };

    private static TagList filterTags(TagList tags, CharSequence query) {
        if (TextUtils.isEmpty(query))
            return tags;
        String queryString = query.toString().toLowerCase();
        TagList filtered = new TagList();
        boolean found = false;
        for (Tag tag : tags) {
            if (queryString.equalsIgnoreCase(tag.tag)) {
                filtered.add(0, tag);
                found = true;
            } else if (tag.tag.toLowerCase().startsWith(queryString)) {
                filtered.add(tag);
            }
        }
        if (!found) {
            Tag tag = new Tag();
            tag.tag = queryString;
            tag.count = 0;
            filtered.add(0, tag);
        }
        return filtered;
    }

    private static UserList filterUsers(UserList users, CharSequence query) {
        if (TextUtils.isEmpty(query))
            return users;
        String queryString = query.toString().toLowerCase();
        UserList filtered = new UserList();
        boolean found = false;
        for (User user : users) {
            if (queryString.equalsIgnoreCase(user.login)) {
                filtered.add(0, user);
                found = true;
            } else if (user.login.toLowerCase().startsWith(queryString)) {
                filtered.add(user);
            }
        }
        if (!found) {
            User user = new User();
            user.login = queryString;
            user.avatar = Utils.getAvatarByLogin(user.login);
            filtered.add(0, user);
        }
        return filtered;
    }

    SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mFilteredTags = filterTags(mTags, newText);
            showTags(mFilteredTags);
            mFilteredUsers = filterUsers(mUsers, newText);
            showUsers(mFilteredUsers);
            return true;
        }
    };
}
