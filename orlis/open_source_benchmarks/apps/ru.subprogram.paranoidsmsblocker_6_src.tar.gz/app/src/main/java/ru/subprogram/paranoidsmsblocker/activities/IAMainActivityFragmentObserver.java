package ru.subprogram.paranoidsmsblocker.activities;

import ru.subprogram.paranoidsmsblocker.database.CADbEngine;
import ru.subprogram.paranoidsmsblocker.database.entities.CAContact;
import ru.subprogram.paranoidsmsblocker.database.entities.CASms;

import java.util.ArrayList;

public interface IAMainActivityFragmentObserver {

	CADbEngine getDbEngine();

	void showSmsDialog(CAContact contact);
	void showSmsDialog(CASms sms);

	void showDeleteSelectedSmsDialog(ArrayList<Integer> selectedIds);
	void showDeleteAllSmsDialog();

	void moveToInbox(ArrayList<Integer> selectedIds);
	void addContact(String phone);
}
