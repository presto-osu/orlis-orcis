package ru.subprogram.paranoidsmsblocker.fragments;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.AdapterView;
import ru.subprogram.paranoidsmsblocker.activities.utils.CAErrorDisplay;
import ru.subprogram.paranoidsmsblocker.database.CADbEngine;
import ru.subprogram.paranoidsmsblocker.database.entities.CAContact;
import ru.subprogram.paranoidsmsblocker.exceptions.CAException;

public class CAWhiteListFragment extends CAContactListFragment {

	@Override
	protected List<CAContact> getContent() {
		ArrayList<CAContact> list = new ArrayList<CAContact>();
		CADbEngine dbEngine = mObserver.getDbEngine();
		try {
			dbEngine.getContactsTable().getWhiteList(list);
		} catch (CAException e) {
			CAErrorDisplay.showError(getActivity(), e);
		}
		return list;
	}

	@Override
	public void onItemClick(View view, int pos) {
		CAContact contact = mAdapter.getItem(pos);
		mObserver.showSmsDialog(contact);
	}

}
