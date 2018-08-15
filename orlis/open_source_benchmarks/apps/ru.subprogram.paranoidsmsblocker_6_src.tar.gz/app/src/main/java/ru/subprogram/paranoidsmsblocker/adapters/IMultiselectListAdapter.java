package ru.subprogram.paranoidsmsblocker.adapters;

import java.util.List;

/**
 * Created by axel on 12.06.15.
 */
public interface IMultiselectListAdapter {
	void setOnItemLongClickListener(IAOnLongClickListener controller);

	IAOnClickListener getOnItemClickListener();

	void setOnItemClickListener(IAOnClickListener controller);

	void clearSelection();

	boolean toggleSelection(int pos);

	List<Integer> getSelectedItems();
}
