package layout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.bienvenidoainternet.app.MainActivity;
import org.bienvenidoainternet.app.R;
import org.bienvenidoainternet.app.RecentPostAdapter;
import org.bienvenidoainternet.app.ResponseActivity;
import org.bienvenidoainternet.app.ThemeManager;
import org.bienvenidoainternet.app.ThreadListAdapter;
import org.bienvenidoainternet.app.structure.Board;
import org.bienvenidoainternet.app.structure.BoardItem;
import org.bienvenidoainternet.app.structure.ReplyID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 *   BaiApp - Bienvenido a internet Android Application
 *   Copyright (C) 2016 Renard1911(https://github.com/Renard1911)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class FragmentBoardItemList extends Fragment {
    public static final String ARG_CURRENTBOARD = "currentBoard", ARG_THREAD_ID = "currentThreadId", ARG_MAIN_FRAGMENT = "imMainFragment",
            SAVED_BOARDITEMS = "savedBoardItems", RECENT_POST_MODE = "recentpostmode", ARG_CURRENT_THREAD = "currentThread";
    List<ReplyID> idList = new ArrayList<>();
    public ArrayList<BoardItem> boardItems = new ArrayList<BoardItem>();
    public Board currentBoard = null;
    public BoardItem currentThread = null;
    private boolean imMainFragment;
    private OnFragmentInteractionListener mListener;
    private ThreadListAdapter listViewAdapter;
    private RecentPostAdapter recentPostAdapter;
    private ListView listViewBoardItems = null;;
    private ProgressBar loadingBar = null;
    SharedPreferences settings;
    private boolean loadingMoreThreads = false;
    View themedContext;
    private int currentOffset = 0;

    ViewGroup rootView;
    private boolean recentPostMode = false;

//    ProgressBar barThreadProcess;
    LinearLayout layoutThreadProcess;
    TextView txtThreadProcess;

    ThemeManager tm;

    public FragmentBoardItemList() {
        // Required empty public constructor

    }

    public static FragmentBoardItemList newInstance(boolean mainFragment, Board board, BoardItem thread){
        FragmentBoardItemList fragment = new FragmentBoardItemList();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CURRENTBOARD, board);
        args.putParcelable(ARG_CURRENT_THREAD, thread);
        args.putBoolean(ARG_MAIN_FRAGMENT, mainFragment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
        if (getArguments() != null) {
            this.currentBoard = getArguments().getParcelable(ARG_CURRENTBOARD);
            this.currentThread = getArguments().getParcelable(ARG_CURRENT_THREAD);
            this.imMainFragment = getArguments().getBoolean(ARG_MAIN_FRAGMENT);
        }
        tm = new ThemeManager(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVED_BOARDITEMS, boardItems);
        outState.putBoolean(RECENT_POST_MODE, recentPostMode);
        outState.putParcelable(ARG_CURRENT_THREAD, currentThread);
        outState.putParcelable(ARG_CURRENTBOARD, currentBoard);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // cargamos la instancia si esta guardada
        if (savedInstanceState != null){
            recentPostMode = savedInstanceState.getBoolean(RECENT_POST_MODE);
            currentBoard = savedInstanceState.getParcelable(ARG_CURRENTBOARD);
            currentThread = savedInstanceState.getParcelable(ARG_CURRENT_THREAD);
            boardItems = savedInstanceState.getParcelableArrayList(SAVED_BOARDITEMS);
        }

        // Aplicación del Tema
        settings = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        int themeResId = tm.getCurrentThemeId();
        Context context = new ContextThemeWrapper(getActivity(), themeResId);
        LayoutInflater localInflater = inflater.cloneInContext(context);
        View v = localInflater.inflate(R.layout.fragment_fragment_thread_list, container, false);
        themedContext = v;
        this.rootView = (ViewGroup)v;

        // Seteamos los controles que son guardados globalmente
        listViewBoardItems = (ListView)v.findViewById(R.id.lvThreadList);
//        barThreadProcess = (ProgressBar)rootView.findViewById(R.id.barThreadProcess);
        layoutThreadProcess = (LinearLayout)rootView.findViewById(R.id.layoutThreadProcess);
        txtThreadProcess = (TextView)rootView.findViewById(R.id.txtThreadError);
        this.loadingBar = (ProgressBar)rootView.findViewById(R.id.progressBar);

        // Agregamos color al divider del listview
        ColorDrawable cd = new ColorDrawable(tm.getMarginColor());
        listViewBoardItems.setDivider(cd);
        listViewBoardItems.setDividerHeight(1);

        // registramos los menus del listview
        registerForContextMenu(listViewBoardItems);
        // Creamos los dos adaptadores y los seteamos dependiendo del modo del fragmento
        listViewAdapter = new ThreadListAdapter(v.getContext(), boardItems, tm);
        recentPostAdapter = new RecentPostAdapter(v.getContext(), boardItems);
        if (recentPostMode){
            listViewBoardItems.setAdapter(recentPostAdapter);
        }else{
            listViewBoardItems.setAdapter(listViewAdapter);
        }

        if (!imMainFragment){
            listViewAdapter.listThreads = true;
        }

        listViewBoardItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (imMainFragment && !recentPostMode) {
                    BoardItem bi = listViewAdapter.getItem(position);
                    mListener.showThread(currentBoard, bi);
                } else if (imMainFragment && recentPostMode) {
                    BoardItem bi = boardItems.get(position);
                    mListener.showThread(bi.getParentBoard(), bi);
                }
            }
        });


        listViewBoardItems.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int lastFirstVisibleItem = 0;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(lastFirstVisibleItem < firstVisibleItem) { // Scrolling down
                    mListener.hideActionButton();
//                    ((MainActivity)getActivity()).getSupportActionBar().hide();
                }else  if(lastFirstVisibleItem > firstVisibleItem) { // Scrolling Up
                    mListener.showActionButton();
//                    ((MainActivity)getActivity()).getSupportActionBar().show();
                }
                lastFirstVisibleItem = firstVisibleItem;
                for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                    if (!recentPostMode){
                        BoardItem bi = listViewAdapter.getItem(i);
                        if (!bi.getThumb().isEmpty() && bi.getThumbBitmap() == null && !bi.downloadingThumb) {
                            getThumbnail(bi);
                        }
                    }
                }
                if (totalItemCount == firstVisibleItem + visibleItemCount && !loadingMoreThreads && imMainFragment && totalItemCount != 0 && !recentPostMode) {
                    loadingMoreThreads = true;
                    currentOffset += 10;
                    System.out.println("[Scroll] loading more threads! currentThreadCount " + totalItemCount);
                    getThreadList(currentOffset);
//                    Toast.makeText(getActivity().getApplicationContext(), "Cargando más hilos ...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        hideProgressBar();
        if (boardItems.isEmpty()){
            if (currentBoard == null && currentThread == null && imMainFragment){
                loadRecentPost();
            }else{
                updateBoardItems(currentBoard, currentThread);
            }
        }else{
            listViewAdapter.notifyDataSetChanged();
            recentPostAdapter.notifyDataSetChanged();
        }
        return v;
    }

    private void hideProgressBar(){
        if (loadingBar != null)
            loadingBar.setVisibility(View.GONE);
    }

    private void showProgressBar(){
        if (loadingBar != null)
            loadingBar.setVisibility(View.VISIBLE);
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void updateBoardItems(Board board, BoardItem thread){
        currentBoard = board;
        currentThread = thread;

        if (listViewAdapter != null){
            boardItems.clear();
            listViewAdapter.notifyDataSetChanged();
        }
        if (imMainFragment){
            if (currentBoard != null) {
                System.out.println("[MainFragment] Updating -> boardName: " + board.getBoardName() + " dir: " + board.getBoardDir());
                if (currentThread == null){
                    System.out.println("[MainFragment] isCurrentThread null? (it should be!) " + (currentThread == null));
                }
                showProgressBar();
                getThreadList(0);
            }else{
                System.out.println("[MainFragment] Trying to update from a null board object");
            }
        }else{
            if (currentBoard != null && currentThread != null){
                System.out.println("atUpdateBoardItems ChildFragment threadID: " + currentThread.getId() + " parentID: " + currentThread.getParentId() + " boardName: " + board.getBoardName() + " " + board.getBoardDir());
                showProgressBar();
                getThreadReplies();
            }else{
                System.out.println("[childFragment] trying to update from null objects");
                System.out.println("[childFragment] isCurrentBoard null? " + (currentBoard == null));
                System.out.println("[childFragment] isCurrentThread null? " + (currentThread == null));
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info.targetView.getParent() == listViewBoardItems){
            BoardItem bi = boardItems.get(info.position);
            Document doc = Jsoup.parse(bi.getMessage());
            String parsedMessage = doc.text();
            switch (item.getItemId()){
                case R.id.menu_copy:
                    ClipboardManager cm = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData cd = ClipData.newPlainText("Reply", parsedMessage);
                    cm.setPrimaryClip(cd);
                    break;
                case R.id.menu_reply:
                    Intent in = new Intent(getActivity().getApplicationContext(), ResponseActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable("theReply", boardItems.get(info.position));
                    b.putBoolean("quoting", true);
                    in.putExtras(b);
                    getActivity().startActivity(in);
                    break;
                case R.id.menu_savereply:
                    try {
                        File txt = new File(Environment.getExternalStorageDirectory().getPath() + "/Bai/" + bi.getParentBoard().getBoardDir() + "_" + bi.getId() + ".txt");
                        FileOutputStream stream = new FileOutputStream(txt);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(stream);
                        outputStreamWriter.write(parsedMessage);
                        outputStreamWriter.close();
                        stream.close();
                        Toast.makeText(getContext(), bi.getParentBoard().getBoardDir() + "_" + bi.getId() + ".txt guardado.", Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case R.id.menu_delpost:
                    deletePost(false, bi);
                    break;
                case R.id.menu_delimage:
                    deletePost(true, bi);
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.lvThreadList){
            getActivity().getMenuInflater().inflate(R.menu.menu_reply, menu);
            return;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    public void refresh() {
        if (recentPostMode){
            boardItems.clear();
            recentPostAdapter.notifyDataSetChanged();
            getRecentPosts();
        }else{
            updateBoardItems(currentBoard, currentThread);
        }
    }

    public void setRecentPostMode() {
        this.recentPostMode = true;
    }

    public void setCatalogMode() {
        if (recentPostMode){
            boardItems.clear();
            listViewBoardItems.setAdapter(listViewAdapter);
            this.recentPostMode = false;
        }
    }

    public void loadRecentPost(){
        setRecentPostMode();
        mListener.updateToolbar("Post recientes");
        boardItems.clear();
        listViewAdapter.clear();
        listViewAdapter.notifyDataSetChanged();
        listViewBoardItems.setAdapter(recentPostAdapter);
        getRecentPosts();
    }

    public boolean getMode() {
        return recentPostMode;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
        void showThread(Board board, BoardItem thread);
        void updateToolbar(Board currentBoard, BoardItem boardItem);
        void updateToolbar(String s);
        void hideActionButton();
        void showActionButton();
        void onThreadList();

        void onThread();

        void onRecentPosts();
    }

    public void scrollToBotton(){
        if (!listViewAdapter.isEmpty()){
            listViewBoardItems.setSelection(boardItems.size());
        }
    }
    public void scrollToTop(){
        if (!listViewAdapter.isEmpty()){
            listViewBoardItems.setSelection(0);
        }
    }

    public void getThreadList(int offset){
        loadingMoreThreads = true;
        showProgressBar();
        String strOffset = "";

        if (offset == 0){
            currentOffset = 0;
            boardItems.clear();
        }else{
            strOffset = "&offset=" + offset;
        }
        setUpThreadProgess();

        final String repliesForCatalog = settings.getString("pref_repliesperthread", "5");
        Ion.with(getContext())
                .load("http://bienvenidoainternet.org/cgi/api/list?dir=" + currentBoard.getBoardDir() + "&replies=" + repliesForCatalog + strOffset)
                .setLogging("getThreadList", Log.INFO)
                .noCache()
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        hideProgressBar();
                        int threadCount = 0;
                        if (e != null) {
                            e.printStackTrace();
                            displayError(e.getMessage());
                        } else {
                            try {
                                JSONObject json = new JSONObject(result);
                                JSONArray threads = json.getJSONArray("threads");
                                threadCount = threads.length();
                                for (int i = 0; i < threads.length(); i++) {
                                    JSONObject thread = threads.getJSONObject(i);
                                    BoardItem item = new BoardItem();
                                    item.setEmail(thread.getString("email"));
                                    item.setFile(thread.getString("file"));
                                    item.setFilesize(thread.getInt("file_size"));
                                    item.setId(thread.getInt("id"));
                                    item.setName(thread.getString("name"));
                                    item.setSubject(thread.getString("subject"));
                                    item.setThumb(thread.getString("thumb"));
                                    item.setThumbHeight(thread.getInt("thumb_height"));
                                    item.setThumbWidth(thread.getInt("thumb_width"));
                                    item.setTimeStamp(thread.getLong("timestamp"));
                                    item.setTotalFiles(thread.getInt("total_files"));
                                    item.setTotalReplies(thread.getInt("total_replies"));
                                    item.setTripcode(thread.getString("tripcode"));
                                    item.setTimeStampFormatted(thread.getString("timestamp_formatted"));
                                    item.setLockStatus(thread.getInt("locked"));
                                    if (item.getTimeStampFormatted().contains("ID")){
                                        item.setPosterId(item.getTimeStampFormatted().split(" ")[1].replace("ID :", ""));
                                    }
                                    item.setParentBoard(currentBoard);
                                    item.setParentId(0);
                                    item.setIdColor(addReplyID(item.getPosterId()));
                                    if (currentBoard.getBoardType() == 1){
                                        item.setBbsId(1);
                                    }
                                    item.setMessage(thread.getString("message"));
                                    boardItems.add(item);
                                    if (!repliesForCatalog.equals("0")){
                                        JSONArray replies = thread.getJSONArray("replies");
                                        for (int r = 0; r < replies.length(); r++){
                                            JSONObject jReply = replies.getJSONObject(r);
                                            BoardItem reply = new BoardItem();
                                            reply.setDeletedCode(jReply.getInt("IS_DELETED"));
                                            if (currentBoard.getBoardType() == 1){
                                                reply.setBbsId(item.getTotalReplies() - (Integer.valueOf(repliesForCatalog) - r) + 2);
                                            }
                                            if (reply.getDeletedCode() == 0){
                                                reply.setEmail(jReply.getString("email"));
                                                reply.setFile(jReply.getString("file"));
                                                reply.setFilesize(jReply.getInt("file_size"));
                                                reply.setId(jReply.getInt("id"));
                                                reply.setParentId(item.getId());
                                                reply.setLockStatus(item.isLocked ? 1 : 0);
                                                reply.setName(jReply.getString("name"));
                                                reply.setSubject(jReply.getString("subject"));
                                                reply.setThumb(jReply.getString("thumb"));
                                                reply.setThumbHeight(jReply.getInt("thumb_height"));
                                                reply.setThumbWidth(jReply.getInt("thumb_width"));
                                                reply.setTimeStamp(jReply.getLong("timestamp"));
                                                reply.setTripcode(jReply.getString("tripcode"));
                                                reply.setParentBoard(currentBoard);
                                                reply.setTimeStampFormatted(jReply.getString("timestamp_formatted"));
                                                reply.isReply = true;
                                                if (reply.getTimeStampFormatted().contains("ID")){
                                                    reply.setPosterId(reply.getTimeStampFormatted().split(" ")[1].replace("ID:", ""));
                                                }
                                                reply.setIdColor(addReplyID(reply.getPosterId()));
                                                //
                                                reply.setTotalReplies(item.getTotalReplies());
                                                reply.setMessage(jReply.getString("message"));
                                            }else{
                                                reply.setTimeStamp(jReply.getLong("timestamp"));
                                                reply.setId(jReply.getInt("id"));
                                                reply.isReply = true;
                                                reply.setLockStatus(item.isLocked ? 1 : 0);
                                            }
                                            boardItems.add(reply);
                                        }
                                    }
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                displayError(e1.getMessage());
                            }
                        }
                        listViewAdapter.notifyDataSetChanged();
                        listViewAdapter.updateBoardItems(boardItems);
                        mListener.onThreadList();
                        if (threadCount != 0){
                            loadingMoreThreads = false;
                        }
                        if (boardItems.isEmpty()){
                            mListener.updateToolbar(currentBoard, currentThread);
                        }
                    }
                });


    }

    private void getThreadReplies() {
        showProgressBar();
        boardItems.clear();
        setUpThreadProgess();
        int limit = Integer.valueOf(settings.getString("pref_lastreplies", "1000"));
        int parentTotalReplies = currentThread.getTotalReplies(); // TODO: asddas
        String offset = "&offset=0";
        if (limit <= parentTotalReplies){
            offset = "&offset=" + (parentTotalReplies - limit + 1);
        }else{
            limit = 1337;
        }
        final int finalLimit = limit;
        Ion.with(getContext())
                .load("http://bienvenidoainternet.org/cgi/api/thread?id=" + currentThread.realParentId() + "&dir=" + currentThread.getParentBoard().getBoardDir() + "&limit=" + limit + offset)
                .setLogging("getThreadReplies", Log.INFO)
                .noCache()
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null){
                            e.printStackTrace();
                            displayError(e.getMessage());
                        }else{
                            try {
                                JSONObject json = new JSONObject(result);
                                JSONArray thread = json.getJSONArray("posts");
                                for (int i = 0; i < thread.length(); i++){
                                    JSONObject reply = thread.getJSONObject(i);
                                    BoardItem item = new BoardItem();
                                    item.setDeletedCode(reply.getInt("IS_DELETED"));
                                    if (item.getDeletedCode() == 0){
                                        item.setEmail(reply.getString("email"));
                                        item.setFile(reply.getString("file"));
                                        item.setFilesize(reply.getInt("file_size"));
                                        item.setId(reply.getInt("id"));
                                        item.setName(reply.getString("name"));
                                        item.setSubject(reply.getString("subject"));
                                        item.setThumb(reply.getString("thumb"));
                                        item.setThumbHeight(reply.getInt("thumb_height"));
                                        item.setThumbWidth(reply.getInt("thumb_width"));
                                        item.setTimeStamp(reply.getLong("timestamp"));
                                        item.setParentId(json.getInt("id"));
                                        item.setLockStatus(json.getInt("locked"));
                                        item.setTripcode(reply.getString("tripcode"));
                                        item.setTimeStampFormatted(reply.getString("timestamp_formatted"));
                                        if (item.getTimeStampFormatted().contains("ID")){
                                            item.setPosterId(item.getTimeStampFormatted().split(" ")[1].replace("ID:", ""));
                                        }
                                        item.setParentBoard(currentBoard);
                                        item.isReply = true;
                                        item.setIdColor(addReplyID(item.getPosterId()));
                                        item.setTotalReplies(json.getInt("total_replies"));
                                        if (currentBoard.getBoardType() == 1){
                                            if (item.getTotalReplies() < finalLimit){
                                                item.setBbsId(i + 1);
                                            }else{
                                                item.setBbsId((item.getTotalReplies() - finalLimit + i) + 2);
                                            }
                                        }
                                        item.setMessage(reply.getString("message"));

                                    } else {
                                        item.setId(reply.getInt("id"));
                                        item.setTimeStamp(reply.getLong("timestamp"));
                                        item.isReply = true;
                                        item.setTotalReplies(json.getInt("total_replies"));
                                        if (currentBoard.getBoardType() == 1){
                                            if (item.getTotalReplies() < finalLimit){
                                                item.setBbsId(i + 1);
                                            }else{
                                                item.setBbsId((item.getTotalReplies() - finalLimit + i) + 2);
                                            }
                                        }
                                    }
                                    boardItems.add(item);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                displayError(e1.getMessage());
                            }
                        }
                        listViewAdapter.notifyDataSetChanged();
                        listViewAdapter.updateBoardItems(boardItems);
                        if (settings.getBoolean("setting_scrollatnewthread", true)){
                            listViewBoardItems.setSelection(boardItems.size());
                            mListener.showActionButton();
                        }
                        mListener.onThread();
                        hideProgressBar();
                    }
                });
    }

    private void getRecentPosts(){
        boardItems.clear();
        loadingMoreThreads = true;
        setUpThreadProgess();
        String limit =  settings.getString("pref_lastreplies_limit", "30");
        Ion.with(getContext())
                .load("http://bienvenidoainternet.org/cgi/api/last?limit=" + limit)
                .setLogging("getRecentPosts", Log.INFO)
                .noCache()
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null){
                            e.printStackTrace();
                            displayError(e.getMessage());
                        }else{
                            try {
                                JSONObject json = new JSONObject(result);
                                JSONArray posts = json.getJSONArray("posts");
                                for (int i = 0; i < posts.length(); i++){
                                    JSONObject jPost = posts.getJSONObject(i);
                                    BoardItem recentPost = new BoardItem();
                                    recentPost.setEmail(jPost.getString("email"));
                                    recentPost.setFile(jPost.getString("file"));
                                    recentPost.setFilesize(jPost.getInt("file_size"));
                                    recentPost.setId(jPost.getInt("id"));
                                    recentPost.setName(jPost.getString("name"));
                                    recentPost.setSubject(jPost.getString("subject"));
                                    recentPost.setThumb(jPost.getString("thumb"));
                                    recentPost.setThumbHeight(jPost.getInt("thumb_height"));
                                    recentPost.setThumbWidth(jPost.getInt("thumb_width"));
                                    recentPost.setTimeStamp(jPost.getLong("timestamp"));
                                    recentPost.setTripcode(jPost.getString("tripcode"));
                                    recentPost.setTimeStampFormatted(jPost.getString("timestamp_formatted"));
                                    if (recentPost.getTimeStampFormatted().contains("ID")){
                                        recentPost.setPosterId(recentPost.getTimeStampFormatted().split(" ")[1].replace("ID:", ""));
                                    }
                                    recentPost.setParentBoard(((MainActivity) getActivity()).getBoardFromDir(jPost.getString("dir")));
                                    recentPost.setParentId(jPost.getInt("parentid"));
                                    recentPost.setMessage(jPost.getString("message"));
                                    boardItems.add(recentPost);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                displayError(e1.getMessage());
                            }
                        }
                        recentPostAdapter.notifyDataSetChanged();
                        mListener.onRecentPosts();
                    }
                });

    }

    private void getThumbnail(final BoardItem bi){
        bi.downloadingThumb = true;
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        boolean usingWifi = (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);

        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        File directory = cw.getDir("thumbs", Context.MODE_PRIVATE);
        if (!directory.exists()){
            directory.mkdir();
        }
        final File mypath;
        if (bi.youtubeLink){
            mypath = new File(directory, currentBoard.getBoardDir() + "_" + bi.youtubeID);
        }else{
            mypath = new File(directory, currentBoard.getBoardDir() + "_" + bi.getThumb());
        }

        if (mypath.exists()){
            try {
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(mypath));
//                bi.setThumbBitmap(Bitmap.createScaledBitmap(b, 128, 128, false));
                bi.setThumbBitmap(b);
                listViewAdapter.notifyDataSetChanged();
                Log.i("getThumb", bi.getThumb() + " from cache");
                return;
            }catch (Exception e){
                e.printStackTrace();
                displayError(e.getMessage());
            }
        }
        if (settings.getBoolean("setting_downloadOnlyWithWifi", false) == true && !usingWifi){
            Log.i("getThumb", "Not using wifi");
            return;
        }
        boolean mobileThumbs = settings.getBoolean("pref_usemobilethumbs", true);
        String imgURL = "http://bienvenidoainternet.org/" + bi.getParentBoard().getBoardDir() + (mobileThumbs ? "/mobile/" : "/thumb/") + bi.getThumb();
        if (bi.getThumb().startsWith("http")){
            imgURL = bi.getThumb();
        }
        Ion.with(getContext())
                .load(imgURL)
                .setLogging("getThumbnail", Log.INFO)
                .asBitmap()
                .setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        if (e != null) {
                            displayError(e.getMessage());
                            e.printStackTrace();
                        }else{
                            bi.setThumbBitmap(result);//Bitmap.createScaledBitmap(result, 128, 128, false));
                            listViewAdapter.notifyDataSetChanged();
                            FileOutputStream out;
                            try{
                                out = new FileOutputStream(mypath);
                                result.compress(Bitmap.CompressFormat.PNG, 100, out);
                                if(out != null){
                                    out.close();
                                }
                                Log.v("getThumb", bi.getThumb() + " saved.");
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void deletePost(final boolean imageOnly, BoardItem reply) {
        String password =  settings.getString("pref_password", "12345678");
        Ion.with(getContext())
                .load("http://bienvenidoainternet.org/cgi/api/delete?dir=" + currentThread.getParentBoard().getBoardDir() + "&id=" + reply.getId() + "&password=" + password + "&imageonly=" + (imageOnly ? 1 : 0))
                .setLogging("deletePost", Log.INFO)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            e.printStackTrace();
                            displayError(e.getMessage());
                        } else {
                            JSONObject json = null;
                            try {
                                json = new JSONObject(result);
                                if (json.getString("state").equals("success")) {
                                    Toast.makeText(getContext(), imageOnly ? "Imágen" : "Respuesta" + " eliminada", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), URLDecoder.decode(json.getString("message"), "UTF-8"), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                displayError(e1.getMessage());
                            } catch (UnsupportedEncodingException e1) {
                                e1.printStackTrace();
                                displayError(e1.getMessage());
                            }
                        }
                    }
                });
    }



    public int addReplyID(String s){
        if (!idList.contains(new ReplyID(s, tm))){
            idList.add(new ReplyID(s, tm));
        }
        for (ReplyID r : idList){
            if (r.id.equals(s)){return r.color;}
        }
        return 0;
    }

    private void setUpThreadProgess(){
        txtThreadProcess.setVisibility(View.GONE);
        layoutThreadProcess.setVisibility(View.VISIBLE);
    }

    private void displayError(String error){
        hideProgressBar();
        if (error != null){
            layoutThreadProcess.setVisibility(View.VISIBLE);
            txtThreadProcess.setVisibility(View.VISIBLE);
            txtThreadProcess.setText("( ; u ; )　\r\n/!\\ ERROR\r\n" + error);
        }

    }
}
