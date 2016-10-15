package org.mappr;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.example.codeyasam.mappr.R;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mappr.org.mappr.model.CYM_Utility;
import org.mappr.org.mappr.model.CategoryFragment;
import org.mappr.org.mappr.model.FavoritesFragment;
import org.mappr.org.mappr.model.JSONParser;
import org.mappr.org.mappr.model.MapprBranch;
import org.mappr.org.mappr.model.MapprCategory;
import org.mappr.org.mappr.model.MapprJSONSearch;
import org.mappr.org.mappr.model.MapprSession;
import org.mappr.org.mappr.model.SearchesFragment;
import org.mappr.org.mappr.model.ViewPageAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String SEARCH_SUGGESTIONS_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/getSearchSuggestions.php";

    public static List<MapprCategory> categoryList;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private MenuItem scanMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Drawable drawable = getResources().getDrawable(R.drawable.logo);
        Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
        toolbar.setNavigationIcon(new BitmapDrawable(getResources(), CYM_Utility.getResizedBitmap(bm, 70, 80)));
        toolbar.setTitle("mappr");
        setSupportActionBar(toolbar);

        setupmFragments();
        DateTime dateTime = new DateTime();
        Log.i("poop", String.valueOf(dateTime.getDayOfWeek()));
    }

    private void setupmFragments() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        final TabLayout.Tab category = tabLayout.getTabAt(0);
        category.setIcon(android.R.drawable.ic_dialog_dialer);
        final TabLayout.Tab favorites = tabLayout.getTabAt(1);
        favorites.setIcon(android.R.drawable.star_big_off);
        final TabLayout.Tab recentSearches = tabLayout.getTabAt(2);
        recentSearches.setIcon(android.R.drawable.ic_menu_recent_history);
    }

    //search by string
    private void searchClick(String searchString) {
        Intent intent = new Intent(this.getApplicationContext(), MapActivity.class);
        intent.putExtra(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_STRING);
        intent.putExtra("searchString", searchString);
        startActivity(intent);
    }

    private void scanQrCode() {
        Log.i("poop", "poop");
        Intent intent = new Intent(this.getApplicationContext(), QrCodeScanner.class);
        startActivity(intent);
    }

    private void setupVisibleMenu(Menu menu) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        MenuItem loginMenu = menu.findItem(R.id.to_login);
        MenuItem registerMenu = menu.findItem(R.id.to_register);
        MenuItem logoutMenu = menu.findItem(R.id.to_logout);
        MenuItem editProfile = menu.findItem(R.id.to_edit_profile);
        MenuItem changePass = menu.findItem(R.id.to_change_password);
        if (!settings.getString(MapprSession.LOGGED_USER_ID, "").isEmpty()) {
            loginMenu.setVisible(false);
            registerMenu.setVisible(false);
            logoutMenu.setVisible(true);
            editProfile.setVisible(true);
            changePass.setVisible(true);
        } else {
            logoutMenu.setVisible(false);
            editProfile.setVisible(false);
            changePass.setVisible(false);
            loginMenu.setVisible(true);
            registerMenu.setVisible(true);
        }
        Log.i("poop", "setupVisibleMenu is called");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setupVisibleMenu(menu);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    private SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        scanMenuItem = menu.findItem(R.id.scan_qrcode);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem mSearchMenuItem = menu.findItem(R.id.action_settings);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchMenuItem);
        mSearchView.setSuggestionsAdapter(new SimpleCursorAdapter(
                getApplicationContext(), android.R.layout.simple_list_item_1, null,
                new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 },
                new int[] { android.R.id.text1 }));
        mSearchView.setIconifiedByDefault(false);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                implementSearchHistory(query);
                searchClick(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {
                    new FetchSearchTermSuggestionsTask(newText).execute(newText);
                } else {
                    mSearchView.getSuggestionsAdapter().changeCursor(null);
                }
                return true;
            }
        });

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

            @Override
            public boolean onSuggestionSelect(int position) {
                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
                String term = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                cursor.close();
                searchClick(term);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                return onSuggestionSelect(position);
            }
        });

        return true;
    }

    public class FetchSearchTermSuggestionsTask extends AsyncTask<String, Void, Cursor> {

        private String searchStr;

        public FetchSearchTermSuggestionsTask(String searchStr) {
            this.searchStr = searchStr;
        }

        private final String[] sAutocompleteColNames = new String[] {
                BaseColumns._ID,                         // necessary for adapter
                SearchManager.SUGGEST_COLUMN_TEXT_1      // the full search term
        };

        @Override
        protected Cursor doInBackground(String... args) {
            MatrixCursor cursor = new MatrixCursor(sAutocompleteColNames);

            try {
                JSONObject jsonObject = JSONParser.getJSONfromURL(SEARCH_SUGGESTIONS_URL + "?searchStr=" + searchStr);
                JSONArray jsonArray = jsonObject.getJSONArray("Suggestions");

                for (int i = 0; i < jsonArray.length(); i++) {
                    //String term = jsonArray.getString(index);
                    JSONObject eachJson = jsonArray.getJSONObject(i);
                    String term = eachJson.getString("name");
                    String index = eachJson.getString("id");

                    Object[] row = new Object[] { index, term };
                    cursor.addRow(row);
                }

                return cursor;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            mSearchView.getSuggestionsAdapter().changeCursor(cursor);
        }
    }


    private void implementSearchHistory(String searchValue) {
        MapprJSONSearch mapprJSONSearch = new MapprJSONSearch(CYM_Utility.OPT_BY_STRING, searchValue);
        mapprJSONSearch.setDisplayValue(searchValue);
        mapprJSONSearch.saveSearchRequest(getApplicationContext());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //return true;
            MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    Log.i("poop", "searchview expanded");
                    scanMenuItem.setVisible(false);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    Log.i("poop", "searchview collapsed");
                    scanMenuItem.setVisible(true);
                    return true;
                }
            });
        } else if (id == R.id.scan_qrcode) {
            scanQrCode();
        } else if (id == R.id.to_login) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.to_register) {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
        } else if (id == R.id.to_logout) {
            MapprSession.logoutUser(MainActivity.this);
        } else if (id == R.id.to_edit_profile) {
            //Intent intent = new Intent(getApplicationContext(), );
        } else if (id == R.id.to_change_password) {
            Intent intent = new Intent(getApplicationContext(), ChangePassActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }




    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main2, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
//            return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case 0: return new CategoryFragment();
                case 1: return new FavoritesFragment();
                case 2: return new SearchesFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "CATEGORIES";
                case 1:
                    return "FAVORITES";
                case 2:
                    return "SEARCHES";
            }
            return null;
        }
    }

}
