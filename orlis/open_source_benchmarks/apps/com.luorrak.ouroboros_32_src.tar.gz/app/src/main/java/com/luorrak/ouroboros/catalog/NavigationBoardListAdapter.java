package com.luorrak.ouroboros.catalog;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.util.CursorRecyclerAdapter;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.DragAndDropRecyclerView.OnStartDragListener;
import com.luorrak.ouroboros.util.DragAndDropRecyclerView.TouchHelperInterface;
import com.luorrak.ouroboros.util.InfiniteDbHelper;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class NavigationBoardListAdapter extends CursorRecyclerAdapter implements TouchHelperInterface{
    private InfiniteDbHelper infiniteDbHelper;
    private Context context;
    private OnStartDragListener dragStartListener;
    public NavigationBoardListAdapter(Cursor cursor, Context context, InfiniteDbHelper infiniteDbHelper, OnStartDragListener dragStartListener) {
        super(cursor);
        this.context = context;
        this.infiniteDbHelper = infiniteDbHelper;
        this.dragStartListener = dragStartListener;
    }

    @Override
    public void onBindViewHolderCursor(final RecyclerView.ViewHolder holder, Cursor cursor) {
        final NavigationBoardListViewHolder navigationBoardListViewHolder = (NavigationBoardListViewHolder) holder;
        navigationBoardListViewHolder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    dragStartListener.onStartDrag(navigationBoardListViewHolder);
                }
                return false;
            }
        });
        navigationBoardListViewHolder.boardObject.boardName = cursor.getString(cursor.getColumnIndex(DbContract.BoardEntry.COLUMN_BOARDS));
        navigationBoardListViewHolder.boardObject.boardOrder = cursor.getInt(cursor.getColumnIndex(DbContract.BoardEntry.BOARD_ORDER));
        navigationBoardListViewHolder.boardNameBtn.setText("/" + navigationBoardListViewHolder.boardObject.boardName + "/");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_list_item, parent, false);
        return new NavigationBoardListViewHolder(view);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        infiniteDbHelper.swapBoardOrder(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(final int position) {
        //Disabled
    }



    class NavigationBoardListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Button boardNameBtn;
        public ImageButton deleteItemBtn;
        public ImageView handleView;
        public BoardObject boardObject;

        public NavigationBoardListViewHolder(View itemView) {
            super(itemView);
            boardNameBtn = (Button) itemView.findViewById(R.id.boardlist_boardname_button);
            deleteItemBtn = (ImageButton) itemView.findViewById(R.id.boardlist_delete_button);
            handleView = (ImageView) itemView.findViewById(R.id.boardlist_handle);
            boardObject = new BoardObject();

            boardNameBtn.setAllCaps(false);
            boardNameBtn.setOnClickListener(this);
            deleteItemBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.boardlist_boardname_button:{
                    String buttonText = boardNameBtn.getText().toString();
                    ((CatalogActivity)context).launchBoardFragment(buttonText.substring(1, buttonText.length()-1));
                    break;
                }
                case R.id.boardlist_delete_button:{
                    final String buttonText = boardNameBtn.getText().toString();
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle("Remove Board")
                            .setMessage("Are you sure you want to remove board: " + buttonText.substring(1, buttonText.length() - 1) + "?")
                            .setPositiveButton("Delete Board",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            infiniteDbHelper.removeBoardEntry(boardObject.boardOrder);
                                            changeCursor(infiniteDbHelper.getBoardCursor());
                                        }
                                    })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // what ever you want to do with No option.
                                }
                            })
                            .create();
                    alertDialog.show();
                    break;
                }
            }
        }
    }

    class BoardObject {
        public String boardName;
        public int boardOrder;
    }
}
