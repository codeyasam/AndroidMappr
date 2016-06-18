package com.example.codeyasam.mappr;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    private List<MapprBranch> branchesList = new ArrayList<>();
    private SharedPreferences settings;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappr_favorites);
        listView = (ListView)findViewById(R.id.listView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = PreferenceManager.getDefaultSharedPreferences(MapprFavorites.this);
        String userId = settings.getString(MapprSession.LOGGED_USER_ID, "");
        if (userId.isEmpty()) {

            return;
        }
        new FavoritesLoader(userId).execute();
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
                    ArrayAdapter<MapprBranch> adapter = new FavoriteAdapter(MapprFavorites.this, branchesList);
                    listView.setAdapter(adapter);
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
}
