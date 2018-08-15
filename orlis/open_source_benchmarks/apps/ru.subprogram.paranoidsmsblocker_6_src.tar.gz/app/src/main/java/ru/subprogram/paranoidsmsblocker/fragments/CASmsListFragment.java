package ru.subprogram.paranoidsmsblocker.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import ru.subprogram.paranoidsmsblocker.R;
import ru.subprogram.paranoidsmsblocker.activities.utils.CAErrorDisplay;
import ru.subprogram.paranoidsmsblocker.activities.utils.RecyclerMultiSelectionUtil;
import ru.subprogram.paranoidsmsblocker.adapters.CASmsListAdapter;
import ru.subprogram.paranoidsmsblocker.adapters.IAOnClickListener;
import ru.subprogram.paranoidsmsblocker.adapters.IASmsListAdapterObserver;
import ru.subprogram.paranoidsmsblocker.database.CADbEngine;
import ru.subprogram.paranoidsmsblocker.database.entities.CASms;
import ru.subprogram.paranoidsmsblocker.exceptions.CAException;
import ru.subprogram.paranoidsmsblocker.smsreceiver.CADefaultSmsReceiver;

import java.util.ArrayList;
import java.util.List;

public class CASmsListFragment extends CAAbstractFragment
	implements IAOnClickListener,
	IASmsListAdapterObserver,
	ActionMode.Callback, RecyclerMultiSelectionUtil.MultiChoiceModeListener {

	private static final int LOAD_PART_SIZE = 20;

	private CASmsListAdapter mAdapter;
	private RecyclerMultiSelectionUtil.Controller mMultiSelectionController;
	private RecyclerView mRecyclerView;

	private MenuItem mAddContactItem;

	private final Runnable mLoadMoreRunnable = new Runnable() {
		@Override
		public void run() {
			List<CASms> list = getContent(mAdapter.getItemCount(), LOAD_PART_SIZE);
			mAdapter.addAll(list);
			mAdapter.notifyDataSetChanged();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_sms_list, container, false);
		
		mRecyclerView = (RecyclerView)v;
		mRecyclerView.setHasFixedSize(true);

		LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
		mRecyclerView.setLayoutManager(layoutManager);

		mAdapter = new CASmsListAdapter(getActivity(), this);
		mAdapter.setOnItemClickListener(this);
		mRecyclerView.setAdapter(mAdapter);
		
		NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(CADefaultSmsReceiver.NOTIFICATION_ID);

		mMultiSelectionController = RecyclerMultiSelectionUtil
			.attachMultiSelectionController(
				mRecyclerView,
				mAdapter,
				(AppCompatActivity) getActivity(), this);

		return v ;
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

	private List<CASms> getContent(int offset, int count) {
		ArrayList<CASms> list = new ArrayList<CASms>();
		CADbEngine dbEngine = mObserver.getDbEngine();
		try {
			dbEngine.getSmsTable().getAll(list, offset, count);
		} catch (CAException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public void updateContent() {
		if(mObserver==null) return;
		mAdapter.setList(getContent(0, LOAD_PART_SIZE));
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void loadMore() {
		mRecyclerView.post(mLoadMoreRunnable);
	}

	@Override
	public void onItemClick(View view, int pos) {
		CASms sms = mAdapter.getItem(pos);
		mObserver.showSmsDialog(sms);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.sms_list, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem deleteAllItem = menu.findItem(R.id.action_delete_all);
		deleteAllItem.setVisible(mAdapter.getItemCount() > 0);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_delete_all:
				mObserver.showDeleteAllSmsDialog();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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
		inflater.inflate(R.menu.sms_list_item, menu);
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
			case R.id.action_delete:
				mObserver.showDeleteSelectedSmsDialog(selectedIds);
				break;
			case R.id.action_move_to_inbox:
				mObserver.moveToInbox(selectedIds);
				break;
			case R.id.action_add_contact:
				try {
					CASms sms = mObserver.getDbEngine().getSmsTable().getById(selectedIds.get(0));
					if (sms != null)
						mObserver.addContact(sms.getAddress());
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
