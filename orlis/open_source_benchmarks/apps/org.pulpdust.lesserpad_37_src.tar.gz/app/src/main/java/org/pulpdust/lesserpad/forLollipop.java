package org.pulpdust.lesserpad;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toolbar;

/**
 * Created by kodakana on 15/06/29.
 */
@TargetApi(21)
public class forLollipop {
    static public void readyActionBar(View v, Activity av, int mode, int theme, boolean notshow){
        Toolbar tb = (Toolbar) v;
        tb.setContentInsetsAbsolute(0, 0);
        av.setActionBar(tb);
        ActionBar ab = av.getActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        if (mode < 1){
            if (theme > 0){
                ab.setHomeAsUpIndicator(R.drawable.ic_action_new_dark);
            } else {
                ab.setHomeAsUpIndicator(R.drawable.ic_action_new_light);
            }
            ab.setHomeActionContentDescription(R.string.label_new);

        } else {
            if (theme > 0){
                ab.setHomeAsUpIndicator(R.drawable.ic_action_done_dark);
            } else {
                ab.setHomeAsUpIndicator(R.drawable.ic_action_done_light);
            }
            ab.setHomeActionContentDescription(R.string.label_done);
        }
        if (notshow){
            ab.hide();
        }
        Window win = av.getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (theme > 0){
            win.setStatusBarColor(Color.parseColor("#242424"));
        } else {
            win.setStatusBarColor(Color.parseColor("#31009C"));
        }
    }

    static public void setMenuItems(Menu menu, boolean abarnotsplit, boolean daexist){
        MenuItem msh = (MenuItem) menu.findItem(R.id.menu_share);
        if (abarnotsplit == true){
            MenuItem mda = (MenuItem) menu.findItem(R.id.menu_da);
            MenuItem mpa = (MenuItem) menu.findItem(R.id.menu_paste);
            MenuItem msa = (MenuItem) menu.findItem(R.id.menu_selectall);
            MenuItem mud = (MenuItem) menu.findItem(R.id.menu_undo);
            mda.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
            mpa.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
            msa.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
            mud.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
            msh.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);

        }
        if (!daexist){
            msh.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

    }
}
