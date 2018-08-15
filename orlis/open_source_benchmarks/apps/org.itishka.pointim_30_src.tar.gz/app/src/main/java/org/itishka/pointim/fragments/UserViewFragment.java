package org.itishka.pointim.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.itishka.pointim.R;
import org.itishka.pointim.adapters.PostListAdapter;
import org.itishka.pointim.adapters.UserInfoPostListAdapter;
import org.itishka.pointim.model.point.ExtendedUser;
import org.itishka.pointim.model.point.PointResult;
import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.network.PointConnectionManager;
import org.itishka.pointim.network.requests.PostListRequest;
import org.itishka.pointim.network.requests.UserInfoRequest;

import java.io.IOException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class UserViewFragment extends PostListFragment {

    private String mUser;
    private RequestListener<ExtendedUser> mUserInfoRequestListener = new RequestListener<ExtendedUser>() {
        @Override
        public void onRequestSuccess(ExtendedUser user) {
            if (user != null && user.isSuccess()) {
                ((UserInfoPostListAdapter) getAdapter()).setUserInfo(user);
            } else if (!isDetached()) {
                Toast.makeText(getActivity(), String.format(getString(R.string.toast_error_template), (user == null) ? "null" : user.error), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onRequestFailure(SpiceException retrofitError) {
            if (!isDetached()) {
                Toast.makeText(getActivity(), retrofitError.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private RequestListener<ExtendedUser> mUserInfoCacheListener = new RequestListener<ExtendedUser>() {
        @Override
        public void onRequestSuccess(ExtendedUser user) {
            if (user != null && user.isSuccess()) {
                ((UserInfoPostListAdapter) getAdapter()).setUserInfo(user);
            }
        }

        @Override
        public void onRequestFailure(SpiceException retrofitError) {
        }
    };

    public static UserViewFragment newInstance(String tag) {
        UserViewFragment fragment = new UserViewFragment();
        Bundle args = new Bundle();
        args.putString("user", tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected PostListAdapter createAdapter() {
        return new UserInfoPostListAdapter(getActivity());
    }

    @Override
    protected PostListRequest createRequest() {
        return new BlogRequest(mUser);
    }

    @Override
    protected PostListRequest createRequest(long before) {
        return new BlogRequest(mUser, before);
    }

    protected UserInfoRequest createUserInfoRequest() {
        return new UserInfoRequest(mUser);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getArguments().getString("user");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        UserInfoRequest request = createUserInfoRequest();
        getSpiceManager().getFromCache(ExtendedUser.class, request.getCacheName(), DurationInMillis.ALWAYS_RETURNED, mUserInfoCacheListener);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void update() {
        super.update();
        UserInfoRequest request = createUserInfoRequest();
        getSpiceManager().execute(request, request.getCacheName(), DurationInMillis.ALWAYS_EXPIRED, mUserInfoRequestListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_subscribe) {
            PointConnectionManager.getInstance().pointIm.subscribeUser(mUser, "", new Callback<Void>() {
                @Override
                public void success(Void postList, Response response) {
                    if (!isDetached())
                        Toast.makeText(getActivity(), getString(R.string.toast_subscribed), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("UserViewFragment", "failure " + error.getBody());
                    if (!isDetached())
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        } else if (id == R.id.action_unsubscribe) {
            PointConnectionManager.getInstance().pointIm.unsubscribeUser(mUser, new Callback<PointResult>() {
                @Override
                public void success(PointResult postList, Response response) {
                    if (postList.isSuccess()) {
                        Toast.makeText(getActivity(), getString(R.string.toast_unsubscribed), Toast.LENGTH_SHORT).show();
                    } else {
                        if (!isDetached())
                            Toast.makeText(getActivity(), postList.error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (!isDetached())
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        } else if (id == R.id.action_subscribe_recommendations) {
            PointConnectionManager.getInstance().pointIm.subscribeUserRecommendations(mUser, "", new Callback<Void>() {
                @Override
                public void success(Void postList, Response response) {
                    if (!isDetached())
                        Toast.makeText(getActivity(), getString(R.string.toast_recommendations_subscribed), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void failure(RetrofitError error) {
                    if (!isDetached())
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        } else if (id == R.id.action_unsubscribe_recommendations) {
            PointConnectionManager.getInstance().pointIm.unsubscribeUserRecommendations(mUser, new Callback<PointResult>() {
                @Override
                public void success(PointResult postList, Response response) {
                    if (postList.isSuccess()) {
                        Toast.makeText(getActivity(), getString(R.string.toast_recommendations_unsubscribed), Toast.LENGTH_SHORT).show();
                    } else {
                        if (!isDetached())
                            Toast.makeText(getActivity(), postList.error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (!isDetached())
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_user, menu);

    }

    public static class BlogRequest extends PostListRequest {
        private final String mUser;

        public BlogRequest(String user, long before) {
            super(before);
            mUser = user;
        }

        public BlogRequest(String user) {
            super();
            mUser = user;
        }

        @Override
        public String getCacheName() {
            return super.getCacheName() + "-" + mUser;
        }

        @Override
        public PostList load() throws Exception {
            return getService().getBlog(mUser);
        }

        @Override
        public PostList loadBefore(long before) throws Exception {
            return getService().getBlog(before, mUser);
        }

    }
}
