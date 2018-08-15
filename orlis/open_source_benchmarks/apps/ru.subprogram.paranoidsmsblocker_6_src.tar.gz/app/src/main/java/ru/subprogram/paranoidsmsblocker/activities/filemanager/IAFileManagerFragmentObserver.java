package ru.subprogram.paranoidsmsblocker.activities.filemanager;

import android.content.Intent;

interface IAFileManagerFragmentObserver {

	void finishActivity(int resultOk, Intent resultIntent);
}
