package ru.subprogram.paranoidsmsblocker.activities.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import ru.subprogram.paranoidsmsblocker.R;
import ru.subprogram.paranoidsmsblocker.activities.utils.CAErrorDisplay;
import ru.subprogram.paranoidsmsblocker.activities.utils.CAMime;
import ru.subprogram.paranoidsmsblocker.activities.utils.MultiSelectionUtil;
import ru.subprogram.paranoidsmsblocker.activities.utils.MultiSelectionUtil.MultiChoiceModeListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CAFileManagerFragment extends ListFragment implements MultiChoiceModeListener, OnClickListener  {
	
	public static final String ARG_ACTION = "action_pick_multiple_files";
	public static final String ARG_TYPE = "ARG_TYPE";

	public static final String KEY_CURRENT_FOLDER = "KEY_CURRENT_FOLDER";

	public static final String ACTION_PICK_FILE = "action_pick_file";
	public static final String ACTION_PICK_MULTIPLE_FILES = "action_pick_multiple_files";
	public static final String ACTION_PICK_DIRECTORY = "action_pick_dir";
	public static final String EXTRA_RESULT_PATH = "result_path";
	
	private static final int POSITION_MIME_FILE_TYPE = 0;
	private static final int POSITION_MIME_FILE_EXSTENSION = 1;
	
	private TextView mPath;
	private ProgressBar mProgressBar;
	
	private MultiSelectionUtil.Controller mMultiSelectionController;
	private Bundle mViewDestroyedInstanceState;
	private ActionMode mActionMode;
	
	private enum TAction { ENone, EPickFile, EPickMultipleFiles, EPickDirectory }
	private TAction mCurrentAction = TAction.ENone;
	
	private String mMimeFileType = "*";
	private String mMimeFileExtension = "*";
	
	private File mCurrentFolder;
	
	private volatile List<File> mFilesList;
	private CAFilesAdapter mFilesAdapter;
	
	private enum TPhase { EIdle, ELoadFileList }
	private TPhase mLoadPhase = TPhase.EIdle;
	
	private LoadTask mLoadTask;
	
	private MenuItem mAcceptItem;

	private IAFileManagerFragmentObserver mObserver;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		
		mCurrentAction = getActionFromIntent();
		extractMimeFromIntent();
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_file_manager, container, false);
		
		mPath = (TextView) v.findViewById(R.id.path);
		mProgressBar = (ProgressBar) v.findViewById(R.id.load_progress);
		
		mMultiSelectionController = MultiSelectionUtil
				.attachMultiSelectionController(
						(ListView) v.findViewById(android.R.id.list),
						(AppCompatActivity) getActivity(), this);
		if (savedInstanceState == null && isMenuVisible()) {
			savedInstanceState = mViewDestroyedInstanceState;
		}
		mMultiSelectionController.tryRestoreInstanceState(savedInstanceState);
		
		mFilesList = new ArrayList<File>();
		mFilesAdapter = new CAFilesAdapter(getActivity(), mFilesList);
		setListAdapter(mFilesAdapter);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(savedInstanceState!=null) {
			mCurrentFolder = new File(savedInstanceState.getString(KEY_CURRENT_FOLDER));
		}

		if(mCurrentFolder==null)
			mCurrentFolder = Environment.getExternalStorageDirectory();
		browseTo(mCurrentFolder);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mObserver = (IAFileManagerFragmentObserver) activity;
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
	public void onDetach() {
		mObserver = null;
		super.onDetach();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_CURRENT_FOLDER, mCurrentFolder.getAbsolutePath());
		if (mMultiSelectionController != null) {
			mMultiSelectionController.saveInstanceState(outState);
		}
	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);

		if (mMultiSelectionController == null) {
			return;
		}

		if (!menuVisible) {
			Bundle bundle = new Bundle();
			if (mMultiSelectionController.saveInstanceState(bundle)) {
				mViewDestroyedInstanceState = bundle;
				mMultiSelectionController.finish();
			}

		} else if (mViewDestroyedInstanceState != null) {
			mMultiSelectionController.tryRestoreInstanceState(mViewDestroyedInstanceState);
			mViewDestroyedInstanceState = null;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.file_manager_icon_menu, menu);
		
		mAcceptItem = menu.findItem(R.id.action_accept);

		if(mFilesAdapter!=null)
			mFilesAdapter.clearSelection();

		mAcceptItem.setVisible(mCurrentAction==TAction.EPickDirectory);

		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.action_accept:
			Intent resultIntent = new Intent();
			
			ArrayList<String> list = new ArrayList<String>();
			switch (mCurrentAction) {
			case EPickDirectory:
				list.add(mCurrentFolder.getAbsolutePath());
				break;
			case EPickFile: 
			case EPickMultipleFiles: 
				List<Integer> items = mFilesAdapter.getSelectedItems();
				for(Integer i: items) 
					list.add((mFilesAdapter.getItem(i)).getAbsolutePath());
				break;
			default:
				break;
			}
			resultIntent.putExtra(EXTRA_RESULT_PATH, list);
			mObserver.finishActivity(Activity.RESULT_OK, resultIntent);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public boolean onBackPressed() {
		
		if(mCurrentFolder == null)
			return false;
		
		String parentPath = mCurrentFolder.getParent();
		if(parentPath != null) {
			upOneLevel();
			return true;
		} else {
			return false;
		}	
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		File file = mFilesAdapter.getItem(position);
		if(file.isDirectory()) {
			browseTo(file);
		} else if (mCurrentAction == TAction.EPickFile) {
			mFilesAdapter.clearSelection();
			mFilesAdapter.selectItem(position);
		} else if(mCurrentAction == TAction.EPickMultipleFiles) {
			mFilesAdapter.selectItem(position);
		}

		mAcceptItem.setVisible(mFilesAdapter.getSelectedItems().size() > 0
			|| mCurrentAction == TAction.EPickDirectory);
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		//List<Integer> selectedPositions = getSelectedItemsPositions();
		mode.finish();
		
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mFilesAdapter.clearSelection();
		mActionMode = null;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		return false;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		
		List<Integer> selectedPositions = getSelectedItemsPositions();
		//int numSelected = selectedPositions.size();
		//mode.setTitle(getResources().getString(R.string.cab_selected_title,numSelected));
		
		mFilesAdapter.updateSelection(selectedPositions);
	}
	
	private List<Integer> getSelectedItemsPositions() {
		List<Integer> checkedPositions = new ArrayList<Integer>();
		SparseBooleanArray checkedPositionsBool = getListView().getCheckedItemPositions();
		for (int i = 0; i < checkedPositionsBool.size(); i++) {
			if (checkedPositionsBool.valueAt(i)) {
				checkedPositions.add(checkedPositionsBool.keyAt(i));
			}
		}
		return checkedPositions;
	}
	
	private void upOneLevel() {
		
		if(mCurrentAction != TAction.EPickDirectory && mAcceptItem != null) {
			mFilesAdapter.clearSelection();
			mAcceptItem.setVisible(false);
		}
		
		if(mActionMode != null)
			mActionMode.finish();
		
		if(mLoadTask != null && mLoadPhase == TPhase.ELoadFileList) {
			mLoadTask.cancel(true);
		} else {
			String parentPath = mCurrentFolder.getParent();
			if(parentPath != null) {
				browseTo(new File(parentPath));
			}
		}	
	}
	
	private void browseSd() {
		browseTo(Environment.getExternalStorageDirectory());
	}
	
	private void browseTo(File file) {
		
		if(mCurrentAction != TAction.EPickDirectory && mAcceptItem != null) {
			mFilesAdapter.clearSelection();
			mAcceptItem.setVisible(false);
		}	
		
		if(mActionMode != null)
			mActionMode.finish();
		
		if(mLoadPhase == TPhase.EIdle) {
			mLoadTask = new LoadTask();
			mLoadTask.execute(file);
		}
	}
	
	private TAction getActionFromIntent() {
		Bundle args = getArguments();
		if(args == null)
			return TAction.ENone;
		
		String action = args.getString(ARG_ACTION);
		if(TextUtils.isEmpty(action)) {
			return TAction.ENone;
		} else if(action.equals(ACTION_PICK_FILE)) {
			return TAction.EPickFile;
		} else if(action.equals(ACTION_PICK_MULTIPLE_FILES)) {
			return TAction.EPickMultipleFiles;
		} else if (action.equals(ACTION_PICK_DIRECTORY)) {
			return TAction.EPickDirectory;
		}
		return TAction.ENone;
	}
	
	private void extractMimeFromIntent() {
		Bundle args = getArguments();
		if(args == null)
			return;
		
		String mime = args.getString(ARG_TYPE);
		if(!TextUtils.isEmpty(mime)) {
			String[] mimeData = mime.split("/");
			if(mimeData.length != 2)
				return;
			
			mMimeFileType = mimeData[POSITION_MIME_FILE_TYPE];
			mMimeFileExtension = mimeData[POSITION_MIME_FILE_EXSTENSION];
		}
	}
	
	private boolean matchMimeType(String fileMime) {

		if(fileMime == null) {
			return mMimeFileType.equals("*") && mMimeFileExtension.equals("*");
		}
		
		String[] mimeData = fileMime.split("/");
		
		if(mimeData.length != 2)
			return false;
		
		if(mMimeFileType.equals("*") || mMimeFileType.equals(mimeData[POSITION_MIME_FILE_TYPE])) {
			return mMimeFileExtension.equals("*") || mMimeFileExtension.equals(mimeData[POSITION_MIME_FILE_EXSTENSION]);
		}
		return false;
	}
	
	private class LoadTask extends AsyncTask<File, Void, List<File>> {

		@Override
	    protected void onPreExecute() {
	      super.onPreExecute();
	      
	      mLoadPhase = TPhase.ELoadFileList;
	      
	      mProgressBar.setVisibility(View.VISIBLE);
	      getListView().setVisibility(View.INVISIBLE);
	    }
		
		@Override
		protected List<File> doInBackground(File... params) {
			
			List<File> filesList = new ArrayList<File>();
			
			if(params.length == 0) return null;
			File file = params[0];
			
			if(file.isDirectory() && file.canRead()) {
				
				for(File f : file.listFiles()) {
					
					if(f.isDirectory() && !f.canRead())
						continue;
					
					switch (mCurrentAction) {
					case EPickFile:
					case EPickMultipleFiles:
						
						if(f.isFile()) {
							String fileMime = CAMime.getMimeType(f.getAbsolutePath());
							if(matchMimeType(fileMime)) {
								filesList.add(f);
							}
						} else if(f.isDirectory() && !f.isHidden()){
							filesList.add(f);
						}
						
						break;
					case EPickDirectory:

						if(f.isDirectory() && !f.isHidden())
							filesList.add(f);
						else if(f.isFile())
							filesList.add(f);
							
						break;
						
					default:
						filesList.add(f);
						break;
					}
				}
				
				Collections.sort(filesList, new FileComparator());
				mCurrentFolder = file;

			} else {
				return  null; 
			}
			return filesList;
		}
		
		@Override
		protected void onPostExecute(List<File> resultList) {

			if(resultList == null) {
				CAErrorDisplay.showText(getActivity(),
					R.string.havent_access_privileges);
			} else {
				mFilesList = resultList;
				mFilesAdapter.updateFileList(resultList);
				mFilesAdapter.notifyDataSetChanged();
				mPath.setText(mCurrentFolder.getPath());
			}
			
			mProgressBar.setVisibility(View.INVISIBLE);
		    getListView().setVisibility(View.VISIBLE);
		    
			mLoadPhase = TPhase.EIdle;
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			
			mProgressBar.setVisibility(View.INVISIBLE);
			getListView().setVisibility(View.VISIBLE);
			      
			mLoadPhase = TPhase.EIdle;
		}
	}
	
	private static class FileComparator implements Comparator<File> {

		@Override
		public int compare(File lhs, File rhs) {
			
			if( (lhs.isDirectory() && rhs.isDirectory()) || (lhs.isFile() && rhs.isFile())) {
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			} else if(lhs.isDirectory()) {
				return -1;
			} else {
				return  1;
			}
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		default:
			break;
		}
		
	}
	
}
