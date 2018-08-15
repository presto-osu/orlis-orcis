package org.itishka.pointim.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.itishka.pointim.R;
import org.itishka.pointim.listeners.OnCommentActionsListener;
import org.itishka.pointim.listeners.OnPointClickListener;
import org.itishka.pointim.listeners.OnPostActionsListener;
import org.itishka.pointim.model.point.Comment;
import org.itishka.pointim.model.point.Post;
import org.itishka.pointim.utils.ImageSearchHelper;
import org.itishka.pointim.utils.Utils;
import org.itishka.pointim.widgets.ImageList;

import java.lang.ref.WeakReference;

/**
 * Created by Tishka17 on 20.10.2014.
 */
public class SinglePostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final WeakReference<Context> mContext;
    private View.OnClickListener mOnTagClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mOnPointClickListener.onTagClicked(((TextView) view).getText().toString());
        }
    };
    private Post mPost = null;
    private ImageSearchTask mTask;
    private OnPointClickListener mOnPointClickListener;
    private OnPostActionsListener mOnPostActionsListener;
    private OnCommentActionsListener mOnCommentActionsListener;
    private OnItemClickListener mOnItemClickListener;

    public SinglePostAdapter(Context context) {
        super();
        mContext = new WeakReference<>(context);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return 0;
        else
            return 1;
    }

    public int searchCommentById(long id) {
        if (mPost == null || mPost.comments == null)
            return -1;
        for (int i = 0; i < mPost.comments.size(); i++) {
            if (id == mPost.comments.get(i).id)
                return i;
        }
        return -1;
    }

    public void setData(Post post) {
        mPost = post;
        notifyDataSetChanged();
        if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
            mTask.cancel(true);
        }
        mTask = new ImageSearchTask();
        mTask.execute(post);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == 0) {
            final View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.single_post_header, viewGroup, false);
            final PostViewHolder holder = new PostViewHolder(v);
            holder.webLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnPointClickListener.onBrowserLinkClicked((Uri) view.getTag());
                }
            });
            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String user = (String) view.getTag(R.id.imageView);
                    if (!TextUtils.isEmpty(user)) {
                        mOnPointClickListener.onUserClicked(user);
                    }
                }
            });
            holder.favourite.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (mOnPostActionsListener != null)
                                                            mOnPostActionsListener.onBookmark(mPost, holder.favourite);
                                                    }
                                                }
            );
            holder.post_id.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnPointClickListener.onPostClicked(view.getTag().toString());
                }
            });
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onPostClicked(v);
                    }
                }
            });
            return holder;
        } else {
            final View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_comment, viewGroup, false);
            final CommentViewHolder holder = new CommentViewHolder(v);
            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String user = (String) view.getTag(R.id.imageView);
                    if (!TextUtils.isEmpty(user)) {
                        mOnPointClickListener.onUserClicked(user);
                    }
                }
            });
            holder.toolbar.inflateMenu(R.menu.menu_adapter_comment);
            MenuItem item = holder.toolbar.getMenu().findItem(R.id.menu_item_share);
            MenuItemCompat.setActionProvider(item, holder.shareActionProvider);

            holder.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (mOnPostActionsListener != null) {
                        int i = searchCommentById(((Long) v.getTag(R.id.comment_id)).intValue());
                        if (i >= 0) {
                            mOnCommentActionsListener.onMenuClicked(mPost, mPost.comments.get(i), holder.toolbar.getMenu(), item);
                        }
                    }
                    return true;
                }
            });

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onCommentClicked(v, view.getTag(R.id.comment_id).toString());
                    }
                }
            });
            return holder;
        }

    }

    public void removeComment(Comment comment) {
        int index = searchCommentById(comment.id);
        if (index >= 0) {
            mPost.comments.remove(index);
            notifyItemRemoved(index + 1);//+1 beacuse of post in beginning
        }
    }

    public void notifyCommentChanged(Comment comment) {
        int index = searchCommentById(comment.id);
        if (index >= 0)
            notifyItemChanged(index + 1);//+1 beacuse of post in beginning
    }


    private Context getContext() {
        return mContext.get();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        if (i == 0) {
            PostViewHolder postHolder = (PostViewHolder) holder;
            postHolder.author.setText("@" + mPost.post.author.login);
            postHolder.text.setText(mPost.post.text.text);
            postHolder.imageList.setImageUrls(mPost.post.text.images, mPost.post.files);
            Utils.showAvatar(mPost.post.author.login, mPost.post.author.avatar, postHolder.avatar);
            postHolder.date.setText(Utils.formatDate(mPost.post.created));

            postHolder.post_id.setText("#" + mPost.post.id);
            postHolder.post_id.setTag(mPost.post.id);
            postHolder.favourite.setTag(mPost.post.id);
            postHolder.webLink.setTag(Utils.generateSiteUri(mPost.post.id));

            if (mPost.post.comments_count > 0) {
                postHolder.comments.setText(String.valueOf(mPost.post.comments_count));
            } else {
                postHolder.comments.setText("");
            }
            postHolder.tags.removeAllViews();
            if (mPost.post.tags == null || mPost.post.tags.size() == 0) {
                postHolder.tags.setVisibility(View.GONE);
            } else {
                postHolder.tags.setVisibility(View.VISIBLE);
                int n = 0;
                for (String tag : mPost.post.tags) {
                    LayoutInflater li;
                    li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    final TextView v = (TextView) li.inflate(R.layout.tag, postHolder.tags, false);
                    v.setText(tag);
                    postHolder.tags.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    v.setOnClickListener(mOnTagClickListener);
                }
            }
        } else {
            Comment comment = mPost.comments.get(i - 1);
            CommentViewHolder commentHolder = (CommentViewHolder) holder;
            commentHolder.itemView.setTag(R.id.comment_id, comment.id);
            if (mOnPostActionsListener != null) {
                mOnCommentActionsListener.updateMenu(commentHolder.toolbar.getMenu(), commentHolder.shareActionProvider, comment);
            }
            Utils.showAvatar(comment.author.login, comment.author.avatar, commentHolder.avatar);
            if (i == 1) {
                commentHolder.divider.setVisibility(View.INVISIBLE);
            } else {
                commentHolder.divider.setVisibility(View.VISIBLE);
            }
            commentHolder.date.setText(Utils.formatDate(comment.created));
            commentHolder.text.setText(comment.text.text);
            commentHolder.imageList.setImageUrls(comment.text.images, comment.files);
            commentHolder.author.setText("@" + comment.author.login);
            if (TextUtils.isEmpty(comment.to_comment_id))
                commentHolder.comment_id.setText("/" + comment.id);
            else
                commentHolder.comment_id.setText("/" + comment.id + " → " + "/" + comment.to_comment_id);
        }
    }

    @Override
    public int getItemCount() {
        if (mPost == null)
            return 0;
        else if (mPost.comments == null)
            return 1;
        else
            return mPost.comments.size() + 1;
    }

    protected class PostViewHolder extends RecyclerView.ViewHolder {
        final TextView text;
        final ViewGroup tags;
        final ImageView avatar;
        final TextView author;
        final TextView post_id;
        final TextView comments;
        final TextView date;
        final ImageView webLink;
        final CheckBox favourite;
        final View mainContent;
        final ImageList imageList;

        public PostViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            tags = (ViewGroup) itemView.findViewById(R.id.tags);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            author = (TextView) itemView.findViewById(R.id.author);
            post_id = (TextView) itemView.findViewById(R.id.post_id);
            comments = (TextView) itemView.findViewById(R.id.comments);
            Utils.setTint(comments);
            date = (TextView) itemView.findViewById(R.id.date);
            webLink = (ImageView) itemView.findViewById(R.id.weblink);
            favourite = (CheckBox) itemView.findViewById(R.id.favourite);
            Utils.setTint(favourite);
            mainContent = itemView.findViewById(R.id.main_content);
            imageList = (ImageList) itemView.findViewById(R.id.imageList);
        }
    }

    protected class CommentViewHolder extends RecyclerView.ViewHolder {
        final TextView text;
        final ImageView avatar;
        final TextView author;
        final TextView date;
        final View divider;
        final TextView comment_id;
        final ImageList imageList;
        final Toolbar toolbar;
        final ShareActionProvider shareActionProvider;

        public CommentViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            author = (TextView) itemView.findViewById(R.id.author);
            date = (TextView) itemView.findViewById(R.id.date);
            divider = itemView.findViewById(R.id.divider);
            comment_id = (TextView) itemView.findViewById(R.id.comment_id);
            imageList = (ImageList) itemView.findViewById(R.id.imageList);
            toolbar = (Toolbar) itemView.findViewById(R.id.comment_toolbar);
            shareActionProvider = new ShareActionProvider(itemView.getContext());
        }
    }

    private class ImageSearchTask extends AsyncTask<Post, Integer, Void> {
        SharedPreferences prefs;
        boolean loadImages;

        @Override
        protected Void doInBackground(Post... posts) {
            Post post = posts[0];
            post.post.text.images = ImageSearchHelper.checkImageLinks(ImageSearchHelper.getAllLinks(post.post.text.text));
            publishProgress(-1);
            if (post.comments != null) {
                for (int i = 0; i < post.comments.size(); i++) {
                    Comment comment = post.comments.get(i);
                    comment.text.images = ImageSearchHelper.checkImageLinks(ImageSearchHelper.getAllLinks(comment.text.text));
                    publishProgress(i);
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            prefs = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
            loadImages = prefs.getBoolean("loadImages", true);
            if (!loadImages) cancel(true);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            notifyItemChanged(values[0] + 1);
            super.onProgressUpdate(values);
        }
    }

    public interface OnItemClickListener {
        void onCommentClicked(View v, String comment_id);

        void onPostClicked(View v);
    }

    public void setOnCommentClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnPointClickListener(OnPointClickListener onPointClickListener) {
        mOnPointClickListener = onPointClickListener;
    }

    public void setOnPostActionsListener(OnPostActionsListener postActionsListener) {
        mOnPostActionsListener = postActionsListener;
    }

    public void setOnCommentActionsListener(OnCommentActionsListener commentActionsListener) {
        mOnCommentActionsListener = commentActionsListener;
    }

}
