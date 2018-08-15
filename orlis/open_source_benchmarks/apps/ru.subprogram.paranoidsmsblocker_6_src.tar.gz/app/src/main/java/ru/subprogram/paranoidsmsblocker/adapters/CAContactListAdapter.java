package ru.subprogram.paranoidsmsblocker.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.subprogram.paranoidsmsblocker.R;
import ru.subprogram.paranoidsmsblocker.database.entities.CAContact;

import java.util.LinkedList;
import java.util.List;

public class CAContactListAdapter
	extends RecyclerView.Adapter<CAContactListAdapter.ViewHolder>
	implements IAOnClickListener, IAOnLongClickListener, IMultiselectListAdapter {

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

		private final IAOnClickListener onClickListener;
		private final IAOnLongClickListener onLongClickListener;

		TextView address;

		public ViewHolder(View v, IAOnClickListener onClickListener, IAOnLongClickListener onLongClickListener) {
			super(v);

			this.onClickListener = onClickListener;
			this.onLongClickListener = onLongClickListener;
			address = (TextView) v.findViewById(R.id.address);

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
	private List<CAContact> mList;
	private final List<Integer> mSelectedItems = new LinkedList<>();
	private IAOnClickListener mOnClickListener;
	private IAOnLongClickListener mOnLongClickListener;

	public CAContactListAdapter(Context context) {
		super();
		mContext = context;
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
	public boolean onItemLongClick(View view, int pos) {
		return mOnLongClickListener!=null
			&& mOnLongClickListener.onItemLongClick(view, pos);
	}

	@Override
	public void onItemClick(View view, int pos) {
		if(mOnClickListener !=null)
			mOnClickListener.onItemClick(view, pos);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.contactlist_row_item, parent, false);
		return new ViewHolder(v, this, this);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		final CAContact contact = getItem(position);
		holder.address.setText(String.valueOf(position+1)+". "+contact.getAddress());
		if (isSelected(position)) {
			holder.itemView.setBackgroundResource(R.drawable.list_item_selector_checked);
		} else {
			holder.itemView.setBackgroundResource(R.drawable.list_item_selector);
		}
	}

	public void setList(List<CAContact> list) {
		mList = list;
		mSelectedItems.clear();
	}

	public boolean toggleSelection(int position) {
		int item = mSelectedItems.indexOf(position);
		if (item >= 0) {
			mSelectedItems.remove(item);
			notifyDataSetChanged();
			return false;
		} else {
			mSelectedItems.clear();
			mSelectedItems.add(position);
			notifyDataSetChanged();
			return true;
		}
	}

	@Override
	public int getItemCount() {
		return mList==null ? 0 : mList.size();
	}

	public CAContact getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mList.get(position).getId();
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

}
