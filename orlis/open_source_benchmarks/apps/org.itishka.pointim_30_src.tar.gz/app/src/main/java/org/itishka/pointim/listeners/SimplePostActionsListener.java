package org.itishka.pointim.listeners;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.itishka.pointim.R;
import org.itishka.pointim.activities.NewPostActivity;
import org.itishka.pointim.fragments.ReplyDialogFragment;
import org.itishka.pointim.model.point.Comment;
import org.itishka.pointim.model.point.PointResult;
import org.itishka.pointim.model.point.Post;
import org.itishka.pointim.network.PointConnectionManager;
import org.itishka.pointim.utils.Utils;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Tishka17 on 28.04.2016.
 */
public class SimplePostActionsListener implements OnPostActionsListener {

    private Fragment mFragment = null;
    private OnPostChangedListener mOnPostChangedListener;

    public SimplePostActionsListener(Fragment fragment) {
        mFragment = fragment;
    }

    private Context getContext() {
        return mFragment.getContext();
    }

    @Override
    public void onBookmark(@NonNull final Post post, final CheckBox checkBox) {
        if (checkBox.isChecked()) {
            PointConnectionManager.getInstance().pointIm.addBookmark(
                    post.post.id,
                    null,
                    new Callback<PointResult>() {
                        @Override
                        public void success(PointResult pointResult, Response response) {
                            Toast.makeText(getContext(), String.format(getContext().getString(R.string.toast_bookmarked_template), post.post.id), Toast.LENGTH_SHORT).show();
                            post.bookmarked = true;
                            notifyChanged(post);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(getContext(), String.format(getContext().getString(R.string.toast_bookmark_error_template), post.post.id, error), Toast.LENGTH_SHORT).show();
                            notifyChanged(post);
                        }
                    }
            );
        } else {
            PointConnectionManager.getInstance().pointIm.deleteBookmark(
                    post.post.id,
                    new Callback<Void>() {
                        @Override
                        public void success(Void pointResult, Response response) {
                            Toast.makeText(getContext(), String.format(getContext().getString(R.string.toast_bookmark_remove_template), post.post.id), Toast.LENGTH_SHORT).show();
                            post.bookmarked = false;
                            notifyChanged(post);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(getContext(), String.format(getContext().getString(R.string.toast_bookmark_remove_error_template), post.post.id, error), Toast.LENGTH_SHORT).show();
                            notifyChanged(post);
                        }
                    }
            );
        }
    }

    @Override
    public void onMenuClicked(@NonNull Post post, Menu menu, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                onEditPost(post, menu, item);
                break;
            case R.id.action_delete:
                onDeletePost(post, menu, item);
                break;
            case R.id.action_copy_link:
                onCopyLink(post, menu, item);
                break;
            case R.id.action_recommend:
                onRecommendPost(post, menu, item);
                break;
            case R.id.action_not_recommend:
                onNotRecommendPost(post, menu, item);
                break;
            case R.id.action_reply:
                onReply(post, menu, item);
                break;
        }
    }


    private void onReply(Post post, Menu menu, MenuItem item) {
        if (post.rec != null) {
            ReplyDialogFragment.show((AppCompatActivity) getContext(), post.post.id, post.rec.comment_id);
        } else {
            ReplyDialogFragment.show((AppCompatActivity) getContext(), post.post.id);
        }
    }

    private void onRecommendPost(@NonNull final Post post, Menu menu, MenuItem item) {
        final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(String.format(getContext().getString(R.string.dialog_recommend_title_template), post.post.id))
                .positiveText(android.R.string.ok)
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String text = ((EditText) (dialog.findViewById(R.id.recommend_text))).getText().toString();
                        PointConnectionManager.getInstance().pointIm.recommend(post.post.id, text, new Callback<PointResult>() {
                            @Override
                            public void success(PointResult pointResult, Response response) {
                                if (pointResult.isSuccess()) {
                                    Toast.makeText(getContext(), getContext().getString(R.string.toast_recommended), Toast.LENGTH_SHORT).show();
                                    if (mOnPostChangedListener != null) {
                                        post.recommended = true;
                                        mOnPostChangedListener.onChanged(post);
                                    }
                                } else {
                                    Toast.makeText(getContext(), pointResult.error, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Toast.makeText(getContext(), error.toString() + "\n\n" + error.getCause(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .customView(R.layout.dialog_input, true)
                .build();
        dialog.show();
    }


    private void onNotRecommendPost(@NonNull final Post post, Menu menu, MenuItem item) {
        PointConnectionManager.getInstance().pointIm.notRecommend(post.post.id, new Callback<PointResult>() {
            @Override
            public void success(PointResult pointResult, Response response) {
                if (pointResult.isSuccess()) {
                    Toast.makeText(getContext(), getContext().getString(R.string.toast_recommended_not), Toast.LENGTH_SHORT).show();
                    if (mOnPostChangedListener != null) {
                        post.recommended = false;
                        mOnPostChangedListener.onChanged(post);
                    }
                } else {
                    Toast.makeText(getContext(), pointResult.error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getContext(), error.toString() + "\n\n" + error.getCause(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void onEditPost(@NonNull Post post, Menu menu, MenuItem item) {
        Intent intent = new Intent(getContext(), NewPostActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(NewPostActivity.EXTRA_ID, post.post.id);
        bundle.putBoolean(NewPostActivity.EXTRA_PRIVATE, post.post.isPrivate);
        bundle.putString(NewPostActivity.EXTRA_TEXT, post.post.text.text.toString());
        bundle.putStringArray(NewPostActivity.EXTRA_TAGS, post.post.tags.toArray(new String[post.post.tags.size()]));
        intent.putExtras(bundle);
        getContext().startActivity(intent);
    }

    private void onCopyLink(@NonNull Post post, Menu menu, MenuItem item) {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        Uri uri = Utils.generateSiteUri(post.post.id);
        ClipData clip = ClipData.newRawUri(uri.toString(), uri);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), String.format(getContext().getString(R.string.toast_link_copied__template), uri.toString()), Toast.LENGTH_SHORT).show();
    }


    private void onDeletePost(@NonNull final Post post, Menu menu, MenuItem item) {
        final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(String.format(getContext().getString(R.string.dialog_delete_title_template), post.post.id))
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PointConnectionManager.getInstance().pointIm.deletePost(post.post.id, new Callback<PointResult>() {
                            @Override
                            public void success(PointResult pointResult, Response response) {
                                if (pointResult.isSuccess()) {
                                    Toast.makeText(getContext(), getContext().getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
                                    if (mOnPostChangedListener != null) {
                                        mOnPostChangedListener.onDeleted(post);
                                    }
                                } else {
                                    Toast.makeText(getContext(), pointResult.error, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Toast.makeText(getContext(), error.toString() + "\n\n" + error.getCause(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .build();
        dialog.show();
    }

    private void notifyChanged(Post post) {
        if (mOnPostChangedListener != null)
            mOnPostChangedListener.onChanged(post);
    }

    public void setOnPostChangedListener(OnPostChangedListener onPostChangedListener) {
        mOnPostChangedListener = onPostChangedListener;
    }

    @Override
    public void updateMenu(Menu menu, ShareActionProvider provider, Post post) {
        menu.setGroupVisible(R.id.group_my, post.post.author.login.equalsIgnoreCase(PointConnectionManager.getInstance().loginResult.login));
        menu.setGroupVisible(R.id.group_my_editable,
                post.post.author.login.equalsIgnoreCase(PointConnectionManager.getInstance().loginResult.login) && post.editable
        );
        menu.setGroupVisible(R.id.group_not_recommended,
                !post.post.author.login.equalsIgnoreCase(PointConnectionManager.getInstance().loginResult.login) && !post.recommended
        );
        menu.setGroupVisible(R.id.group_recommended,
                !post.post.author.login.equalsIgnoreCase(PointConnectionManager.getInstance().loginResult.login) && post.recommended
        );

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        StringBuilder sb = new StringBuilder();
        sb.append("@")
                .append(post.post.author.login)
                .append(":");
        if (post.post.tags != null)
            for (String tag : post.post.tags) {
                sb.append(" *").append(tag);
            }
        sb.append("\n\n")
                .append(post.post.text.text)
                .append("\n\n")
                .append(Utils.generateSiteUri(post.post.id));
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());

        provider.setShareIntent(sendIntent);

    }
}
