/*
   Copyright (C) 2014  Samsung Electronics Polska Sp. z o.o.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU AFFERO General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    You may obtain a copy of the License at

                http://www.gnu.org/licenses/agpl-3.0.txt

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.samsung.srpol.ui;

import java.util.Timer;
import java.util.TimerTask;
import com.samsung.srpol.R;
import com.samsung.srpol.utils.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WelcomeDialog extends Dialog implements android.view.View.OnClickListener{
    private boolean isFirstTime;
    private ImageButton mOkButton;
    private TextView mDescriptionTextLink;
    private ImageView mNpLogo, mSamsungLogo, mGovLogo;
    private ProgressBar mProgressBar;
    private Context mContext;
    private boolean attachedToWindow = false;
    private boolean mCanBeClosed;
    private boolean mOpenedFromMenu = false;

    public WelcomeDialog(Context context, boolean openedFromMenu) {
        super(context, R.style.WelcomeDialog);
        mContext = context;
        mOpenedFromMenu = openedFromMenu;
        setContentView(R.layout.welcome_dialog);

        mOkButton = (ImageButton) this.findViewById(R.id.button);
        mDescriptionTextLink = (TextView) this.findViewById(R.id.welcome_description_link);
        mDescriptionTextLink.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        mProgressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        mNpLogo = (ImageView) findViewById(R.id.fondation_logo);
        mSamsungLogo = (ImageView) findViewById(R.id.samsung_logo);
        mGovLogo = (ImageView) findViewById(R.id.gov_logo);
        TextView versionName = (TextView) findViewById(R.id.app_version);
        versionName.setText(Utils.getAppVersionCode(context));
        mOkButton.setOnClickListener(this);
        mDescriptionTextLink.setOnClickListener(this);
        mNpLogo.setOnClickListener(this);
        mSamsungLogo.setOnClickListener(this);
        mGovLogo.setOnClickListener(this);

        isFirstTime = Utils.isFirstRun(this.getContext());

        mCanBeClosed = false;
        
        if (!isFirstTime && !mOpenedFromMenu) {
            new Timer().schedule(new TimerTask() {
                
                @Override
                public void run() {
                    synchronized (WelcomeDialog.this) {
                        if (mCanBeClosed)
                            WelcomeDialog.this.dismissDialog();
                        else
                            mCanBeClosed = true;
                    }
                }
            }, 2000);
        }
    }

    /* (non-Javadoc)
     * @see android.app.Dialog#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        if(mOkButton.getVisibility() == View.VISIBLE){
            super.onBackPressed();
        }
    }


    @Override
    public void onClick(View v) {
        if(v == mOkButton){
            this.dismissDialog();
        } else if(v == mDescriptionTextLink){
            Utils.startBrowser(mContext, mContext.getString(R.string.app_info_more_info));
        } else if(v == mNpLogo){
            Utils.startBrowser(mContext, mContext.getString(R.string.fond_site));
        } else if(v == mSamsungLogo){
            Utils.startBrowser(mContext, mContext.getString(R.string.samsung_site));
        } else if(v == mGovLogo){
            Utils.startBrowser(mContext, mContext.getString(R.string.gov_site));
        }
    }
    /* (non-Javadoc)
     * @see android.app.Dialog#onAttachedToWindow()
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
    }


    /* (non-Javadoc)
     * @see android.app.Dialog#onDetachedFromWindow()
     */
    @Override
    public void onDetachedFromWindow() {
        attachedToWindow = false;
        super.onDetachedFromWindow();
    }

    public void loadingDone() {
        if (mOpenedFromMenu) {
            return;
        }
        if (isFirstTime) {
            mProgressBar.setVisibility(View.GONE);
            mOkButton.setVisibility(View.VISIBLE);
        } else {
            synchronized (this) {
                if (mCanBeClosed) {
                    dismissDialog();
                }
                mCanBeClosed = true;
            }
        }
    }

    private void dismissDialog(){
        if(attachedToWindow){
            this.dismiss();
        }
    }

    /**
     * @return the mOpenedFromMenu
     */
    public boolean ismOpenedFromMenu() {
        return mOpenedFromMenu;
    }

    public void show(boolean openedFromMenu) {
        mOpenedFromMenu = openedFromMenu;
        if (openedFromMenu) {
            mOkButton.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            mOkButton.setVisibility(View.GONE);
        }
        this.show();
    }
}