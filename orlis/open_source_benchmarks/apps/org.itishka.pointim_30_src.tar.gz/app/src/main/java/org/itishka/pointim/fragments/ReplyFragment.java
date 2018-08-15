package org.itishka.pointim.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.itishka.pointim.R;
import org.itishka.pointim.adapters.UserCompletionAdapter;
import org.itishka.pointim.model.point.Comment;
import org.itishka.pointim.model.point.PointResult;
import org.itishka.pointim.model.point.Post;
import org.itishka.pointim.model.point.User;
import org.itishka.pointim.model.point.UserList;
import org.itishka.pointim.network.PointConnectionManager;
import org.itishka.pointim.network.requests.UserSubscriptionsRequest;
import org.itishka.pointim.widgets.ImageUploadingPanel;
import org.itishka.pointim.widgets.SymbolTokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Tishka17 on 04.05.2016.
 */
public class ReplyFragment extends SpicedFragment {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final java.lang.String ARG_POST = "post";
    private static final String ARG_DIALOG = "dialog";
    private static final String ARG_COMMENT = "comment";
    private TextView mCommentId;
    private MultiAutoCompleteTextView mText;
    private UserCompletionAdapter mUsersListAdapter;
    private ImageButton mSendButton;
    private ImageUploadingPanel mImagesPanel;
    private ImageButton mAttachButton;
    private String mPost;

    public ReplyFragment() {
        setArguments(new Bundle());
    }

    private RequestListener<UserList> mUsersRequestListener = new RequestListener<UserList>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            //
        }

        @Override
        public void onRequestSuccess(UserList users) {
            Log.d("SinglePostFragment", "users: " + users);
            if (users != null) {
                for (User u : users)
                    mUsersListAdapter.addIfAbsent(u);
                mUsersListAdapter.notifyDataSetChanged();
            }
        }
    };
    private OnReplyListener mOnReplyListener = null;
    private Callback<PointResult> mCommentCallback = new Callback<PointResult>() {
        @Override
        public void success(PointResult post, Response response) {
            getView().setEnabled(true);
//            hideDialog(); // FIXME: 04.05.2016
            if (post.isSuccess()) {
                mCommentId.setVisibility(View.GONE);
                mCommentId.setText("");
                mText.setText("");
                mImagesPanel.reset();
                if (!isDetached()) {
                    if (mOnReplyListener != null) {
                        mOnReplyListener.onReplied();
                    }
                    Toast.makeText(getActivity(), getString(R.string.toast_commented), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (!isDetached())
                    Toast.makeText(getActivity(), post.error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            getView().setEnabled(true);
//            hideDialog(); // FIXME: 04.05.2016
            if (!isDetached())
                Toast.makeText(getActivity(), error.toString() + "\n\n" + error.getCause(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        mUsersListAdapter = new UserCompletionAdapter(getContext());

        mCommentId = (TextView) rootView.findViewById(R.id.comment_id);
        mCommentId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCommentId.setText("");
                mCommentId.setVisibility(View.GONE);
            }
        });
        mText = (MultiAutoCompleteTextView) rootView.findViewById(R.id.text);
        mText.setInputType(mText.getInputType() & ~EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE | EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT);
        mText.setAdapter(mUsersListAdapter);
        mText.setTokenizer(new SymbolTokenizer('@'));
        if (getArguments() != null && getArguments().getBoolean(ARG_DIALOG, false)) {
            Long comment = getArguments().getLong(ARG_COMMENT, -1);
            if (comment > 0)
                setCommentId(String.valueOf(comment));
            mText.requestFocus();
            mText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mText, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 1);
        }

        mImagesPanel = (ImageUploadingPanel) rootView.findViewById(R.id.imagesPanel);
        mAttachButton = (ImageButton) rootView.findViewById(R.id.attach);
        mAttachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });

        UserSubscriptionsRequest request2 = new UserSubscriptionsRequest(PointConnectionManager.getInstance().loginResult.login);
        getSpiceManager().getFromCacheAndLoadFromNetworkIfExpired(request2, request2.getCacheName(), DurationInMillis.ONE_DAY, mUsersRequestListener);

        mSendButton = (ImageButton) rootView.findViewById(R.id.send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = mText.getText().toString();
                if (!mImagesPanel.isUploadFinished()) {
                    Toast.makeText(getActivity(), getString(R.string.toast_upload_not_finished), Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuilder sb = new StringBuilder(text);
                for (String l : mImagesPanel.getLinks()) {
                    sb.append("\n").append(l);
                }
                text = sb.toString().trim();

                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(getActivity(), getString(R.string.toast_empty_comment), Toast.LENGTH_SHORT).show();
                    return;
                }

                String comment = mCommentId.getText().toString();
                getView().setEnabled(false);
                // FIXME: 04.05.2016
//                showDialog();
                if (TextUtils.isEmpty(comment)) {
                    PointConnectionManager.getInstance().pointIm.addComment(mPost, text, mCommentCallback);
                } else {
                    PointConnectionManager.getInstance().pointIm.addComment(mPost, text, comment, mCommentCallback);
                }
            }
        });

        loadData();
    }

    File dataDir;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataDir = new File(context.getFilesDir(), "replies");
        dataDir.mkdir();
        for (File f : dataDir.listFiles()) {
            //delete files older than month
            if (f.lastModified() < System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000) {
                f.delete();
            }
        }
    }

    private static class SavedData {
        public String text;
        public List<String> images;
    }

    private void saveData() {
        File file = new File(dataDir, mPost);
        try {
            Gson gson = new Gson();
            JsonWriter writer = new JsonWriter(new FileWriter(file, false));
            SavedData data = new SavedData();
            data.text = mText.getText().toString();
            data.images = mImagesPanel.getLinks();
            gson.toJson(data, SavedData.class, writer);
            mText.setText(data.text);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        File file = new File(dataDir, mPost);
        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(file));
            SavedData data = gson.fromJson(reader, SavedData.class);
            if (data != null) {
                mText.setText(data.text);
                for (String img : data.images) {
                    mImagesPanel.addUploadedImage(img);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reply, container, false);
    }

    public static ReplyFragment newInstance(@NonNull String post) {
        ReplyFragment fragment = new ReplyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST, post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPost = getArguments().getString(ARG_POST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            mImagesPanel.addImage(data.getData(), data.getType());
        }
    }

    public void addAuthorsToCompletion(Post pointPost) {
        if (pointPost == null)
            return;
        mUsersListAdapter.addIfAbsent(pointPost.post.author);
        for (Comment c : pointPost.comments) {
            mUsersListAdapter.addIfAbsent(c.author);
        }
        mUsersListAdapter.notifyDataSetChanged();
    }

    public void setCommentId(String commentId) {
        mCommentId.setText(commentId);
        mCommentId.setVisibility(TextUtils.isEmpty(commentId) ? View.GONE : View.VISIBLE);
    }

    public void setOnReplyListener(OnReplyListener onReplyListener) {
        mOnReplyListener = onReplyListener;
    }

    public static ReplyFragment newInstanceForDialog(@NonNull String post) {
        ReplyFragment fragment = newInstance(post);
        fragment.getArguments().putBoolean(ARG_DIALOG, true);
        return fragment;
    }

    public interface OnReplyListener {
        void onReplied();
    }
}
