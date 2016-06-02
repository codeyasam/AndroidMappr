package com.example.codeyasam.mappr;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapprPlotter extends AppCompatActivity implements OnMapReadyCallback {

    private static final String PLOTTER_URL = CYM_UtilityClass.MAPPR_ROOT_URL + "tests/getMarkerOptions.php";
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_qr_place);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String mappr_opt = getIntent().getStringExtra(CYM_UtilityClass.MAPPR_OPT);
        MarkerPlotter task = new MarkerPlotter();
        task.setMapperOpt(mappr_opt);
        if (mappr_opt.equals(CYM_UtilityClass.OPT_BY_QRCODE)) {
            String branchID = getIntent().getStringExtra("branchID");
            task.setBranchID(branchID);
        } else if (mappr_opt.equals(CYM_UtilityClass.OPT_BY_CATEGORY)) {
            String categoryID = getIntent().getStringExtra("categoryID");
            task.setCategoryID(categoryID);
        }
        task.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    class MarkerPlotter extends AsyncTask<String, String, String> {

        private String mapperOpt;
        private String branchID;
        private String categoryID;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                if (mapperOpt.equals(CYM_UtilityClass.OPT_BY_QRCODE)) {
                    Log.i("POOP", "branchID: " + branchID);
                    params.add(new BasicNameValuePair(CYM_UtilityClass.MAPPR_OPT, CYM_UtilityClass.OPT_BY_QRCODE));
                    params.add(new BasicNameValuePair("branch_id", branchID));
                    JSONObject json = JSONParser.makeHttpRequest(PLOTTER_URL, "GET", params);
                    JSONArray branches = json.getJSONArray("Branches");
                    return branches.toString();
                } else if (mapperOpt.equals(CYM_UtilityClass.OPT_BY_CATEGORY)) {
                    params.add(new BasicNameValuePair(CYM_UtilityClass.MAPPR_OPT, CYM_UtilityClass.OPT_BY_CATEGORY));
                    params.add(new BasicNameValuePair("category_id", categoryID));
                    JSONObject json = JSONParser.makeHttpRequest(PLOTTER_URL, "GET", params);
                    JSONArray mapprEstabs = json.getJSONArray("Branches");
                    return mapprEstabs.toString();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String branchesString) {
            if (branchesString != null) {
                try {
                    Log.i("poop", branchesString);
                    JSONArray branches = new JSONArray(branchesString);
                    JSONObject firstBranch = branches.getJSONObject(0);
                    LatLng ll = new LatLng(Double.parseDouble(firstBranch.getString("lat")), Double.parseDouble(firstBranch.getString("lng")));
                    for (int i = 0; i < branches.length(); i++) {
                        JSONObject eachBranch = branches.getJSONObject(i);
                        LatLng latlng = new LatLng(Double.parseDouble(eachBranch.getString("lat")), Double.parseDouble(eachBranch.getString("lng")));
                        MarkerOptions options = new MarkerOptions()
                                .title(eachBranch.getString("address"))
                                .position(latlng);

                        if (mapperOpt.equals(CYM_UtilityClass.OPT_BY_QRCODE)) {
                            if (eachBranch.getString("id").equals(branchID)) {
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                ll = new LatLng(Double.parseDouble(eachBranch.getString("lat")), Double.parseDouble(eachBranch.getString("lng")));
                            }
                        }

                        mMap.addMarker(options);
                    }
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 10);
                    mMap.moveCamera(update);
                } catch(JSONException e) {
                   e.printStackTrace();
                }
            }
        }

        public void setMapperOpt(String mapperOpt) {
            this.mapperOpt = mapperOpt;
        }

        public String getMapperOpt() {
            return this.mapperOpt;
        }

        public void setBranchID(String branchID) {
            this.branchID = branchID;
        }

        public void setCategoryID(String categoryID) {
            this.categoryID = categoryID;
        }
    }

}
