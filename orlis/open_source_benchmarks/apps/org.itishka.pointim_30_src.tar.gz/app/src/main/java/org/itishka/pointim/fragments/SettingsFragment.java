package org.itishka.pointim.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.itishka.pointim.BuildConfig;
import org.itishka.pointim.R;
import org.itishka.pointim.activities.ImgurAuthActivity;
import org.itishka.pointim.activities.LoginActivity;
import org.itishka.pointim.activities.UserViewActivity;
import org.itishka.pointim.model.imgur.Token;
import org.itishka.pointim.model.point.LoginResult;
import org.itishka.pointim.network.ImgurConnectionManager;
import org.itishka.pointim.network.PointConnectionManager;
import org.itishka.pointim.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsFragment extends Fragment {
    private static final int REQUEST_LOGIN_POINT = 1;
    private static final int REQUEST_LOGIN_IMGUR = 2;
    SharedPreferences prefs;
    private ImageView mPointAvatar;
    private ImageButton mPointLogout;
    private TextView mPointName;
    private ImageButton mPointLogin;
    private ImageButton mImgurLogin;
    private ImageButton mImgurLogout;
    private TextView mImgurName;
    private ImageView mImgurAvatar;

    public SettingsFragment() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOGIN_POINT || requestCode == REQUEST_LOGIN_IMGUR) {
            updateViews();
        }
    }

    private void updateViews() {
        LoginResult loginResult = PointConnectionManager.getInstance().loginResult;
        if (loginResult == null) {
            mPointAvatar.setVisibility(View.GONE);
            mPointLogout.setVisibility(View.GONE);
            mPointLogin.setVisibility(View.VISIBLE);
            mPointName.setText(getString(R.string.logged_out_stub));
        } else {
            mPointAvatar.setVisibility(View.VISIBLE);
            mPointLogout.setVisibility(View.VISIBLE);
            mPointLogin.setVisibility(View.GONE);
            mPointName.setText(loginResult.login);
            Utils.showAvatarByLogin(loginResult.login, mPointAvatar);
        }

        Token imgutToken = ImgurConnectionManager.getInstance().token;
        if (imgutToken == null) {
            mImgurAvatar.setVisibility(View.GONE);
            mImgurLogout.setVisibility(View.GONE);
            mImgurLogin.setVisibility(View.VISIBLE);
            mImgurName.setText(getString(R.string.logged_out_stub));
        } else {
            mImgurAvatar.setVisibility(View.VISIBLE);
            mImgurLogout.setVisibility(View.VISIBLE);
            mImgurLogin.setVisibility(View.GONE);
            mImgurName.setText(imgutToken.account_username);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        prefs = getActivity().getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        mPointName = (TextView) rootView.findViewById(R.id.point_login);
        mImgurName = (TextView) rootView.findViewById(R.id.imgur_login);

        mPointAvatar = (ImageView) rootView.findViewById(R.id.point_avatar);
        mImgurAvatar = (ImageView) rootView.findViewById(R.id.imgur_avatar);

        mPointLogout = (ImageButton) rootView.findViewById(R.id.action_point_logout);
        mPointLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPointLogout();
            }
        });
        mPointLogin = (ImageButton) rootView.findViewById(R.id.action_point_login);
        mPointLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPointLogin();
            }
        });

        mImgurLogin = (ImageButton) rootView.findViewById(R.id.action_imgur_login);
        mImgurLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askImgurLogin();
            }
        });
        mImgurLogout = (ImageButton) rootView.findViewById(R.id.action_imgur_logout);
        mImgurLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askImgurLogout();
            }
        });

        TextView version = (TextView) rootView.findViewById(R.id.version);
        version.setText(getString(R.string.version) + " " + BuildConfig.VERSION_NAME);

        rootView.findViewById(R.id.google_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.itishka.pointim"));
                getActivity().startActivity(browserIntent);
            }
        });
        rootView.findViewById(R.id.github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Tishka17/Point.im-Android/"));
                getActivity().startActivity(browserIntent);
            }
        });

        ImageView avatarTishka17 = (ImageView) rootView.findViewById(R.id.avatar_tishka17);
        Utils.showAvatarByLogin("Tishka17", avatarTishka17);
        avatarTishka17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), UserViewActivity.class);
                intent.putExtra(UserViewActivity.EXTRA_USER, "Tishka17");
                ActivityCompat.startActivity(getActivity(), intent, null);
            }
        });

        ImageView avatarArts = (ImageView) rootView.findViewById(R.id.avatar_arts);
        Utils.showAvatarByLogin("arts", avatarArts);
        avatarArts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), UserViewActivity.class);
                intent.putExtra(UserViewActivity.EXTRA_USER, "arts");
                ActivityCompat.startActivity(getActivity(), intent, null);
            }
        });

        //LoadPictures switch
        Switch swLoadPictures = (Switch) rootView.findViewById(R.id.swLoadPictures);
        swLoadPictures.setChecked(prefs.getBoolean("loadImages", true));
        swLoadPictures.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("loadImages", isChecked);
                editor.apply();
            }
        });
        //LoadPictures switch
        Switch swMultiColumns = (Switch) rootView.findViewById(R.id.swMultiColumn);
        swMultiColumns.setChecked(prefs.getBoolean("multiColumns", true));
        swMultiColumns.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("multiColumns", isChecked);
                editor.apply();
            }
        });
        Switch swThemeDark = (Switch) rootView.findViewById(R.id.swThemeDark);
        swThemeDark.setChecked(prefs.getBoolean("themeDark", false));
        swThemeDark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("themeDark", isChecked);
                editor.apply();
            }
        });

        updateViews();
        return rootView;
    }

    private void askImgurLogout() {
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_logout_imgur_title)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        ImgurConnectionManager.getInstance().resetAuthorization(getActivity());
                        updateViews();
                    }
                })
                .build();
        dialog.show();
    }

    private void askImgurLogin() {
        startActivityForResult(new Intent(getActivity(), ImgurAuthActivity.class), REQUEST_LOGIN_IMGUR);
    }

    private void askPointLogin() {
        startActivityForResult(new Intent(getActivity(), LoginActivity.class), REQUEST_LOGIN_POINT);
    }

    private void askPointLogout() {
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_logout_point_title)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        PointConnectionManager.getInstance().resetAuthorization(getActivity());
                        updateViews();
                    }
                })
                .build();
        dialog.show();
    }
}
