package com.github.dfa.diaspora_android.listener;

/**
 * Created by Gregor Santner (gsantner) on 26.03.16.
 */
public interface WebUserProfileChangedListener {
    void onUserProfileNameChanged(String name);

    void onUserProfileAvatarChanged(String avatarUrl);

    void onNotificationCountChanged(int notificationCount);

    void onUnreadMessageCountChanged(int unreadMessageCount);
}