package mq;

import java.util.Locale;

import misc.tracerengine;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

class Speaker implements OnInitListener {
    private tracerengine Tracer = null;
    private final String mytag = this.getClass().getName();

    private final TextToSpeech tts;

    private boolean ready = false;

    private boolean allowed = false;

    public Speaker(Context context) {
        tts = new TextToSpeech(context, this);
        SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(context);
        Tracer = tracerengine.getInstance(SP_params, context);

    }

    public boolean isAllowed() {
        return allowed;
    }

    public void allow(boolean allowed) {
        this.allowed = allowed;
    }

    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Change this to match your
            // locale
            tts.setLanguage(new Locale(Locale.getDefault().getISO3Language(), Locale.getDefault().getISO3Country()));
            ready = true;
        } else {
            ready = false;
            Tracer.e(mytag, "Unable to set TTS to ready (check your locales)");
        }
    }

    public void speak(String text) {

        // Speak only if the TTS is ready
        // and the user has allowed speech

        if (ready && allowed) {
            //			Log.e(this.getClass().getSimpleName(),"Try tts "+text);
            //			HashMap<String, String> hash = new HashMap<String,String>();
            //			hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
            //					String.valueOf(AudioManager.STREAM_NOTIFICATION));
            //tts.speak(text, TextToSpeech.QUEUE_ADD, hash);
            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    public void pause(int duration) {
        tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }

    // Free up resources
    public void destroy() {
        tts.shutdown();
    }

}

