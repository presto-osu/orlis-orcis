package org.itishka.pointim.listeners;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.itishka.pointim.R;
import org.itishka.pointim.model.point.Comment;
import org.itishka.pointim.model.point.PointResult;
import org.itishka.pointim.model.point.Post;
import org.itishka.pointim.network.PointConnectionManager;
import org.itishka.pointim.utils.Utils;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Tishka17 on 08.05.2016.
 */
public class SimpleCommentActionsListener implements OnCommentActionsListener {

    private Fragment mFragment = null;

    public SimpleCommentActionsListener(Fragment fragment) {
        mFragment = fragment;
    }

    private Context getContext() {
        return mFragment.getContext();
    }

    private OnCommentChangedListener omOnCommentChangedListener = null;

    @Override
    public void onBookmark(@NonNull Post post, CheckBox button) {
        throw new RuntimeException("Not implemented yet!");
    }

    @Override
    public void onMenuClicked(@NonNull Post post, @NonNull Comment comment, Menu menu, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_copy_link:
                onCopyLink(post, comment, menu, item);
                break;
            case R.id.action_recommend:
                onRecommendComment(post, comment, menu, item);
                break;
            case R.id.action_not_recommend:
                onNotRecommendComment(post, comment, menu, item);
                break;
            case R.id.action_delete:
                onDeleteComment(post, comment, menu, item);
                break;
        }
    }


    private void onRecommendComment(@NonNull final Post post, final Comment comment, Menu menu, MenuItem item) {
        final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(String.format(getContext().getString(R.string.dialog_recommend_title_template), post.post.id))
                .positiveText(android.R.string.ok)
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String text = ((EditText) (dialog.findViewById(R.id.recommend_text))).getText().toString();
                        PointConnectionManager.getInstance().pointIm.recommendCommend(post.post.id, comment.id, text, new Callback<PointResult>() {
                            @Override
                            public void success(PointResult pointResult, Response response) {
                                if (pointResult.isSuccess()) {
                                    Toast.makeText(getContext(), getContext().getString(R.string.toast_recommended), Toast.LENGTH_SHORT).show();
                                    if (omOnCommentChangedListener != null) {
                                        comment.recommended = true;
                                        omOnCommentChangedListener.onCommentChanged(post, comment);
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

    private void onNotRecommendComment(@NonNull final Post post, @NonNull final Comment comment, Menu menu, MenuItem item) {
        PointConnectionManager.getInstance().pointIm.notRecommendComment(post.post.id, comment.id, new Callback<PointResult>() {
            @Override
            public void success(PointResult pointResult, Response response) {
                if (pointResult.isSuccess()) {
                    Toast.makeText(getContext(), getContext().getString(R.string.toast_recommended_not), Toast.LENGTH_SHORT).show();
                    if (omOnCommentChangedListener != null) {
                        comment.recommended = false;
                        omOnCommentChangedListener.onCommentChanged(post, comment);
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


    private void onCopyLink(@NonNull Post post, Comment comment, Menu menu, MenuItem item) {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        Uri uri = Utils.generateSiteUri(post.post.id, comment.id);
        ClipData clip = ClipData.newRawUri(uri.toString(), uri);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), String.format(getContext().getString(R.string.toast_link_copied__template), uri.toString()), Toast.LENGTH_SHORT).show();
    }

    private void onDeleteComment(@NonNull final Post post, @NonNull final Comment comment, Menu menu, MenuItem item) {
        final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(String.format(getContext().getString(R.string.dialog_delete_comment_title_template), post.post.id, comment.id))
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PointConnectionManager.getInstance().pointIm.deleteComment(post.post.id, comment.id, new Callback<PointResult>() {
                            @Override
                            public void success(PointResult pointResult, Response response) {
                                if (pointResult.isSuccess()) {
                                    Toast.makeText(getContext(), getContext().getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
                                    if (omOnCommentChangedListener != null) {
                                        omOnCommentChangedListener.onCommentDeleted(post, comment);
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

    public void setOnCommentChangedListener(OnCommentChangedListener onCommentChangedListener) {
        omOnCommentChangedListener = onCommentChangedListener;
    }

    @Override
    public void updateMenu(Menu menu, ShareActionProvider provider, Comment comment) {
        menu.setGroupVisible(R.id.group_my, comment.author.login.equalsIgnoreCase(PointConnectionManager.getInstance().loginResult.login));
        menu.setGroupVisible(R.id.group_not_recommended,
                !comment.author.login.equalsIgnoreCase(PointConnectionManager.getInstance().loginResult.login) && !comment.recommended
        );
        menu.setGroupVisible(R.id.group_recommended,
                !comment.author.login.equalsIgnoreCase(PointConnectionManager.getInstance().loginResult.login) && comment.recommended
        );

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        StringBuilder sb = new StringBuilder();
        sb.append("@")
                .append(comment.author.login)
                .append(":");
        sb.append("\n\n")
                .append(comment.text.text)
                .append("\n\n")
                .append(Utils.generateSiteUri(comment.post_id, comment.id));
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());

        provider.setShareIntent(sendIntent);

    }
}
