package com.alexcruz.papuhwalls.Live;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alexcruz.papuhwalls.R;
import com.github.mrengineer13.snackbar.SnackBar;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Daniel Huber on 22.12.2015.
 */
public class MyWallsManager extends Activity {

    private GridAdapter gridAdapter;
    private GridView gridView;
    private ArrayList<WallItem> lwPoolWalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wallpapers);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        lwPoolWalls = new ArrayList<>();

        File saveWallLoc = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.walls_save_location));

        File file[] = saveWallLoc.listFiles();
        for (File wall : file) {
            if(wall.getName().startsWith("PapuhLive")){
                lwPoolWalls.add(new WallItem(wall.getAbsolutePath()));
            }
        }

        checkIfEmpty();

        gridView = (GridView)findViewById(R.id.gridView);
        gridView.setNumColumns(3);
        gridAdapter = new GridAdapter(this, lwPoolWalls);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean checked = gridAdapter.getItem(position).isChecked();
                gridAdapter.setItemChecked(position, !checked);
            }
        });

        gridView.setAdapter(gridAdapter);

        Button button = (Button)findViewById(R.id.button);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleWalls();
            }
        });
    }

    private void handleWalls(){
        final ArrayList<WallItem> uncheckedItems = gridAdapter.getUncheckedItems();
        int count = uncheckedItems.size();

        if(count > 0) {

            String countText = " " + String.valueOf(count) + " ";

            MaterialDialog materialDialog = new MaterialDialog.Builder(MyWallsManager.this)
                    .title(R.string.confirm_action)
                    .content(getString(R.string.del_walls_message_1) + countText + getString(R.string.del_walls_message_2))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                            removeWalls(uncheckedItems);
                            gridAdapter = new GridAdapter(MyWallsManager.this, lwPoolWalls);
                            gridView.setAdapter(gridAdapter);

                            checkIfEmpty();

                            new SnackBar.Builder(MyWallsManager.this)
                                    .withMessageId(R.string.live_wall_pool_update_success)
                                    .withActionMessageId(R.string.ok)
                                    .withStyle(SnackBar.Style.ALERT)
                                    .withDuration(SnackBar.MED_SNACK)
                                    .show();
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                            materialDialog.cancel();
                        }
                    })
                    .build();

            materialDialog.setActionButton(DialogAction.POSITIVE, R.string.ok);
            materialDialog.setActionButton(DialogAction.NEGATIVE, R.string.dismiss);
            materialDialog.show();
        }
        else new SnackBar.Builder(MyWallsManager.this)
                .withMessageId(R.string.no_item_deselected)
                .withActionMessageId(R.string.ok)
                .withStyle(SnackBar.Style.ALERT)
                .withDuration(SnackBar.MED_SNACK)
                .show();
    }

    private void removeWalls(ArrayList<WallItem> items) {

        for(WallItem item : items) {
            File wall = new File(item.getPath());
            if(wall.exists())
                wall.delete();
            lwPoolWalls.remove(item);
        }
    }

    private void checkIfEmpty(){
        if(lwPoolWalls.size() == 0){
            TextView emptyView = (TextView)findViewById(R.id.empty);
            emptyView.setVisibility(View.VISIBLE);
        }
    }
}
