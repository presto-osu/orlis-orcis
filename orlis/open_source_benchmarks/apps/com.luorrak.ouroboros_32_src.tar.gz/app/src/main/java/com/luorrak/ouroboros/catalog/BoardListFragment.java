package com.luorrak.ouroboros.catalog;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.DragAndDropRecyclerView.BoardListTouchHelper;
import com.luorrak.ouroboros.util.DragAndDropRecyclerView.OnStartDragListener;
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

/*
   Adopted from
   https://github.com/slidenerd/materialtest
 */
/**
 * A simple {@link Fragment} subclass.
 */
public class BoardListFragment extends Fragment implements OnStartDragListener {
    private NavigationBoardListAdapter boardListAdapter;
    private ItemTouchHelper touchHelper;

    public BoardListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        getActivity().setTitle("Boards");
        View view = inflater.inflate(R.layout.fragment_board_list, container, false);

        InfiniteDbHelper infiniteDbHelper = new InfiniteDbHelper(getActivity());
        Cursor boardListCursor = infiniteDbHelper.getBoardCursor();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.board_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        boardListAdapter = new NavigationBoardListAdapter(boardListCursor, getActivity(), infiniteDbHelper, this);

        ItemTouchHelper.Callback callback = new BoardListTouchHelper(boardListAdapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(boardListAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_catalog, menu);
        MenuItem addBoard = menu.findItem(R.id.action_add_board);
        addBoard.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_board:{
                final EditText edittext = new EditText(getActivity());
                edittext.setInputType(InputType.TYPE_CLASS_TEXT);
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setMessage("New Board")
                        .setTitle("Please enter a board name.")
                        .setView(edittext)
                        .setPositiveButton("Add Board",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //Add board to database, should consider doing some error checking as well.
                                        final String boardName = edittext.getText().toString();
                                        Ion.with(getActivity())
                                                .load(ChanUrls.getCatalogUrl(boardName))
                                                .asString()
                                                .withResponse()
                                                .setCallback(new FutureCallback<Response<String>>() {
                                                    @Override
                                                    public void onCompleted(Exception e, Response<String> stringResponse) {
                                                        if (getActivity() != null){
                                                            if (e != null || stringResponse.getHeaders().code() == 404){
                                                                Snackbar.make(getView(), "Server Error! Does board exist?", Snackbar.LENGTH_LONG).show();
                                                                return;
                                                            }
                                                            InfiniteDbHelper infiniteDbHelper = new InfiniteDbHelper(getActivity());
                                                            infiniteDbHelper.insertBoardEntry(boardName, boardListAdapter.getCursor().getCount());
                                                            boardListAdapter.changeCursor(infiniteDbHelper.getBoardCursor());
                                                        }
                                                    }
                                                });
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }
}
