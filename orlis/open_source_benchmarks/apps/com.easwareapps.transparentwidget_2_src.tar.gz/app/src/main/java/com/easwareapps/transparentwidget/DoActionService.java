package com.easwareapps.transparentwidget;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DoActionService extends IntentService {


    Context context;

    public DoActionService() {
        super("DoActionService");
    }
    @Override
    public void onCreate() {
        context = getApplicationContext();
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            int action;
            try {
                action = Integer.parseInt(intent.getAction());
            }catch (Exception e){
                action = -1;
            }
            if (DoActionActivity.TOGGLE_FLASH == action) {
                if(!FlashSettings.flashOn){
                    try {
                        FlashSettings.turnOnFlash();
                    }catch (Exception e){

                    }
                }else{
                    try {
                        FlashSettings.turnOffFlash();
                    }catch (Exception e){

                    }
                }
            }
        }
    }


}
