package io.github.phora.androptpb.network;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by phora on 8/27/15.
 */
public class RequestData {
    private final static String LOG_TAG = "RequestData";

    public final static String crlf = "\r\n";
    public final static String hyphens = "--";
    public final static String boundary = "------------------------8977674a05eb9620";

    public final static String NO_MIME_TYPE = "no/mime/type";

    private DataOutputStream request;
    private ContentResolver cr;

    public RequestData(DataOutputStream request, ContentResolver cr) {
        this.request = request;
        this.cr = cr;
    }

    public void addPrivacy() throws IOException {
        request.writeBytes("Content-Disposition: form-data; name=\"p\"" + crlf);
        request.writeBytes(crlf);
        request.writeBytes("1" + crlf);
        request.writeBytes(hyphens + boundary + crlf);
    }

    public void addSunset(long sunset) throws IOException {
        request.writeBytes("Content-Disposition: form-data; name=\"s\"" + crlf);
        request.writeBytes(crlf);
        request.writeBytes(sunset + crlf);
        request.writeBytes(hyphens + boundary + crlf);
    }

    public void addRawData(byte[] bArray) throws IOException {
        addRawData(bArray, null, "application/octet-stream");
    }

    public void addRawData(byte[] bArray, String fname) throws IOException {
        addRawData(bArray, fname, "application/octet-stream");
    }

    public void addRawData(byte[] bArray, String fname, String mimetype) throws IOException {
        if (mimetype == null) {
            mimetype = "application/octet-stream";
        }
        Log.d(LOG_TAG, "Uploading " + fname);

        if (fname == null) {
            request.writeBytes("Content-Disposition: form-data; name=\"c\"" + crlf);
        }
        else {
            request.writeBytes("Content-Disposition: form-data; name=\"c\"; filename=\"" + fname + "\"" + crlf);
        }
        if (!mimetype.equals(NO_MIME_TYPE)) {
            request.writeBytes(String.format("Content-Type: %s", mimetype) + crlf);
        }
        request.writeBytes(crlf);
        //request.flush();

        //write image data
        request.write(bArray);
        Log.d(LOG_TAG, "Got data? " + (bArray != null));

        //finish the format http post packet
        request.writeBytes(crlf);
        request.writeBytes(hyphens + boundary + hyphens + crlf);
        //oldflush
    }

    // http://stackoverflow.com/questions/566462/upload-files-with-httpwebrequest-multipart-form-data
    public void addFile(Uri fpath) throws IOException {
        //setup filename and say that octets follow
        Cursor fileInfo = cr.query(fpath, null, null, null, null);
        fileInfo.moveToFirst();
        String fname = fileInfo.getString(fileInfo.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        //long file_length = fileInfo.getLong(fileInfo.getColumnIndex(OpenableColumns.SIZE));
        fileInfo.close();
        String mimetype = cr.getType(fpath);


        InputStream instream = cr.openInputStream(fpath);
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        byte[] bArray = null;

        int readed = 0;
        byte[] buffer = new byte[1024];
        while(readed != -1) {
            try {
                readed = instream.read(buffer);
                if(readed != -1)
                    outstream.write(buffer,0,readed);
            } catch (IOException e) {
                e.printStackTrace();
                readed = -1;
            }
        }
        bArray = outstream.toByteArray();
        outstream.close();
        instream.close();

        addRawData(bArray, fname, mimetype);
    }
}
