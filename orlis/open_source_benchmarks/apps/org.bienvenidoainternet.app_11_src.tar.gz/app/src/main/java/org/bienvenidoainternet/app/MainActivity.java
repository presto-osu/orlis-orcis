package org.bienvenidoainternet.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.bienvenidoainternet.app.structure.Board;
import org.bienvenidoainternet.app.structure.BoardItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import layout.FragmentBoardItemList;

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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentBoardItemList.OnFragmentInteractionListener {
    private ViewPager pager; // variable del ViewPager
    CustomFragmentPagerAdapter pagerAdapter; // Adaptador del ViewPager
    NavigationView navigationView;
    DrawerLayout drawer;
    FloatingActionButton fab;
    public ThemeManager themeManager;
    FragmentBoardItemList childFragment; // Segunda página del ViewPager, se muestra un solo hilo (selecionado del catálogo)
    FragmentBoardItemList mainFragment; // Primera página del ViewPager, se muestra una lista de hilos. (catálogo)
    Toolbar toolbar = null;
    public int currentThemeId = 0, themeId = 0; // Id del recurso, Id del tema
    public ArrayList<Board> boardList = new ArrayList<>();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentThemeId", currentThemeId);
        outState.putInt("themeId", themeId);
        outState.putParcelableArrayList("boardList", boardList);
        if (getSupportFragmentManager().getFragments() != null) {
            if (getSupportFragmentManager().getFragments().size() != 0) {
                try {
                    getSupportFragmentManager().putFragment(outState, "mainFragment", mainFragment);
                    getSupportFragmentManager().putFragment(outState, "childFragment", childFragment);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public int getCurrentThemeId() {
        return currentThemeId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        Ion.getDefault(getApplicationContext()).configure().setLogging("IonLog", Log.DEBUG);
        ThemeManager tm = new ThemeManager(this);
        this.setTheme(tm.getThemeForMainActivity());

        if (savedInstanceState != null) {
            currentThemeId = savedInstanceState.getInt("currentThemeId");
            boardList = savedInstanceState.getParcelableArrayList("boardList");
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getString("pref_password", "").isEmpty()){
            SharedPreferences.Editor edit = settings.edit();
            edit.putString("pref_password", makePassword());
            edit.commit();
        }

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Bievenido a internet");
        this.setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pager.getCurrentItem() == 0){
                    if (!mainFragment.getMode() && mainFragment.currentBoard != null){
                        Intent in = new Intent(getApplicationContext(), ResponseActivity.class);
                        Bundle b = new Bundle();
                        BoardItem temp = new BoardItem();
                        temp.setParentBoard(mainFragment.currentBoard);
                        b.putParcelable("theReply", temp);
                        b.putBoolean("quoting", false);
                        b.putBoolean("newthread", true);
                        in.putExtras(b);
                        startActivity(in);
                    }
                }else{
                    if (childFragment.currentBoard != null) {
                        if (!childFragment.boardItems.isEmpty()) {
                            try {
                                Intent in = new Intent(getApplicationContext(), ResponseActivity.class);
                                Bundle b = new Bundle();
                                BoardItem reply = childFragment.boardItems.get(0);
                                if (!reply.isLocked) {
                                    b.putParcelable("theReply", reply);
                                    b.putBoolean("quoting", false);
                                    b.putBoolean("newthread", false);
                                    in.putExtras(b);
                                    startActivity(in);
                                }else{
                                    Toast.makeText(getApplicationContext(), "Error: Este hilo está cerrado", Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        fab.setVisibility(View.GONE);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null) {
            mainFragment = (FragmentBoardItemList) getSupportFragmentManager().getFragment(savedInstanceState, "mainFragment");
            childFragment = (FragmentBoardItemList) getSupportFragmentManager().getFragment(savedInstanceState, "childFragment");
        } else {
            mainFragment = FragmentBoardItemList.newInstance(true, null, null);
            childFragment = FragmentBoardItemList.newInstance(false, null, null);
        }

        this.pager = (ViewPager) findViewById(R.id.pager);
        this.pagerAdapter = new CustomFragmentPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(mainFragment);
        pagerAdapter.addFragment(childFragment);
        this.pager.setAdapter(pagerAdapter);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0){
                    fab.setImageResource(R.drawable.ic_action_add);
                    if (mainFragment.currentBoard != null) {
                        toolbar.setTitle("Catálogo");
                        toolbar.setSubtitle(mainFragment.currentBoard.getBoardName());
                    }
                    if (mainFragment.getMode()){
                        toolbar.setTitle("Post recientes");
                        toolbar.setSubtitle("");
                    }
                    if (mainFragment.getMode()){
                        fab.hide();
                    }else{
                        fab.show();
                    }
                }else if (position == 1){
                    fab.setImageResource(R.drawable.ic_edit);
                    if (childFragment.currentBoard != null) {
                        toolbar.setTitle(childFragment.currentBoard.getBoardName());
                        if (!childFragment.boardItems.isEmpty()){
                            toolbar.setSubtitle(childFragment.boardItems.get(0).getSubject());
                        }
                        fab.show();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (boardList.isEmpty()){
            getBoardList();
        }else{
            Menu menu = navigationView.getMenu();
            SubMenu sub = menu.addSubMenu("Lista de Boards");
            for (Board b : boardList) {
                sub.add(b.getBoardName());
            }
            refreshNavigator();
        }

        if (tm.isDarkTheme()){
//            navigationView.setBackgroundColor(0xFF888888);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                boolean result = data.getBooleanExtra("result", false);
                if (result){
                    this.recreate();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (this.pager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            this.pager.setCurrentItem(this.pager.getCurrentItem() - 1);
            return;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_exit:
                System.exit(0);
                break;
            case R.id.action_refresh:
                if (pager.getCurrentItem() == 0) {
                    mainFragment.refresh();
                } else {
                    childFragment.refresh();
                }
                if (boardList.isEmpty()){
                    getBoardList();
                }
                break;
            case R.id.action_settings:
                Intent in2 = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(in2, 1);
                break;
            case R.id.action_to_bot:
                if (pager.getCurrentItem() == 0) {
                    mainFragment.scrollToBotton();
                } else {
                    childFragment.scrollToBotton();
                }
                break;
            case R.id.action_to_top:
                if (pager.getCurrentItem() == 0) {
                    mainFragment.scrollToTop();
                } else {
                    childFragment.scrollToTop();
                }
                break;
            case R.id.action_about:
                startActivity(new Intent(getApplicationContext(), LicensesActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        int id = item.getItemId();
        toolbar.setSubtitle(item.getTitle());
        if (id == R.id.nav_recent_post){
            toolbar.setTitle("Post recientes");
            toolbar.setSubtitle("");
            pager.setCurrentItem(0);
            mainFragment.loadRecentPost();
        }
        for (Board b : boardList){
            if (b.getBoardName() == item.getTitle()){
                System.out.println("Updating mainfragment to " + b.getBoardName() + " d: " + b.getBoardDir());
                mainFragment.setCatalogMode();
                mainFragment.updateBoardItems(b, null);
                pager.setCurrentItem(0);
                navigationView.getMenu().findItem(R.id.nav_recent_post).setChecked(false);
            }
        }
        return true;
    }

    public Board getBoardFromDir(String dir){
        for (Board b : boardList){
            if (b.getBoardDir().equals(dir)){
                return b;
            }
        }
        System.out.println("[MainActivity] Board not found " + dir);
        return null;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void showThread(Board board, BoardItem thread) {
        childFragment.updateBoardItems(board, thread);
        pager.setCurrentItem(1);
    }


    @Override
    public void updateToolbar(Board cBoard, BoardItem btem) {
        if (pager.getCurrentItem() == 1){
            toolbar.setTitle(cBoard.getBoardName());
            toolbar.setSubtitle(btem.getSubject());
        }
    }

    @Override
    public void updateToolbar(String s) {
        toolbar.setTitle(s);
        toolbar.setSubtitle("");
    }

    @Override
    public void hideActionButton() {
        if (pager.getCurrentItem() == 1){
            fab.hide();
        }
    }

    @Override
    public void showActionButton() {
        if (pager.getCurrentItem() == 1){
            fab.show();
        }
    }

    @Override
    public void onThreadList() {
        if (pager.getCurrentItem() == 0){
            getSupportActionBar().setTitle("Catálogo");
            getSupportActionBar().setSubtitle(mainFragment.currentBoard.getBoardName());
            fab.show();
        }
    }

    @Override
    public void onThread() {
        if (pager.getCurrentItem() == 1){
            getSupportActionBar().setTitle(childFragment.currentBoard.getBoardName());
            getSupportActionBar().setSubtitle(childFragment.boardItems.get(0).getSubject());
        }
    }

    @Override
    public void onRecentPosts() {
        if (pager.getCurrentItem() == 0){
            getSupportActionBar().setTitle("Post recientes");
            getSupportActionBar().setSubtitle("");
            fab.hide();
        }
    }

    private void getBoardList(){
        Menu menu = navigationView.getMenu();
        final SubMenu sub = menu.addSubMenu("Lista de Boards");
        Ion.with(getApplicationContext())
                .load("http://bienvenidoainternet.org/cgi/api/boards")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }else {
                            try {
                                JSONArray boards = new JSONObject(result).getJSONArray("boards");
                                for (int i = 0; i < boards.length(); i++) {
                                    JSONObject board = boards.getJSONObject(i);
                                    Board parsedBoard = new Board(board.getString("name"), board.getString("dir"), board.getInt("board_type"), board.getInt("allow_image_replies") == 1 ? true : false);
                                    sub.add(parsedBoard.getBoardName());
                                    boardList.add(parsedBoard);
                                }
//                                Board polka = new Board("Testing", "polka", 0, true);
//                                boardList.add(polka);
//                                sub.add("Testing");

                            }catch (JSONException e1) {
                                Toast.makeText(getApplicationContext(), e1.getMessage(), Toast.LENGTH_LONG).show();
                                e1.printStackTrace();
                            }
                        }
                    }
                });
        refreshNavigator();
    }

    public void refreshNavigator(){
        for (int i = 0, count = navigationView.getChildCount(); i < count; i++) {
            final View child = navigationView.getChildAt(i);
            if (child != null && child instanceof ListView) {
                final ListView menuView = (ListView) child;
                final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                wrapped.notifyDataSetChanged();
            }
        }
    }
    
    /*
            Crea una secuencia de caracteres de 8 digitos aleatorios (incluye mayusculas, minisculas y numeros).
    */
    public String makePassword(){
        Random r = new Random();
        String rnd = "";
        for (int i = 0; i < 8; i++){
            int a = r.nextInt(3);
            char b;
            if (a == 0){
                b = (char)(66 + r.nextInt(25));
            }else if (a == 1){
                b = (char)(97 + r.nextInt(25));
            }else{
                b = (char) (48 + r.nextInt(9));
            }
            rnd = rnd + b;
        }
        return rnd;
    }
}
