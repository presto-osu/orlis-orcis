package ru.subprogram.paranoidsmsblocker.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.subprogram.paranoidsmsblocker.R;
import ru.subprogram.paranoidsmsblocker.database.entities.CASms;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;

public class CASmsListAdapter
	extends RecyclerView.Adapter<CASmsListAdapter.ViewHolder>
	implements IAOnClickListener, IAOnLongClickListener, IMultiselectListAdapter {

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

		private final IAOnClickListener onClickListener;
		private final IAOnLongClickListener onLongClickListener;

		TextView address;
		TextView text;
		TextView date;
		View progress;

		public ViewHolder(View v, IAOnClickListener onClickListener, IAOnLongClickListener onLongClickListener) {
			super(v);

			this.onClickListener = onClickListener;
			this.onLongClickListener = onLongClickListener;
			address = (TextView) v.findViewById(R.id.address);
			text = (TextView) v.findViewById(R.id.text);
			date = (TextView) v.findViewById(R.id.date);
			progress = v.findViewById(R.id.progress);

			v.setOnClickListener(this);
			v.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View v) {
			onClickListener.onItemClick(v, getPosition());
		}

		@Override
		public boolean onLongClick(View v) {
			return onLongClickListener.onItemLongClick(v, getPosition());
		}
	}

	private final Context mContext;
	private final IASmsListAdapterObserver mObserver;
	private IAOnClickListener mOnClickListener;
	private IAOnLongClickListener mOnLongClickListener;
	private boolean mIsMoreDataExist = true;

	private List<CASms> mList;
	private final List<Integer> mSelectedItems = new LinkedList<Integer>();

	public CASmsListAdapter(Context context, IASmsListAdapterObserver observer) {
		super();
		mContext = context;
		mObserver = observer;
	}

	public void setOnItemClickListener(IAOnClickListener listener) {
		mOnClickListener = listener;
	}

	public IAOnClickListener getOnItemClickListener() {
		return mOnClickListener;
	}

	public void setOnItemLongClickListener(IAOnLongClickListener listener) {
		mOnLongClickListener = listener;
	}

	@Override
	public void onItemClick(View view, int pos) {
		if(mOnClickListener !=null)
			mOnClickListener.onItemClick(view, pos);
	}

	@Override
	public boolean onItemLongClick(View view, int pos) {
		return mOnLongClickListener!=null
			&& mOnLongClickListener.onItemLongClick(view, pos);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.smslist_row_item, parent, false);
		return new ViewHolder(v, this, this);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if(position == mList.size()-1 && mIsMoreDataExist) {
			mObserver.loadMore();
			updateItemVisibility(holder, true);
			return;
		}

		updateItemVisibility(holder, false);
		final CASms sms = getItem(position);
		holder.address.setText(sms.getAddress());
		holder.text.setText(sms.getText());

		DateFormat df = DateFormat.getDateInstance();
		holder.date.setText(df.format(sms.getDate()));
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", mContext.get);


		if (isSelected(position)) {
			holder.itemView.setBackgroundResource(R.drawable.list_item_selector_checked);
		} else {
			holder.itemView.setBackgroundResource(R.drawable.list_item_selector);
		}

	}

	private void updateItemVisibility(ViewHolder holder, boolean isProgressVisible) {
		holder.progress.setVisibility(isProgressVisible ? View.VISIBLE : View.INVISIBLE);
		int vis = isProgressVisible ? View.INVISIBLE : View.VISIBLE;
		holder.address.setVisibility(vis);
		holder.text.setVisibility(vis);
		holder.date.setVisibility(vis);
	}

	public void setList(List<CASms> list) {
		mList = list;
		mSelectedItems.clear();
		mIsMoreDataExist = true;
	}

	public boolean toggleSelection(int position) {
		int item = mSelectedItems.indexOf(position);
		if (item >= 0) {
			mSelectedItems.remove(item);
			notifyDataSetChanged();
			return false;
		} else {
			mSelectedItems.add(position);
			notifyDataSetChanged();
			return true;
		}
	}

	public void setItemSelected(int position, boolean isSelected) {
		int item = mSelectedItems.indexOf(position);
		if(item>=0 && !isSelected)
			mSelectedItems.remove(item);
		else if(item<0 && isSelected)
			mSelectedItems.add(position);
		notifyDataSetChanged();
	}

	public List<Integer> getSelectedItems() {
		return mSelectedItems;
	}

	public void clearSelection() {
		mSelectedItems.clear();
		notifyDataSetChanged();
	}

	private boolean isSelected(int position) {
		return mSelectedItems.contains(position);
	}

	public void updateSelection(List<Integer> selectedPositions) {
		mSelectedItems.clear();
		mSelectedItems.addAll(selectedPositions);
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		return mList==null ? 0 : mList.size();
	}

	public CASms getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mList.get(position).getId();
	}

	public void addAll(List<CASms> list) {
		if(list.size()>0)
			mList.addAll(list);
		else
			mIsMoreDataExist = false;
	}

}
