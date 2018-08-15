package co.loubo.icicle;

import java.io.IOException;
import java.io.InputStream;

import net.pterodactylus.fcp.SSKKeypair;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
 
public class UploadActivity extends ActionBarActivity {
 
	private static final int SELECT_FILE = 0;
    private static final int SELECT_FILE_KITKAT = 1;
    private ImageButton thumbnail;
	private FileUploadMessage fileUploadMessage;
	private GlobalState gs;
	private SSKKeypair anSSKey;
    private Uri selectedFileUri;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload);
        // Set up the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        // setHasOptionsMenu(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        thumbnail = (ImageButton) findViewById(R.id.thumbnail);
        this.gs = (GlobalState) getApplication();
        this.fileUploadMessage = new FileUploadMessage();
        new GetSSKeypairTask().execute("");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String strUploadKeyType = sharedPref.getString(Constants.PREF_UPLOAD_KEY, Constants.KEY_TYPE_DEFAULT);
        RadioButton radioButton;
        ImageButton uploadButton = (ImageButton) this.findViewById(R.id.file_upload_button);
		if(strUploadKeyType.equals(Constants.KEY_TYPE_SSK)){
			radioButton = (RadioButton) this.findViewById(R.id.radio_button_SSK);
			radioButton.setChecked(true);
			uploadButton.setEnabled(anSSKey != null);
		}else{
			radioButton = (RadioButton) this.findViewById(R.id.radio_button_CHK);
			radioButton.setChecked(true);
			uploadButton.setEnabled(true);
		}
        if(savedInstanceState != null){
            selectedFileUri = savedInstanceState.getParcelable(Constants.SELECTED_URI);
            if(selectedFileUri != null){
                onFileSelected();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(selectedFileUri != null) {
            outState.putParcelable(Constants.SELECTED_URI, selectedFileUri);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        this.gs.registerActivity(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        this.gs.unregisterActivity(this);
        super.onStop();
    }
    
    public void pickFile(View view) {
        if (Build.VERSION.SDK_INT <19){
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_file)),SELECT_FILE);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, SELECT_FILE_KITKAT);
        }
    }
    
    public void updateKeyType(View view){
    	RadioButton chk_rb = (RadioButton) this.findViewById(R.id.radio_button_CHK);
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    	Editor editor = sharedPref.edit();
    	ImageButton uploadButton = (ImageButton) this.findViewById(R.id.file_upload_button);
    	if(chk_rb.isChecked()){
    		//User has selected CHK
    		uploadButton.setEnabled(true);
    		editor.putString(Constants.PREF_UPLOAD_KEY, Constants.KEY_TYPE_CHK);
    	}else{
    		//User has selected SSK
    		uploadButton.setEnabled(anSSKey != null);
    		editor.putString(Constants.PREF_UPLOAD_KEY, Constants.KEY_TYPE_SSK);
    	}
    	 editor.apply();
    	 if(anSSKey != null){
    		 RadioButton ssk_rb = (RadioButton) this.findViewById(R.id.radio_button_SSK);
    		 ssk_rb.setText(R.string.SSK);
    	 }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode != RESULT_OK || data == null) return;
        if (requestCode != SELECT_FILE && requestCode != SELECT_FILE_KITKAT) return;
        if (requestCode == SELECT_FILE) {
            selectedFileUri = data.getData();
        } else {
            selectedFileUri = data.getData();
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //noinspection ResourceType
                getContentResolver().takePersistableUriPermission(selectedFileUri, takeFlags);
            }
        }
        if(selectedFileUri == null) return;
        onFileSelected();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void onFileSelected(){
        ImageButton uploadButton = (ImageButton) this.findViewById(R.id.file_upload_button);
        ImageButton pickButton = (ImageButton) this.findViewById(R.id.file_picker_button);
        TextView exifRemoved = (TextView) this.findViewById(R.id.remove_exif);
        exifRemoved.setVisibility(View.INVISIBLE);
        pickButton.setVisibility(View.GONE);
        uploadButton.setVisibility(View.VISIBLE);
        thumbnail.setVisibility(View.VISIBLE);
        TextView instructions = (TextView) this.findViewById(R.id.file_upload_instructions);
        instructions.setText(R.string.file_upload_instructions_another);


        fileUploadMessage.setUri(selectedFileUri);
        //just to display the imagepath
        //Toast.makeText(this.getApplicationContext(), filemanagerstring, Toast.LENGTH_SHORT).show();
        ContentResolver cR = getApplicationContext().getContentResolver();
        fileUploadMessage.setMimeType(cR.getType(selectedFileUri));
        if(fileUploadMessage.getMimeType().startsWith("image/")){
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                InputStream is;
                is = cR.openInputStream(selectedFileUri);
                BitmapFactory.decodeStream(is,null,options);
                is.close();
                is = cR.openInputStream(selectedFileUri);
                // here w and h are the desired width and height
                options.inSampleSize = Math.max(options.outWidth/512, options.outHeight/512);
                // bitmap is the resized bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(is,null,options);
                thumbnail.setImageBitmap(bitmap);
            } catch (IOException e) {
                Bitmap b = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_photo_black_48dp);
                thumbnail.setImageBitmap(Bitmap.createScaledBitmap(b, 96, 96, false));
            }
            if(fileUploadMessage.getMimeType().equals("image/jpeg")){
                exifRemoved.setVisibility(View.VISIBLE);
            }
        }else if(fileUploadMessage.getMimeType().startsWith("video/")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(this, selectedFileUri) ) {
                final String docId = DocumentsContract.getDocumentId(selectedFileUri);
                final String strID = docId.split(":")[1];
                long id = Long.valueOf(strID);
                ContentResolver crThumb = getContentResolver();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, id, MediaStore.Video.Thumbnails.MINI_KIND, options);
                if(curThumb!= null) {
                    thumbnail.setImageBitmap(curThumb);
                }else{
                    Bitmap b = BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_videocam_black_48dp);
                    thumbnail.setImageBitmap(Bitmap.createScaledBitmap(b, 96, 96, false));
                }
            }else{
                Bitmap b = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_videocam_black_48dp);
                thumbnail.setImageBitmap(Bitmap.createScaledBitmap(b, 96, 96, false));
            }
        }else if(fileUploadMessage.getMimeType().startsWith("audio/")) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(this, selectedFileUri) ) {
                final String docId = DocumentsContract.getDocumentId(selectedFileUri);
                final String strID = docId.split(":")[1];
                long id = Long.valueOf(strID);
                String selection = MediaStore.Audio.Media._ID + " = " + id + "";

                Cursor cursor = getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{
                                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID},
                        selection, null, null);

                if (cursor.moveToFirst()) {
                    long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                    Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        InputStream is;
                        is = cR.openInputStream(albumArtUri);
                        BitmapFactory.decodeStream(is,null,options);
                        is.close();
                        is = cR.openInputStream(albumArtUri);

                        options.inSampleSize = Math.max(options.outWidth/512, options.outHeight/512);
                        // bitmap is the resized bitmap
                        Bitmap bitmap = BitmapFactory.decodeStream(is,null,options);
                        thumbnail.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Bitmap b = BitmapFactory.decodeResource(getResources(),
                                R.drawable.ic_headset_black_48dp);
                        thumbnail.setImageBitmap(Bitmap.createScaledBitmap(b, 96, 96, false));
                    }
                }else{
                    Bitmap b = BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_headset_black_48dp);
                    thumbnail.setImageBitmap(Bitmap.createScaledBitmap(b, 96, 96, false));
                }
                cursor.close();
            }else{
                Bitmap b = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_headset_black_48dp);
                thumbnail.setImageBitmap(Bitmap.createScaledBitmap(b, 96, 96, false));
            }
        }else{
            thumbnail.setImageResource(R.drawable.ic_insert_drive_file_black_48dp);
        }
        Cursor returnCursor =
                getContentResolver().query(selectedFileUri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        fileUploadMessage.setName(returnCursor.getString(nameIndex));
        fileUploadMessage.setSize(returnCursor.getLong(sizeIndex));
        returnCursor.close();
    }
    
    public void uploadFile(View view) {
    	try {
			executeMultipartPost();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void executeMultipartPost() throws Exception {
        RadioButton chk_rb = (RadioButton) this.findViewById(R.id.radio_button_CHK);
        if(chk_rb.isChecked()){
            fileUploadMessage.setKey(Constants.KEY_TYPE_CHK);
        }else{
            fileUploadMessage.setKey(anSSKey.getInsertURI()+fileUploadMessage.getName());
        }
		try {
			this.gs.getQueue().put(Message.obtain(null, 0, Constants.MsgFileUpload,0,fileUploadMessage));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(Activity.RESULT_OK);
		finish();
	}
    

    private class GetSSKeypairTask extends AsyncTask<String, Void, SSKKeypair> {
        /** The system calls this to perform work in a worker thread and
          * delivers it the parameters given to AsyncTask.execute() */
        protected SSKKeypair doInBackground(String... urls) {
            return gs.getSSKKeypair();
        }
        
        /** The system calls this to perform work in the UI thread and delivers
          * the result from doInBackground() */
        protected void onPostExecute(SSKKeypair result) {
        	anSSKey = result;
        	updateKeyType(null);
        }
    }
}