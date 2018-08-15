package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.jparkie.aizoban.R;

import java.util.List;

public class CatalogueFilterAdapter extends BaseAdapter {
    private Context mContext;

    private List<String> mAvailableGenres;
    private List<String> mSelectedGenres;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String currentName = buttonView.getText().toString();

            if (isChecked) {
                if (!mSelectedGenres.contains(currentName)) {
                    mSelectedGenres.add(currentName);
                }
            } else {
                if (mSelectedGenres.contains(currentName)) {
                    mSelectedGenres.remove(currentName);
                }
            }
        }
    };

    public CatalogueFilterAdapter(Context context, List<String> availableGenres, List<String> selectedGenres) {
        mContext = context;

        mAvailableGenres = availableGenres;
        mSelectedGenres = selectedGenres;
    }

    @Override
    public int getCount() {
        return mAvailableGenres.size();
    }

    @Override
    public Object getItem(int position) {
        return mAvailableGenres.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View currentView = convertView;

        if (currentView == null) {
            currentView = LayoutInflater.from(mContext).inflate(R.layout.item_catalogue_genre, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) currentView.getTag();
        }

        String genreName = mAvailableGenres.get(position);
        boolean genreChecked = mSelectedGenres.contains(genreName);

        viewHolder.renderView(genreName, genreChecked);
        viewHolder.attachOnCheckedChangeListener(mOnCheckedChangeListener);

        return currentView;
    }

    public List<String> getSelectedGenres() {
        return mSelectedGenres;
    }

    public void setSelectedGenres(List<String> selectedGenres) {
        mSelectedGenres = selectedGenres;

        notifyDataSetChanged();
    }

    private static class ViewHolder {
        private CheckBox mCheckBox;

        public ViewHolder(View itemView) {
            mCheckBox = (CheckBox) itemView.findViewById(R.id.genreCheckBox);
        }

        public void renderView(String name, boolean isChecked) {
            mCheckBox.setText(name);
            mCheckBox.setChecked(isChecked);
        }

        public void attachOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
            mCheckBox.setOnCheckedChangeListener(listener);
        }
    }
}
