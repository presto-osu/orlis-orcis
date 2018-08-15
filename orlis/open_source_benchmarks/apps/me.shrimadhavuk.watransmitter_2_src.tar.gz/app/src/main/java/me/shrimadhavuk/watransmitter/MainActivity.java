package me.shrimadhavuk.watransmitter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView messageText;
    Button uploadButton;
    EditText txt;

    int serverResponseCode = 0;

    ProgressDialog dialog = null;
    String upLoadServerUri = null;

    private static final int READ_REQUEST_CODE = 42;
    private static final String TAG = "spechide";

    String uploadFilePath = "";
    String uploadFileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        uploadButton = (Button) findViewById(R.id.button);
        messageText = (TextView) findViewById(R.id.textView);
        txt = (EditText) findViewById(R.id.editText);
        //messageText.setText("Uploading file path :- "+uploadFilePath+"");

        /************* Php script path ****************/
        upLoadServerUri = "https://projects.shrimadhavuk.me/WhatsAppTransmitter/put.php";
        /************* Php script path ****************/

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(MainActivity.this, "", "Uploading file... \n . . . Please wait", true);
                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                messageText.setText("VERBOSE OUTPUT\nuploading started.....");
                            }
                        });
                        uploadFile(uploadFilePath);
                    }
                }).start();
            }
        });
    }

    public int realUploadThing(HttpURLConnection conn, DataOutputStream dos, File sourceFile, String fileName, String lineEnd, String twoHyphens, String boundary, int maxBufferSize) {
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        try {
            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            // send PHP POST request with the given filename
            conn.setRequestProperty("uploaded_file", fileName);
            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            final StringBuilder responseOutput = new StringBuilder();
            while((line = br.readLine()) != null ) {
                responseOutput.append(line);
            }
            br.close();
            final String serverResponseMessage = conn.getResponseMessage();
            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            if (serverResponseCode == 200) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String msg = txt.getText().toString() + "\r\n Please view it by clicking here: " + responseOutput.toString() + " \r\nSent using https://play.google.com/store/apps/details?id=me.shrimadhavuk.watransmitter";
                        //messageText.setText(msg);
                        Toast.makeText(MainActivity.this, "File Upload Complete.",
                                Toast.LENGTH_SHORT).show();
                        txt.setText("");
                        whatsappintent(msg);

                    }
                });
            }
            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();
            return serverResponseCode;
        } catch (MalformedURLException ex) {
            dialog.dismiss();
            ex.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("VERBOSE OUTPUT\nMalformedURLException Exception : check script url.");
                    Toast.makeText(MainActivity.this, "MalformedURLException",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            return -1;
        } catch (Exception e) {
            dialog.dismiss();
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("VERBOSE OUTPUT\nupload to server exception");
                    Toast.makeText(MainActivity.this, "Exception",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("Upload Exception", "Exception : " + e.getMessage(), e);
            return -1;
        }
    }

    public int uploadFile(String fileName) {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(fileName);
        if (!sourceFile.isFile()) {
            dialog.dismiss();
            Log.e("uploadFile", "Source File not exist :" + uploadFilePath);
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("VERBOSE OUTPUT\nSource File does not exist :" + uploadFilePath);
                }
            });
            return 0;
        } /*else if (fileName.endsWith(".jpg") || fileName.endsWith(".avi") || fileName.endsWith(".mp3") || fileName.endsWith(".png") || fileName.endsWith(".mp4")) {
            dialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("VERBOSE OUTPUT\nthe service is provided in the hope that it will be useful.\n please do not misuse the service.");
                }
            });
            return 0;
        }*/ else {
            int serverResponseCode = realUploadThing(conn, dos, sourceFile, fileName, lineEnd, twoHyphens, boundary, maxBufferSize);
            if (serverResponseCode == 200)
                dialog.dismiss();
            return serverResponseCode;
        }
    }

    public void whatsappintent(String msg) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }

    public void filechooserPfm(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                Log.i(TAG, "name : " + uri.getLastPathSegment());
                uploadFilePath = getRealPathFromURI(MainActivity.this, uri);
                uploadFileName = uri.getLastPathSegment();
                messageText.setText("VERBOSE OUTPUT\nfile path :- " + uploadFilePath + "");
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /** Called when the activity is about to become visible. */
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "The onStart() event");
    }

    /** Called when the activity has become visible. */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "The onResume() event");
    }

    /** Called when another activity is taking focus. */
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "The onPause() event");
    }

    /** Called when the activity is no longer visible. */
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "The onStop() event");
    }

    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "The onDestroy() event");
    }

}
