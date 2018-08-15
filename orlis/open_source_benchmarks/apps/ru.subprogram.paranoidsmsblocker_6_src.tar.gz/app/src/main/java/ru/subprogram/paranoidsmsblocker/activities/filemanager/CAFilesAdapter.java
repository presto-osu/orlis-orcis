package ru.subprogram.paranoidsmsblocker.activities.filemanager;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ru.subprogram.paranoidsmsblocker.R;
import ru.subprogram.paranoidsmsblocker.activities.utils.CAFormatter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

class CAFilesAdapter extends BaseAdapter {

	private List<File> mFileList;
	private final List<Integer> mSelectedItems;
	
	private Context mContext;
	
	private final int mBackgroundResource;
	private final int mSelectedResource;
	private final int mFolderIconId;
	private final int mFileIconId;
	
	public CAFilesAdapter(Context context, List<File> filesList) {
		super();
		mFileList = filesList;
		mSelectedItems = new LinkedList<Integer>();
		mContext = context;
		
        mBackgroundResource = 0;
		mSelectedResource = R.drawable.list_item_selector_checked;

		mFolderIconId = R.drawable.j4_collections_collection_light;
		mFileIconId = R.drawable.j4_collections_view_as_list_light;
	}
	
	@Override
	public int getCount() {
		return mFileList.size();
	}

	@Override
	public File getItem(int position) {
		return mFileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		
		if(convertView == null) {
			
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.row_file_manager_list, parent, false);
			
			holder = new ViewHolder();
			
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
			holder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		File file = mFileList.get(position);
		
		holder.fileName.setText(file.getName());
		
		if(file.isDirectory()) {
			holder.icon.setImageResource(mFolderIconId);
			holder.fileSize.setVisibility(View.GONE);
		} else {
			holder.icon.setImageResource(mFileIconId);
			holder.fileSize.setVisibility(View.VISIBLE);
			
			int size = (int) file.length();
			holder.fileSize.setText(CAFormatter.getFileSize(mContext, size));
		}
		
		if (!isEnabled(position)) {
			convertView.setEnabled(false);
			convertView.setBackgroundResource(mBackgroundResource);
		} else if (isSelected(position)) {
			convertView.setEnabled(true);
			convertView.setBackgroundResource(mSelectedResource);
		} else {
			convertView.setEnabled(true);
			convertView.setBackgroundResource(mBackgroundResource);
		}
		
		return convertView;
	}
	
	private boolean isSelected(int position) {
		return mSelectedItems.contains(position);
	}

	public void updateFileList(List<File> list) {
		mFileList = list;
		mSelectedItems.clear();
	}
	
	private static class ViewHolder {
		ImageView icon;
		TextView fileName;
		TextView fileSize;
	}

	public void selectItem(int position) {
		int item = mSelectedItems.indexOf(position);
		if(item>=0)
			mSelectedItems.remove(item);
		else
			mSelectedItems.add(position);
		notifyDataSetInvalidated();
	}

	public List<Integer> getSelectedItems() {
		return mSelectedItems;
	}

	public void clearSelection() {
		mSelectedItems.clear();
	}

	public void updateSelection(List<Integer> selectedPositions) {
		mSelectedItems.clear();
		mSelectedItems.addAll(selectedPositions);
		notifyDataSetChanged();
	}
}
