package org.itishka.pointim.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
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

import com.pnikosis.materialishprogress.ProgressWheel;

import org.itishka.pointim.R;
import org.itishka.pointim.listeners.OnPointClickListener;
import org.itishka.pointim.listeners.OnPostActionsListener;
import org.itishka.pointim.model.point.Post;
import org.itishka.pointim.model.point.PostList;
import org.itishka.pointim.utils.ImageSearchHelper;
import org.itishka.pointim.utils.Utils;
import org.itishka.pointim.widgets.ImageList;

import java.util.List;

/**
 * Created by Tishka17 on 20.10.2014.
 */
public class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = -1;

    private PostList mPostList = null;
    private ImageSearchTask mTask;
    private OnLoadMoreRequestListener mOnLoadMoreRequestListener = null;
    private OnPointClickListener mOnPointClickListener = null;
    private View.OnClickListener mOnTagClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOnPointClickListener != null)
                mOnPointClickListener.onTagClicked(((TextView) view).getText().toString());
        }
    };
    private OnPostActionsListener mOnPostActionsListener = null;

    protected OnPointClickListener getOnPointClickListener() {
        return mOnPointClickListener;
    }

    private boolean mHasHeader = false;

    @Override
    public long getItemId(int position) {
        switch (getItemViewType(position)) {
            case TYPE_FOOTER:
                return -1;
            case TYPE_HEADER:
                return 0;
            default:
                if (mHasHeader)
                    return mPostList.posts.get(position - 1).uid;
                else
                    return mPostList.posts.get(position).uid;
        }
    }

    public PostListAdapter(Context context) {
        super();
        setHasStableIds(true);
    }

    protected void setHasHeader(boolean hasHeader) {
        mHasHeader = hasHeader;
    }

    public PostList getPostList() {
        return mPostList;
    }

    public void setData(Context context, PostList postList) {
        mPostList = postList;
        if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
            mTask.cancel(true);
        }
        mTask = new ImageSearchTask(context);
        mTask.execute(mPostList);
        notifyDataSetChanged();
    }

    public void appendData(Context context, PostList postList) {
        int oldLength = mPostList.posts.size();
        mPostList.append(postList);
        notifyItemRangeInserted(oldLength, postList.posts.size());
        if (mTask == null || mTask.getStatus() == AsyncTask.Status.FINISHED) {
            mTask = new ImageSearchTask(context);
            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mPostList);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mHasHeader)
            return TYPE_HEADER;
        if (position == mPostList.posts.size() + (mHasHeader ? 1 : 0))
            return TYPE_FOOTER;
        return TYPE_ITEM;
    }

    public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup viewGroup) {
        final View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_footer, viewGroup, false);
        return new FooterHolder(v);
    }

    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        return new RecyclerView.ViewHolder(viewGroup) {
        };
    }

    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup viewGroup) {

        final View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_post, viewGroup, false);
        final ViewHolder holder = new ViewHolder(v);
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
                                                        mOnPostActionsListener.onBookmark(getPost(holder.itemView), holder.favourite);
                                                }
                                            }
        );
        holder.recomender_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = (String) view.getTag(R.id.imageView);
                if (!TextUtils.isEmpty(user)) {
                    mOnPointClickListener.onUserClicked(user);
                }
            }
        });
        holder.toolbar.inflateMenu(R.menu.menu_adapter_post);
        MenuItem item = holder.toolbar.getMenu().findItem(R.id.menu_item_share);
        MenuItemCompat.setActionProvider(item, holder.shareActionProvider);

        holder.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (mOnPostActionsListener != null)
                    mOnPostActionsListener.onMenuClicked(getPost(holder.itemView), holder.toolbar.getMenu(), item);
                return true;
            }
        });
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnPointClickListener != null) {
                    mOnPointClickListener.onPostClicked(view.getTag(R.id.post_id).toString());
                }
            }
        });
        return holder;
    }

    private Post getPost(View itemView) {
        return mPostList.posts.get((Integer) itemView.getTag(R.id.card_view));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewTYpe) {
        if (viewTYpe == TYPE_FOOTER) {
            return onCreateFooterViewHolder(viewGroup);
        } else if (viewTYpe == TYPE_HEADER) {
            return onCreateHeaderViewHolder(viewGroup);
        } else {
            return onCreateItemViewHolder(viewGroup);
        }
    }

    public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
        FooterHolder footerHolder = (FooterHolder) holder;
        if (mPostList.has_next) {
            footerHolder.progressWheel.setVisibility(View.VISIBLE);
            if (mOnLoadMoreRequestListener != null) {
                if (!mOnLoadMoreRequestListener.onLoadMoreRequested()) {
                    footerHolder.progressWheel.setVisibility(View.GONE);
                }
            }
        } else {
            footerHolder.progressWheel.setVisibility(View.GONE);
        }
    }

    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        //do nothin
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        int type = getItemViewType(i);
        if (type == TYPE_FOOTER) {
            onBindFooterViewHolder(holder);
        } else if (type == TYPE_HEADER) {
            onBindHeaderViewHolder(holder);
        } else {
            onBindItemViewHolder((ViewHolder) holder, mHasHeader ? (i - 1) : i);
        }
    }

    public void onBindItemViewHolder(PostListAdapter.ViewHolder holder, int i) {
        Post post = mPostList.posts.get(i);
        if (mOnPostActionsListener != null) {
            mOnPostActionsListener.updateMenu(holder.toolbar.getMenu(), holder.shareActionProvider, post);
        }
        holder.author.setText("@" + post.post.author.login);
        holder.itemView.setTag(R.id.card_view, i);
        holder.itemView.setTag(R.id.post_id, post.post.id);

        holder.imageList.setImageUrls(post.post.text.images, post.post.files);
        holder.text.setText(post.post.text.text);
        Utils.showAvatar(post.post.author.login, post.post.author.avatar, holder.avatar);
        holder.date.setText(Utils.formatDate(post.post.created));

        if (post.rec != null) {
            holder.mainContent.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.quote_background));
            holder.recommend_info.setVisibility(View.VISIBLE);
            if (post.rec.text != null) {
                holder.recommend_text.setVisibility(View.VISIBLE);
                holder.recommend_text.setText(post.rec.text.text);
            } else {
                holder.recommend_text.setVisibility(View.GONE);
            }
            holder.quote_mark.setVisibility(View.VISIBLE);
            holder.quote_mark_top.setVisibility(View.VISIBLE);
            holder.recommend_author.setText("@" + post.rec.author.login);
            holder.recommend_id.setText("");
            Utils.showAvatar(post.rec.author.login, post.rec.author.avatar, holder.recomender_avatar);
        } else {
            holder.mainContent.setBackgroundColor(Color.TRANSPARENT);
            holder.recommend_info.setVisibility(View.GONE);
            holder.recommend_text.setVisibility(View.GONE);
            holder.quote_mark.setVisibility(View.INVISIBLE);
            holder.quote_mark_top.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(post.comment_id)) {
            holder.post_id.setText("#" + post.post.id);
        } else {
            holder.post_id.setText("#" + post.post.id + "/" + post.comment_id);
        }
        holder.post_id.setTag(post.post.id);
        holder.webLink.setTag(Utils.generateSiteUri(post.post.id));
        holder.favourite.setChecked(post.bookmarked);
        holder.favourite.setTag(post.post.id);

        if (post.post.comments_count > 0) {
            holder.comments.setText(String.valueOf(post.post.comments_count));
            //holder.comments.setVisibility(View.VISIBLE);
        } else {
            holder.comments.setText("");
            // holder.comments.setVisibility(View.GONE);
        }
        LayoutInflater li;
        li = (LayoutInflater) holder.itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        holder.tags.removeAllViews();
        if (post.post.tags == null || post.post.tags.size() == 0) {
            holder.tags.setVisibility(View.GONE);
        } else {
            holder.tags.setVisibility(View.VISIBLE);

            int n = 0;
            for (String tag : post.post.tags) {
                final TextView v = (TextView) li.inflate(R.layout.tag, holder.tags, false);
                v.setText(tag);
                holder.tags.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                v.setOnClickListener(mOnTagClickListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mPostList == null) return 0;
        else return mPostList.posts.size() + (mPostList.has_next ? 1 : 0) + (mHasHeader ? 1 : 0);
    }

    public void setOnLoadMoreRequestListener(OnLoadMoreRequestListener onLoadMoreRequestListener) {
        mOnLoadMoreRequestListener = onLoadMoreRequestListener;
    }

    public void setOnPointClickListener(OnPointClickListener onPointClickListener) {
        mOnPointClickListener = onPointClickListener;
    }

    public void setOnPostActionsListener(OnPostActionsListener postActionsListener) {
        mOnPostActionsListener = postActionsListener;
    }

    public void removePost(Post postData) {
        for (int i = 0; i < mPostList.posts.size(); i++) {
            if (mPostList.posts.get(i).post.id.equals(postData.post.id)) {
                mPostList.posts.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void notifyPostChanged(Post post) {
        for (int i = 0; i < mPostList.posts.size(); i++) {
            if (mPostList.posts.get(i).post.id == post.post.id) {
                notifyItemChanged((mHasHeader) ? i + 1 : i);
            }
        }
    }


    public interface OnLoadMoreRequestListener {
        boolean onLoadMoreRequested();//return false if cannot load more
    }

    protected class FooterHolder extends RecyclerView.ViewHolder {
        ProgressWheel progressWheel;

        public FooterHolder(View itemView) {
            super(itemView);
            progressWheel = (ProgressWheel) itemView.findViewById(R.id.progress_wheel);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        final TextView text;
        final ViewGroup tags;
        final ImageView avatar;
        final ImageView recomender_avatar;
        final TextView recommend_text;
        final View quote_mark;
        final View quote_mark_top;
        final TextView recommend_author;
        final TextView author;
        final TextView post_id;
        final View recommend_info;
        final TextView recommend_id;
        final TextView comments;
        final TextView date;
        final ImageView webLink;
        final CheckBox favourite;
        final ImageList imageList;
        final View mainContent;
        final Toolbar toolbar;
        final ShareActionProvider shareActionProvider;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
            tags = (ViewGroup) itemView.findViewById(R.id.tags);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            recomender_avatar = (ImageView) itemView.findViewById(R.id.recommend_avatar);
            recommend_text = (TextView) itemView.findViewById(R.id.recommend_text);
            quote_mark = itemView.findViewById(R.id.quote_mark);
            quote_mark_top = itemView.findViewById(R.id.quote_mark_top);
            recommend_author = (TextView) itemView.findViewById(R.id.recommend_author);
            author = (TextView) itemView.findViewById(R.id.author);
            post_id = (TextView) itemView.findViewById(R.id.post_id);
            recommend_info = itemView.findViewById(R.id.recommend_info);
            recommend_id = (TextView) itemView.findViewById(R.id.recommend_id);
            comments = (TextView) itemView.findViewById(R.id.comments);
            Utils.setTint(comments);
            date = (TextView) itemView.findViewById(R.id.date);
            webLink = (ImageView) itemView.findViewById(R.id.weblink);
            favourite = (CheckBox) itemView.findViewById(R.id.favourite);
            Utils.setTint(favourite);
            mainContent = itemView.findViewById(R.id.main_content);
            imageList = (ImageList) itemView.findViewById(R.id.imageList);
            toolbar = (Toolbar) itemView.findViewById(R.id.card_toolbar);
            shareActionProvider = new ShareActionProvider(itemView.getContext());
        }
    }

    private class ImageSearchTask extends AsyncTask<PostList, Integer, Void> {
        SharedPreferences prefs;
        private final boolean loadImages;

        ImageSearchTask(Context context) {
            prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
            loadImages = prefs.getBoolean("loadImages", true);
        }

        @Override
        protected Void doInBackground(PostList... postLists) {
            List<Post> posts = postLists[0].posts;
            for (int i = 0; i < posts.size(); i++) {
                Post post = posts.get(i);
                post.post.text.images = ImageSearchHelper.checkImageLinks(ImageSearchHelper.getAllLinks(post.post.text.text));
                publishProgress(i);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            if (!loadImages) cancel(true);
            super.onPreExecute();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            notifyItemChanged(values[0]);
            super.onProgressUpdate(values);
        }
    }
}
