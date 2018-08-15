package org.itishka.pointim.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.itishka.pointim.R;
import org.itishka.pointim.model.point.ExtendedUser;
import org.itishka.pointim.utils.Utils;

/**
 * Created by Tishka17 on 31.01.2015.
 */
public class UserInfoPostListAdapter extends PostListAdapter {
    private ExtendedUser mUser;

    public UserInfoPostListAdapter(Context context) {
        super(context);
        setHasHeader(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        final View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_info_header, viewGroup, false);
        return new HeaderHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        super.onBindHeaderViewHolder(holder);
        HeaderHolder headerHolder = (HeaderHolder) holder;
        if (mUser == null)
            return;
        setText(mUser.about.text, headerHolder.about_group, headerHolder.about);
        setText(mUser.name, headerHolder.name, headerHolder.name);
        Utils.showAvatar(mUser.login, mUser.avatar, headerHolder.avatar);
        if (TextUtils.isEmpty(mUser.xmpp) &&
                TextUtils.isEmpty(mUser.icq) &&
                TextUtils.isEmpty(mUser.skype) &&
                TextUtils.isEmpty(mUser.homepage) &&
                TextUtils.isEmpty(mUser.email) &&
                TextUtils.isEmpty(mUser.location)
                ) {
            headerHolder.contacts_splitter.setVisibility(View.GONE);
        } else {
            headerHolder.contacts_splitter.setVisibility(View.VISIBLE);
        }
        headerHolder.gender.setText(holder.itemView.getContext().getString(Utils.getGenderString(mUser.gender)));
        setText(mUser.xmpp, headerHolder.xmpp_group, headerHolder.xmpp);
        setText(mUser.icq, headerHolder.icq_group, headerHolder.icq);
        setText(mUser.skype, headerHolder.skype_group, headerHolder.skype);
        setText(mUser.homepage, headerHolder.web_group, headerHolder.web);
        setText(mUser.email, headerHolder.email_group, headerHolder.email);
        setText(mUser.location, headerHolder.location_group, headerHolder.location);

        if (mUser.created == null) {
            headerHolder.registered_group.setVisibility(View.GONE);
        } else {
            headerHolder.registered_group.setVisibility(View.VISIBLE);
            headerHolder.registered.setText(Utils.formatDateOnly(mUser.created));
        }
        if (mUser.birthdate == null) {
            headerHolder.birthday_group.setVisibility(View.GONE);
        } else {
            headerHolder.birthday_group.setVisibility(View.VISIBLE);
            headerHolder.birthday.setText(Utils.formatDateOnly(mUser.birthdate));
        }
        headerHolder.login.setText("@" + mUser.login);
    }

    public void setUserInfo(ExtendedUser user) {
        if (user == null)
            return;
        mUser = user;
        notifyItemChanged(0);
    }

    private void setText(CharSequence text, View group, TextView field) {
        if (TextUtils.isEmpty(text)) {
            group.setVisibility(View.GONE);
        } else {
            group.setVisibility(View.VISIBLE);
            field.setText(text);
        }
    }

    private class HeaderHolder extends RecyclerView.ViewHolder {
        final TextView login;
        final TextView name;
        final View contacts_splitter;
        final TextView about;
        final View about_group;
        final TextView gender;
        final View gender_group;
        final TextView registered;
        final View registered_group;
        final View birthday_group;
        final TextView birthday;
        final View xmpp_group;
        final TextView xmpp;
        final View icq_group;
        final TextView icq;
        final View skype_group;
        final TextView skype;
        final View web_group;
        final TextView web;
        final View email_group;
        final TextView email;
        final View location_group;
        final TextView location;
        final ImageView avatar;

        public HeaderHolder(View v) {
            super(v);
            login = (TextView) v.findViewById(R.id.login);
            name = (TextView) v.findViewById(R.id.name);
            contacts_splitter = v.findViewById(R.id.contacts_splitter);
            about = (TextView) v.findViewById(R.id.about);
            about_group = v.findViewById(R.id.about_group);
            gender = (TextView) v.findViewById(R.id.gender);
            gender_group = v.findViewById(R.id.gender_group);
            registered = (TextView) v.findViewById(R.id.registered);
            registered_group = v.findViewById(R.id.registered_group);
            birthday = (TextView) v.findViewById(R.id.birthday);
            birthday_group = v.findViewById(R.id.birthday_group);
            xmpp = (TextView) v.findViewById(R.id.xmpp);
            xmpp_group = v.findViewById(R.id.xmpp_group);
            icq = (TextView) v.findViewById(R.id.icq);
            icq_group = v.findViewById(R.id.icq_group);
            skype = (TextView) v.findViewById(R.id.skype);
            skype_group = v.findViewById(R.id.skype_group);
            web = (TextView) v.findViewById(R.id.web);
            web_group = v.findViewById(R.id.web_group);
            email = (TextView) v.findViewById(R.id.email);
            email_group = v.findViewById(R.id.email_group);
            location = (TextView) v.findViewById(R.id.location);
            location_group = v.findViewById(R.id.location_group);
            avatar = (ImageView) v.findViewById(R.id.avatar);
        }
    }
}
