package com.luorrak.ouroboros.reply;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.api.JsonParser;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.Reply;
import com.luorrak.ouroboros.util.SaveReplyText;
import com.luorrak.ouroboros.util.SettingsHelper;
import com.luorrak.ouroboros.util.Util;

import java.util.ArrayList;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ReplyCommentFragment extends Fragment {
    private static boolean isPosting;
    private String resto;
    private String boardName;
    private String replyNo;
    private SharedPreferences sharedPreferences;
    private NetworkHelper networkHelper;
    private final int FILE_SELECT_CODE = 1;
    private Reply reply;

    public ReplyCommentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isPosting = false;
        View view = inflater.inflate(R.layout.fragment_post_comment_activity, container, false);
        setActionBarTitle("Post a comment");

        networkHelper = new NetworkHelper();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        reply = new Reply();
        if (savedInstanceState == null){
            reply.filePath = new ArrayList<>();
        } else {
            reply.filePath = savedInstanceState.getStringArrayList("filePath");
            for (String file : reply.filePath){
                addAttachmentPreview(file, view);
            }
        }
        reply.fileName = new ArrayList<String>();

        resto = getActivity().getIntent().getStringExtra(Util.INTENT_THREAD_NO);
        boardName = getActivity().getIntent().getStringExtra(Util.INTENT_BOARD_NAME);
        replyNo = getActivity().getIntent().getStringExtra(Util.INTENT_REPLY_NO);

        EditText nameText = (EditText) view.findViewById(R.id.post_comment_editText_name);
        final EditText emailText = (EditText) view.findViewById(R.id.post_comment_editText_email);
        CheckBox sageBox = (CheckBox) view.findViewById(R.id.post_comment_checkBox_sage);
        EditText subjetText = (EditText) view.findViewById(R.id.post_comment_editText_subject);
        EditText commentText = (EditText) view.findViewById(R.id.post_comment_editText_comment);

        String defaultName = SettingsHelper.getDefaultName(getActivity());
        String defaultEmail = SettingsHelper.getDefaultEmail(getActivity());

        nameText.setText(sharedPreferences.getString(SaveReplyText.nameEditTextKey, defaultName));
        emailText.setText(sharedPreferences.getString(SaveReplyText.emailEditTextKey, defaultEmail));
        subjetText.setText(sharedPreferences.getString(SaveReplyText.subjectEditTextKey, ""));
        commentText.setText(sharedPreferences.getString(SaveReplyText.commentEditTextKey, ""));

        nameText.addTextChangedListener(new SaveReplyText(sharedPreferences, SaveReplyText.nameEditTextKey));
        emailText.addTextChangedListener(new SaveReplyText(sharedPreferences, SaveReplyText.emailEditTextKey));
        subjetText.addTextChangedListener(new SaveReplyText(sharedPreferences, SaveReplyText.subjectEditTextKey));
        commentText.addTextChangedListener(new SaveReplyText(sharedPreferences, SaveReplyText.commentEditTextKey));
        sageBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reply.sage = isChecked;
            }
        });

        if (replyNo != null){
            if (commentText.getText().toString().equals("")){
                commentText.append(">>" + replyNo + "\n");
            } else {
                commentText.append("\n>>" + replyNo + "\n");
            }
        }
        
        commentText.requestFocus();

        setHasOptionsMenu(true);
        return view;
    }


    private void setActionBarTitle(String title){
        getActivity().setTitle(title);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_post_comment_activity, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_attach_file){
            if (reply.filePath.size() < 5) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.REQUEST_STORAGE_PERMISSION);
                } else {
                    selectFile();
                }
            } else {
                Snackbar.make(getView(), "Maximum amount of attachments reached", Snackbar.LENGTH_LONG).show();
            }
        }

        if (id == R.id.action_submit && !isPosting){
            isPosting = true;
            ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            EditText nameText = (EditText) getActivity().findViewById(R.id.post_comment_editText_name);
            EditText emailText = (EditText) getActivity().findViewById(R.id.post_comment_editText_email);
            EditText subjectText = (EditText) getActivity().findViewById(R.id.post_comment_editText_subject);
            EditText commentText = (EditText) getActivity().findViewById(R.id.post_comment_editText_comment);
            EditText captchaText = (EditText) getActivity().findViewById(R.id.post_comment_captcha_editText);
            ImageView captchaImage = (ImageView) getActivity().findViewById(R.id.post_comment_captcha_image);



            reply.name = nameText.getText().toString();
            reply.email = emailText.getText().toString();
            reply.subject = subjectText.getText().toString();
            reply.comment = commentText.getText().toString();
            reply.captchaText = captchaText.getText().toString();

            if (captchaImage.getTag() != null){
                reply.captchaCookie = captchaImage.getTag().toString();
            }
            reply.resto = resto;
            reply.board = boardName;

            reply.password = SettingsHelper.getPostPassword(getContext());

            networkHelper.postReply(getActivity(), reply, sharedPreferences, new JsonParser(), new InfiniteDbHelper(getActivity()), getView());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("filePath", reply.filePath);
    }

    public static void finishedPosting(){
        isPosting = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Util.REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectFile();
                } else {
                    Snackbar.make(getView(), "Requires Permission", Snackbar.LENGTH_LONG).show();
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void removeFile() {
        reply.filePath.clear();
    }

    private void selectFile() {
        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat) {
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,FILE_SELECT_CODE);

        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent,FILE_SELECT_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            String filePath = getPath(getActivity(), data.getData());
            if (filePath == null){
                Snackbar.make(getView(), "Could not retrieve file", Snackbar.LENGTH_LONG).show();
            } else {
                reply.filePath.add(filePath);
                addAttachmentPreview(filePath, getView());
            }
        }
    }

    private void addAttachmentPreview(final String filePath, View layout) {
        final LinearLayout view = (LinearLayout) layout.findViewById(R.id.post_comment_container);
        final View card = View.inflate(getActivity(), R.layout.card_reply_attachment, null);
        ImageView imagePreview = (ImageView) card.findViewById(R.id.reply_attachment_image);
        TextView filePathTextView = (TextView) card.findViewById(R.id.reply_attachment_path);
        ImageButton deleteAttachment = (ImageButton) card.findViewById(R.id.reply_delete_attachment);
        Ion.with(imagePreview)
                .load(filePath)
                .withBitmapInfo();
        filePathTextView.setText(filePath);
        deleteAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reply.filePath.remove(filePath);
                view.removeView(card);
            }
        });
        view.addView(card);
    }

    //https://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework
    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
