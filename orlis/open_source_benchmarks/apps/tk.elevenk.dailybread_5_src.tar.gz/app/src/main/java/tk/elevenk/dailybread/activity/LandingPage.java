/*
 * LandingPage.java is a part of DailybRead
 *     Copyright (C) 2015  John Krause, Eleven-K Software
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.elevenk.dailybread.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Date;
import tk.elevenk.dailybread.R;
import tk.elevenk.dailybread.fragment.NavigationDrawerFragment;
import tk.elevenk.dailybread.fragment.SavedBooksFragment;
import tk.elevenk.dailybread.fragment.SearchFragment;
import tk.elevenk.dailybread.fragment.TodaysbReadFragment;
import tk.elevenk.dailybread.fragment.reader.MainReaderFragment;
import tk.elevenk.dailybread.preferences.AndroidLibraryPreferences;
import tk.elevenk.olapi.OpenLibrary;
import tk.elevenk.olapi.data.BookData;


public class LandingPage extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static OpenLibrary library;
    private AlertDialog donateDialog;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private TodaysbReadFragment todaysbReadFragment;
    private SavedBooksFragment savedBooksFragment;
    private SearchFragment searchFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        library = OpenLibrary.androidLibrary(new AndroidLibraryPreferences(this.getPreferences(MODE_PRIVATE)));

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getString(R.string.title_todays_bread);

        todaysbReadFragment = new TodaysbReadFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_content_layout, todaysbReadFragment).commit();

        savedBooksFragment = new SavedBooksFragment();
        searchFragment = new SearchFragment();
        searchFragment.setLibrary(library);
        createDonateDialog();
        restoreActionBar();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        getSupportFragmentManager().executePendingTransactions();
        todaysbReadFragment.randomSearch(library);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        String positionTitle = getResources().getStringArray(R.array.navigation_menu_items)[position];
        // update the main content by replacing fragments
        Fragment replacement = null;
        //TODO add other section fragments
        if (positionTitle.equals(getString(R.string.title_todays_bread))) {
            mTitle = positionTitle;
            replacement = todaysbReadFragment;
        } else if (positionTitle.equals(getString(R.string.title_saved_books))) {
            mTitle = positionTitle;
            replacement = savedBooksFragment;
        } else if (positionTitle.equals(getString(R.string.title_search))) {
            mTitle = positionTitle;
            replacement = searchFragment;
        } else if (positionTitle.equals(getString(R.string.title_donate))) {
            displayDonateDialog();
        }

        if (replacement != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_content_layout, replacement);
            transaction.addToBackStack(null);
            transaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    private void createDonateDialog(){
        final TextView donateDialogMessage = new TextView(this);
        final SpannableString s = new SpannableString(getString(R.string.donate_text, null));
        Linkify.addLinks(s, Linkify.ALL);
        donateDialogMessage.setText(s);
        donateDialogMessage.setMovementMethod(LinkMovementMethod.getInstance());
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setView(donateDialogMessage);
        dialogBuilder.setTitle(R.string.donate_title);
        dialogBuilder.setNeutralButton(R.string.action_close, null);
        donateDialog = dialogBuilder.create();
    }

    public void displayDonateDialog() {
        donateDialog.show();
    }

    public void displayHelpDialog() {
        final TextView message = new TextView(this);
        final SpannableString s = new SpannableString(getText(R.string.help_text));
        Linkify.addLinks(s, Linkify.WEB_URLS);
        message.setText(s);
        message.setMovementMethod(LinkMovementMethod.getInstance());
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setView(message);
        dialogBuilder.setTitle(R.string.help_title);
        dialogBuilder.setNeutralButton(R.string.action_close, null);
        dialogBuilder.create().show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
        }
        getMenuInflater().inflate(R.menu.global, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_bread) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content_layout, todaysbReadFragment).commit();
            getSupportFragmentManager().executePendingTransactions();
            todaysbReadFragment.randomSearch(library);
            return true;
        } else if (id == R.id.action_save_book) {
            boolean bookSaved = false;
            Fragment bookFragment = getSupportFragmentManager().findFragmentById(R.id.main_content_layout);
            if (bookFragment instanceof TodaysbReadFragment && ((TodaysbReadFragment) bookFragment).isSearching()) {
                Toast.makeText(this, "Still searching...", Toast.LENGTH_SHORT).show();
            } else {
                if (bookFragment instanceof MainReaderFragment) {
                    bookSaved = ((MainReaderFragment) bookFragment).saveCurrentBook();
                }
                if (!bookSaved) {
                    // TODO better error message
                    Toast.makeText(this, "Error saving book.", Toast.LENGTH_LONG).show();
                } else {
                    // TODO better saved message
                    Toast.makeText(this, "Book saved!", Toast.LENGTH_LONG).show();
                }
            }
            return true;
        } else if (id == R.id.action_book_info) {
            Fragment bookFragment = getSupportFragmentManager().findFragmentById(R.id.main_content_layout);
            if (bookFragment instanceof TodaysbReadFragment && ((TodaysbReadFragment) bookFragment).isSearching()) {
                Toast.makeText(this, "Still searching...", Toast.LENGTH_SHORT).show();
            } else if (bookFragment instanceof MainReaderFragment) {
                //TODO make better info dialog/fragment
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                String info = "";
                BookData bookData = ((MainReaderFragment) bookFragment).getBookData();
                if (bookData != null && bookData.getTitle() != null && !bookData.getTitle().isEmpty()) {
                    info += "Title: " + bookData.getTitle() + "\n\nAuthors: ";
                    List<String> authors = bookData.getAuthorNames();
                    if (authors != null) {
                        for (String name : authors) {
                            info += name + ", ";
                        }
                    } else {
                        List<Map<String, String>> authorsMap = bookData.getAuthorsMap();
                        if (authorsMap != null) {
                            for (Map map : authorsMap) {
                                if (map.get("name") != null)
                                    info += map.get("name") + ", ";
                            }
                        }
                    }
                    info += "\n\nFirst Published: " + bookData.getFirstPublishYear() +
                            "\n\nPublishers: ";
                    List<String> publishers = bookData.getPublishers();
                    if (publishers != null) {
                        for (String publisher : publishers) {
                            info += publisher + ", ";
                        }
                    }
                } else {
                    Object book = ((MainReaderFragment) bookFragment).getCurrentBook();
                    if (book != null && book instanceof Book) {
                        info += "Title: " + ((Book) book).getTitle() + "\n\nAuthors: ";
                        List<Author> authors = ((Book) book).getMetadata().getAuthors();
                        if (authors != null) {
                            for (Author author : authors) {
                                info += author.getFirstname() + " " + author.getLastname() + ", ";
                            }
                        }
                        info += "\n\nPublished: ";
                        List<Date> dates = ((Book) book).getMetadata().getDates();
                        if (dates != null) {
                            for (Date date : dates) {
                                info += date.getValue() + ", ";
                            }
                        }
                        info += "\n\nPublishers: ";
                        List<String> publishers = ((Book) book).getMetadata().getPublishers();
                        if (publishers != null) {
                            for (String publisher : publishers) {
                                info += publisher + ", ";
                            }
                        }

                    }
                }

                builder.setMessage(info);
                if (bookFragment instanceof TodaysbReadFragment) {
                    builder.setTitle("Found from query: " + todaysbReadFragment.getRandomSearchWord());
                } else {
                    builder.setTitle("Book Info");
                }
                builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
            return true;
        } else if (id == R.id.action_quit) {
            this.finish();
            return true;
        } else if (id == R.id.action_help) {
            displayHelpDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            List<String> menuItems = Arrays.asList(getResources().getStringArray(R.array.navigation_menu_items));
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().executePendingTransactions();
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_content_layout);
            if (currentFragment instanceof TodaysbReadFragment) {
                mTitle = getString(R.string.title_todays_bread);
                mNavigationDrawerFragment.onFragManPop(menuItems.indexOf(mTitle));
            } else if (currentFragment instanceof SavedBooksFragment) {
                mTitle = getString(R.string.title_saved_books);
                mNavigationDrawerFragment.onFragManPop(menuItems.indexOf(mTitle));
            } else if (currentFragment instanceof MainReaderFragment) {
                mTitle = getString(R.string.title_reader);
            }
            restoreActionBar();
        }
    }
}
