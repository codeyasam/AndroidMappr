package com.example.codeyasam.mappr;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapprFavorites extends AppCompatActivity {

    private static final String FAVORITES_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/getBookmarks.php";

    private Map<String, MapprEstablishment> estabHm = new HashMap<>();
    private List<MapprBranch> branchesList;
    private SharedPreferences settings;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappr_favorites);
        listView = (ListView)findViewById(R.id.favBranchList);

    }

    @Override
    protected void onResume() {
        super.onResume();
        branchesList = new ArrayList<>();
        settings = PreferenceManager.getDefaultSharedPreferences(MapprFavorites.this);
        String userId = settings.getString(MapprSession.LOGGED_USER_ID, "");
        if (userId.isEmpty()) {
            TextView tv = (TextView) findViewById(R.id.emptyBookmarkTxt);
            tv.setText("You aren't logged in.");
            tv.setVisibility(View.VISIBLE);

            Button loginBtn = (Button) findViewById(R.id.loginBtn);
            loginBtn.setVisibility(View.VISIBLE);

            Log.i("poop", "no user id");
            return;
        }
        new FavoritesLoader(userId).execute();
    }

    public void loginClick(View v) {
        Intent intent = new Intent(MapprFavorites.this, MapprLogin.class);
        intent.putExtra(CYM_Utility.MAPPR_FORM, CYM_Utility.FROM_FAVORITES);
        startActivity(intent);
    }

    private boolean setEstabHm(JSONArray estabs) {
        if (!estabs.toString().equals("[{}]")) {
            try {
                for (int i = 0; i < estabs.length(); i++) {
                    JSONObject eachEstab = estabs.getJSONObject(i);
                    MapprEstablishment estabObj = MapprEstablishment.instantiateJSONEstablishment(eachEstab);
                    estabHm.put(eachEstab.getString("id"), estabObj);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean setBranchesList(JSONArray branches) {
        if (!branches.toString().equals("[{}]")) {
            try {
                for (int i = 0; i < branches.length(); i++) {
                    JSONObject eachBranch = branches.getJSONObject(i);
                    MapprBranch branch = MapprBranch.instantiateBranch(eachBranch, (HashMap) estabHm);
                    branchesList.add(branch);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    class FavoritesLoader extends AsyncTask<String, String, String> {

        private String userId;

        public FavoritesLoader(String userId) {
            this.userId = userId;
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("user_id", userId));
                JSONObject json = JSONParser.makeHttpRequest(FAVORITES_URL, "GET", params);
                JSONArray estabs = json.getJSONArray("Establishments");
                JSONArray branches = json.getJSONArray("Branches");
                setEstabHm(estabs);
                setBranchesList(branches);
                return json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    Log.i("poop", result);
                    if (!branchesList.isEmpty()) {
                        findViewById(R.id.favBranchList).setVisibility(View.VISIBLE);
                        ArrayAdapter<MapprBranch> adapter = new FavoriteAdapter(MapprFavorites.this, branchesList);
                        listView.setAdapter(adapter);
                    } else {
                        findViewById(R.id.emptyBookmarkTxt).setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MapprFavorites.this, MapprCategory.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
