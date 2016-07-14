package org.mappr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.codeyasam.mappr.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mappr.org.mappr.model.CYM_Utility;
import org.mappr.org.mappr.model.JSONParser;
import org.mappr.org.mappr.model.MapprEstablishment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String PLOTTER_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/getMarkerOptions.php";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private Map<String, MapprEstablishment> hmEstablishment = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //getMapAsync(onMapReadyCallBack) - to execute onMapReady
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();

        searchRequestHandler();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                MapprEstablishment estab = hmEstablishment.get(marker.getSnippet());
                View v = getLayoutInflater().inflate(R.layout.custom_info_window, null);
                ImageView estabLogo = (ImageView) v.findViewById(R.id.estabLogo);
                TextView estabName = (TextView) v.findViewById(R.id.estabName);
                estabLogo.setImageBitmap(estab.getDisplay_picture());
                estabName.setText(estab.getName());
                return v;
            }
        });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent(MapActivity.this, EstablishmentDetails.class);
        intent.putExtra("branch_id", marker.getTitle());
        intent.putExtra(CYM_Utility.MAPPR_FORM, CYM_Utility.FROM_PLOTTER);
        intent.putExtra("sourceLat", String.valueOf(mLastLocation.getLatitude()));
        intent.putExtra("sourceLng", String.valueOf(mLastLocation.getLongitude()));
        intent.putExtra("destLat", String.valueOf(marker.getPosition().latitude));
        intent.putExtra("destLng", String.valueOf(marker.getPosition().longitude));
        //Log.i("poop", marker.getTitle());
        startActivity(intent);
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void searchRequestHandler() {
        String mappr_opt = getIntent().getStringExtra(CYM_Utility.MAPPR_OPT);
        MarkerPlotter task = new MarkerPlotter();
        task.setMapperOpt(mappr_opt);
        if (mappr_opt.equals(CYM_Utility.OPT_BY_QRCODE)) {
            String branchID = getIntent().getStringExtra("branchID");
            task.setBranchID(branchID);
        } else if (mappr_opt.equals(CYM_Utility.OPT_BY_CATEGORY)) {
            String categoryID = getIntent().getStringExtra("categoryID");
            task.setCategoryID(categoryID);
        } else if (mappr_opt.equals(CYM_Utility.OPT_BY_STRING)) {
            String searchString = getIntent().getStringExtra("searchString");
            task.setSearchString(searchString);
        }
        task.execute();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("poop", "here here yeah");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            LatLng ll = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 10);
            mMap.moveCamera(update);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    //handles plotting of markers asynchronously
    class MarkerPlotter extends AsyncTask<String, String, String> {

        private String mapperOpt;
        private String branchID;
        private String categoryID;
        private String searchString;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                if (mapperOpt.equals(CYM_Utility.OPT_BY_QRCODE)) {
                    Log.i("POOP", "branchID: " + branchID);
                    params.add(new BasicNameValuePair(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_QRCODE));
                    params.add(new BasicNameValuePair("branch_id", branchID));
                } else if (mapperOpt.equals(CYM_Utility.OPT_BY_CATEGORY)) {
                    params.add(new BasicNameValuePair(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_CATEGORY));
                    params.add(new BasicNameValuePair("category_id", categoryID));
                } else if (mapperOpt.equals(CYM_Utility.OPT_BY_STRING)) {
                    params.add(new BasicNameValuePair(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_STRING));
                    params.add(new BasicNameValuePair("search_string", searchString));
                }
                JSONObject json = JSONParser.makeHttpRequest(PLOTTER_URL, "GET", params);
                JSONArray estabs = json.getJSONArray("Establishments");

                for (int i = 0; i< estabs.length(); i++) {
                    JSONObject eachEstab = estabs.getJSONObject(i);
                    hmEstablishment.put(eachEstab.getString("id"), MapprEstablishment.instantiateJSONEstablishment(eachEstab));
                }

                JSONArray branches = json.getJSONArray("Branches");
                return branches.toString();
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
                    Log.i("poop", "length: " + branches.length());
                    //kind of a hack
                    if (branchesString.equals("[{}]")) {
                        Toast.makeText(MapActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                        return;
                    } else{
                        Toast.makeText(MapActivity.this, branches.length() + " results", Toast.LENGTH_SHORT).show();
                    }

                    LatLng ll = new LatLng(Double.parseDouble(firstBranch.getString("lat")), Double.parseDouble(firstBranch.getString("lng")));
                    for (int i = 0; i < branches.length(); i++) {
                        JSONObject eachBranch = branches.getJSONObject(i);
                        LatLng latlng = new LatLng(Double.parseDouble(eachBranch.getString("lat")), Double.parseDouble(eachBranch.getString("lng")));
                        MarkerOptions options = new MarkerOptions()
                                .title(eachBranch.getString("id"))
                                .position(latlng)
                                .snippet(eachBranch.getString("estab_id"));

                        if (mapperOpt.equals(CYM_Utility.OPT_BY_QRCODE)) {
                            if (eachBranch.getString("id").equals(branchID)) {
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                ll = new LatLng(Double.parseDouble(eachBranch.getString("lat")), Double.parseDouble(eachBranch.getString("lng")));
                            }
                        }

                        mMap.addMarker(options);
                    }
                    //for debugging
//                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 10);
//                    mMap.moveCamera(update);
                } catch(JSONException e) {
                    e.printStackTrace();
                } catch(Exception e) {
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

        public String getSearchString() {
            return searchString;
        }

        public void setSearchString(String searchString) {
            this.searchString = searchString;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MapActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
