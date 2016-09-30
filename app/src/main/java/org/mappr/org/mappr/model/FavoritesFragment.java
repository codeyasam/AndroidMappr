package org.mappr.org.mappr.model;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.codeyasam.mappr.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mappr.FavoritesActivity;
import org.mappr.LoginActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by codeyasam on 7/20/16.
 */
public class FavoritesFragment extends Fragment implements View.OnClickListener {

    private static final String FAVORITES_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/getBookmarks.php";

    private View view;

    private Map<String, MapprEstablishment> estabHm = new HashMap<>();
    private List<MapprBranch> branchesList;
    private SharedPreferences settings;

    private ListView listView;
    private FavoritesLoader favoritesLoader;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_favorites, container, false);
        listView = (ListView) view.findViewById(R.id.favBranchList);

        branchesList = new ArrayList<>();
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("poop", "favoriteFragment mainAcvitity onResume");
        setupmView(view);
    }

    private void setupmView(View view) {
        String userId = settings.getString(MapprSession.LOGGED_USER_ID, "");
        TextView tv = (TextView) view.findViewById(R.id.loginTxt);
        if (userId.isEmpty()) {
            tv.setVisibility(View.VISIBLE);
            tv.setOnClickListener(this);
        } else {
            boolean result = CYM_Utility.isOnline(getActivity().getApplicationContext());
            if (result) {
                tv.setVisibility(View.GONE);
                favoritesLoader = new FavoritesLoader(userId);
                favoritesLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                tv.setVisibility(View.VISIBLE);
                tv.setText("No Internet  Connectivity");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginTxt:
                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                break;
        }
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
            branchesList = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView tv = (TextView) view.findViewById(R.id.emptyBookmarkTxt);
            tv.setText("Loading Favorites...");
            tv.setVisibility(View.VISIBLE);
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
                    TextView tv = (TextView) view.findViewById(R.id.emptyBookmarkTxt);
                    tv.setText("Favorites Empty");
                    Log.i("poop", result);
                    if (!branchesList.isEmpty()) {
                        view.findViewById(R.id.emptyBookmarkTxt).setVisibility(View.INVISIBLE);
                        view.findViewById(R.id.favBranchList).setVisibility(View.VISIBLE);
                    } else {
                        view.findViewById(R.id.emptyBookmarkTxt).setVisibility(View.VISIBLE);
                    }
                    ArrayAdapter<MapprBranch> adapter = new FavoriteAdapter(getActivity(), branchesList);
                    listView.setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }

    @Override
    public void onDestroyView() {
        view = null;
        listView = null;
        super.onDestroyView();
    }
}
