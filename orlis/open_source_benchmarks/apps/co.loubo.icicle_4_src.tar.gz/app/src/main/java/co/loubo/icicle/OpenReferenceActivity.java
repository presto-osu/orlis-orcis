package co.loubo.icicle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import net.pterodactylus.fcp.AddPeer;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OpenReferenceActivity extends ActionBarActivity implements NfcAdapter.CreateNdefMessageCallback {

	public GlobalState gs;
	private AddPeer aPeer;
    private String nodeRef;
    private Spinner lnTrust;
    private Spinner lnVisibility;
	
    private String encodedNodeRef;
    NfcAdapter mNfcAdapter;
    // Flag to indicate that Android Beam is available
    boolean mAndroidBeamAvailable  = false;
    private Bundle mSavedInstanceState;
    private static final String STATE_TRUST = "trust";
    private static final String STATE_VISIBILITY = "visibility";

    protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
	    setContentView(R.layout.activity_open_reference);
		this.gs = (GlobalState) getApplication();

        // Set up the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        // setHasOptionsMenu(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // NFC is available on the device
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)
                //MIME-typed NFC NdefRecords are only supported in API 16
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mAndroidBeamAvailable = true;
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }


	}

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            //opening friend node from NFC
            this.nodeRef = processNFCIntent(intent);
            if(mSavedInstanceState != null){
                aPeer.setField("Trust", mSavedInstanceState.getString(STATE_TRUST,Constants.DEFAULT_TRUST));
                aPeer.setField("Visibility", mSavedInstanceState.getString(STATE_VISIBILITY,Constants.DEFAULT_VISIBILITY));
            }
            if(this.gs.isConnected()) {
                findViewById(R.id.addNodeRef).setVisibility(View.VISIBLE);
            }else {
                findViewById(R.id.saveNodeRef).setVisibility(View.VISIBLE);
            }
            setupSpinners();

        }else if (Intent.ACTION_VIEW.equals(action) && type != null) {
            //opening friend node from intent
            this.nodeRef = handleSendText(intent); // Handle text being sent
            if(mSavedInstanceState != null){
                aPeer.setField("Trust", mSavedInstanceState.getString(STATE_TRUST,Constants.DEFAULT_TRUST));
                aPeer.setField("Visibility", mSavedInstanceState.getString(STATE_VISIBILITY,Constants.DEFAULT_VISIBILITY));
            }
            if(this.gs.isConnected()) {
                findViewById(R.id.addNodeRef).setVisibility(View.VISIBLE);
            }else {
                findViewById(R.id.saveNodeRef).setVisibility(View.VISIBLE);
            }
            setupSpinners();
        } else {
            //opening own node
            findViewById(R.id.trust_visibility_title_row).setVisibility(View.GONE);
            findViewById(R.id.trust_visibility_row).setVisibility(View.GONE);
            int selected = intent.getIntExtra(Constants.LOCAL_NODE_SELECTED,-1);
            if(selected >= 0){
                this.nodeRef = this.gs.getLocalNodeList().get(selected).getNodeReference();
                this.encodedNodeRef = this.gs.getLocalNodeList().get(selected).getEncodedNodeReference();
                findViewById(R.id.shareNodeRef).setVisibility(View.VISIBLE);

                if(mAndroidBeamAvailable){
                    mNfcAdapter.setNdefPushMessageCallback(this, this);
                }
            }
        }
        if (this.nodeRef != null) {
            TextView textView = (TextView) findViewById(R.id.NodeRef_value);
            textView.setText(this.nodeRef);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        if(aPeer != null) {
            savedInstanceState.putString(STATE_TRUST, aPeer.getField("Trust"));
            savedInstanceState.putString(STATE_VISIBILITY, aPeer.getField("Visibility"));
        }
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupSpinners() {
        this.lnTrust =(Spinner) findViewById(R.id.trust_spinner);
        this.lnVisibility =(Spinner) findViewById(R.id.visibility_spinner);
        ArrayAdapter<String> adapterT = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Constants.TrustValues);
        this.lnTrust.setAdapter(adapterT);
        ArrayAdapter<String> adapterV = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Constants.VisibilityValues);
        this.lnVisibility.setAdapter(adapterV);
        this.lnTrust.setSelection(Constants.TrustValues.indexOf(this.aPeer.getField("Trust")));
        this.lnVisibility.setSelection(Constants.VisibilityValues.indexOf(this.aPeer.getField("Visibility")));

        this.lnTrust.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                aPeer.setField("Trust", lnTrust.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // do nothing
            }

        });

        this.lnVisibility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                aPeer.setField("Visibility", lnVisibility.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // do nothing
            }

        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

	@Override
	public void onDestroy(){
		super.onDestroy();
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Because app crashes sometimes without the try->catch
        try {
            File refDir = new File(getExternalFilesDir(null), "fref");
            if(refDir.exists()){
                //delete shared FREF file
                clearFolder(refDir);
            }
        } catch (Exception ignored) {
        }

    }


    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    public String processNFCIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        //textView.setText(new String(msg.getRecords()[0].getPayload()));
        String str = new String(msg.getRecords()[0].getPayload());
        this.aPeer = this.gs.processStringIntoNode(str);
        return str;
    }

	private String handleSendText(Intent intent) {
		Uri uri = intent.getData();
		BufferedReader in;
		StringBuilder sb = new StringBuilder(10000);
        String tempstr;
		try {
			in = new BufferedReader(new InputStreamReader( getContentResolver().openInputStream(uri)));
	        
	        
	        while ((tempstr = in.readLine()) != null) {
                sb.append(tempstr).append("\n");

            }
            in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
        String str = sb.toString();
        this.aPeer = this.gs.processStringIntoNode(str);
        return str;
	}



	public void cancelReference(View view) {
		finish();
	}

    public void saveReference(View view) {
        saveNodeRef();
        Toast.makeText(this, R.string.savingNodeRef, Toast.LENGTH_SHORT).show();
        finish();
    }

	public void addReference(View view) {
		try {
            saveNodeRef();
			this.gs.getQueue().put(Message.obtain(null, 0, Constants.MsgAddNoderef,0,this.aPeer));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		setResult(Activity.RESULT_OK);
		Toast.makeText(this, R.string.addingNodeRef, Toast.LENGTH_SHORT).show();
		finish();
	}

    public void saveNodeRef(){
        FriendNode ref = new FriendNode(this.aPeer.getField("myName"),this.aPeer.getField("identity"),this.aPeer.getField("Trust"),this.aPeer.getField("Visibility"),this.nodeRef);
        this.gs.addFriendNode(ref);
    }

    public void shareReference(View view) {
        startActivityForResult(shareReference(),1);
    }

    public Intent shareReference(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        File outFile = copyFileToInternal();
        if(outFile == null) return null;
        Uri uri = Uri.fromFile(outFile);

        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        outFile.deleteOnExit();
        return shareIntent;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File copyFileToInternal() {
        InputStream is;
        OutputStream os;
        try {
            is = new ByteArrayInputStream(encodedNodeRef.getBytes());
            File refDir = new File(getExternalFilesDir(null), "fref");
            if(!refDir.exists()){
                refDir.mkdirs();
            }
            clearFolder(refDir);
            //Save to a random location, to prevent guess location of ref
            File outFile = new File(refDir, "myref.fref");
            if(outFile.createNewFile()){
                os = new FileOutputStream(outFile.getAbsolutePath());

                byte[] buff = new byte[1024];
                int len;
                while ((len = is.read(buff)) > 0) {
                    os.write(buff, 0, len);
                }
                os.flush();
                os.close();
                is.close();
            }
            outFile.setReadable(true, false);
            return outFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void clearFolder(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        return new NdefMessage(
                new NdefRecord[] { NdefRecord.createMime(
                        "application/vnd.co.loubo.icicle", this.encodedNodeRef.getBytes(Charset.forName("US-ASCII")))
                        /**
                         * The Android Application Record (AAR) is commented out. When a device
                         * receives a push with an AAR in it, the application specified in the AAR
                         * is guaranteed to run. The AAR overrides the tag dispatch system.
                         * You can add it back in to guarantee that this
                         * activity starts when receiving a beamed message. For now, this code
                         * uses the tag dispatch system.
                         */
                        //,NdefRecord.createApplicationRecord("com.example.android.beam")
                });
    }
}
