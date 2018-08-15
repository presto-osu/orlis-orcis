package de.baumann.sieben.helper;


import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TTSManager {

    private TextToSpeech mTts = null;
    private boolean isLoaded = false;

    public void init(Context context) {
        try {
            mTts = new TextToSpeech(context, onInitListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                mTts.setLanguage(Locale.getDefault());
                isLoaded = true;
            }
        }
    };

// --Commented out by Inspection START (31.03.16 09:38):
//    public void shutDown() {
//        mTts.shutdown();
//    }
// --Commented out by Inspection STOP (31.03.16 09:38)

// --Commented out by Inspection START (28.03.16 22:50):
//    public void addQueue(String text) {
//        if (isLoaded) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
//            } else {
//                mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//            }
//        }
//        else
//            Log.e("error", "TTS Not Initialized");
//    }
// --Commented out by Inspection STOP (28.03.16 22:50)

    public void initQueue(String text) {
        if (isLoaded) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        else
            Log.e("error", "TTS Not Initialized");
    }
}
