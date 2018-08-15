package ru.subprogram.paranoidsmsblocker.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.ProgressBar;
import ru.subprogram.paranoidsmsblocker.R;
import ru.subprogram.paranoidsmsblocker.activities.utils.CAErrorDisplay;
import ru.subprogram.paranoidsmsblocker.activities.utils.RecyclerMultiSelectionUtil;
import ru.subprogram.paranoidsmsblocker.adapters.CAContactListAdapter;
import ru.subprogram.paranoidsmsblocker.adapters.IAOnClickListener;
import ru.subprogram.paranoidsmsblocker.database.entities.CAContact;

import java.util.ArrayList;
import java.util.List;

public abstract class CAContactListFragment extends CAAbstractFragment implements IAOnClickListener, RecyclerMultiSelectionUtil.MultiChoiceModeListener {

	protected CAContactListAdapter mAdapter;
	private RecyclerView mRecyclerView;
	private RecyclerMultiSelectionUtil.Controller mMultiSelectionController;
	private MenuItem mAddContactItem;
	private LinearLayoutManager mLayoutManager;
	private ProgressBar mProgress;
	private boolean mShouldListVisible = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		
		View v = inflater.inflate(R.layout.fragment_contact_list, container, false);
		
		mProgress = (ProgressBar) v.findViewById(R.id.progress);
		mRecyclerView = (RecyclerView)v.findViewById(R.id.recycler_list);
		mRecyclerView.setHasFixedSize(true);

		mLayoutManager = new LinearLayoutManager(getActivity());
		mRecyclerView.setLayoutManager(mLayoutManager);

		mAdapter = new CAContactListAdapter(getActivity());
		mAdapter.setOnItemClickListener(this);
		mRecyclerView.setAdapter(mAdapter);
		
		setListVisible(mShouldListVisible);

		mMultiSelectionController = RecyclerMultiSelectionUtil
			.attachMultiSelectionController(
				mRecyclerView,
				mAdapter,
				(AppCompatActivity) getActivity(), this);

		return v ;
	}


	public void setListVisible(boolean b) {
		mShouldListVisible = b;
		if(mRecyclerView ==null || mProgress==null)
			return;
		mRecyclerView.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
		mProgress.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		updateContent();
	}

	@Override
	public void onDestroyView() {
		if (mMultiSelectionController != null) {
			mMultiSelectionController.finish();
		}
		mMultiSelectionController = null;
		super.onDestroyView();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	protected abstract List<CAContact> getContent();

	@Override
	public void updateContent() {
		if(mObserver==null) return;
		mAdapter.setList(getContent());
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		List<Integer> selectedPositions = mAdapter.getSelectedItems();
		int numSelected = selectedPositions.size();
		mode.setTitle(getResources().getString(R.string.cab_selected_title, numSelected));

		mAddContactItem.setVisible(numSelected==1);
	}

	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
		MenuInflater inflater = actionMode.getMenuInflater();
		inflater.inflate(R.menu.contact_list_item, menu);
		mAddContactItem = menu.findItem(R.id.action_add_contact);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
		return false;
	}

	private ArrayList<Integer> getSelectedItemsIds() {
		ArrayList<Integer> checkedIds = new ArrayList<Integer>();
		List<Integer> positions = mAdapter.getSelectedItems();
		for (int pos: positions) {
			checkedIds.add((int) mAdapter.getItemId(pos));
		}
		return checkedIds;
	}

	@Override
	public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
		ArrayList<Integer> selectedIds = getSelectedItemsIds();
		actionMode.finish();

		switch (menuItem.getItemId()) {
			case R.id.action_add_contact:
				try {
					CAContact contact = mObserver.getDbEngine().getContactsTable().getById(selectedIds.get(0));
					if (contact != null)
						mObserver.addContact(contact.getAddress());
				}
				catch (Exception e) {
					CAErrorDisplay.showError(getActivity(), e);
				}
				break;
		}
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode actionMode) {
		mAdapter.clearSelection();
	}
}
