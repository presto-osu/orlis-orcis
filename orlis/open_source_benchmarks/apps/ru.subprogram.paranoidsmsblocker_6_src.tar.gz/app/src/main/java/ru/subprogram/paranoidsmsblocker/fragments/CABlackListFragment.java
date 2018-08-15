package ru.subprogram.paranoidsmsblocker.fragments;

import java.util.ArrayList;
import java.util.List;

import ru.subprogram.paranoidsmsblocker.activities.utils.CAErrorDisplay;
import ru.subprogram.paranoidsmsblocker.database.CADbEngine;
import ru.subprogram.paranoidsmsblocker.database.entities.CAContact;
import android.view.View;
import android.widget.AdapterView;
import ru.subprogram.paranoidsmsblocker.exceptions.CAException;

public class CABlackListFragment extends CAContactListFragment {
	
	@Override
	protected List<CAContact> getContent() {
		ArrayList<CAContact> list = new ArrayList<CAContact>();
		CADbEngine dbEngine = mObserver.getDbEngine();
		try {
			dbEngine.getContactsTable().getBlackList(list);
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
