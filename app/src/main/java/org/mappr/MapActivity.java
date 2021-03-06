package org.mappr;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mappr.org.mappr.model.CYM_Utility;
import org.mappr.org.mappr.model.JSONParser;
import org.mappr.org.mappr.model.MapprBranch;
import org.mappr.org.mappr.model.MapprCategory;
import org.mappr.org.mappr.model.MapprEstablishment;
import org.mappr.org.mappr.model.MapprJSONSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String PLOTTER_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/getMarkerOptions.php";
    private static final String RATING_LOADER_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/getAverageRating.php";
    public static boolean implementedQrSearch = false;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private Map<String, MapprEstablishment> hmEstablishment = new HashMap<>();
    private Polyline line;

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
        Log.i("poop", "map activity restarted");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuMapSatellite) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (id == R.id.menuMapTerrain) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else if (id == R.id.menuMapNormal) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private String lastMarkerId = "";
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i("poop", "marker is clicked id: " + marker.getId());
                //Load the average rating
                //Log.i("poop", marker.getTitle());
                //AverageRatingLoader task = new AverageRatingLoader(marker.getTitle());
                //task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                if (mLastLocation != null && !lastMarkerId.equals(marker.getTitle())) {
                    try {
                        DirectionRouter directionRouter = new DirectionRouter(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                                marker.getPosition().latitude, marker.getPosition().longitude, marker.getTitle());
                        directionRouter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }
        });
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                MapprEstablishment estab = hmEstablishment.get(marker.getSnippet());
                MapprEstablishment.preservedEstablishment = estab;
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
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        Log.i("poop", "connection suspended");
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
        private String qrMarkerId;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MapActivity.this);
            progressDialog.setMessage("Fetching branches, Please wait...");
            progressDialog.setIndeterminate(true);
            //progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
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


                JSONArray branches = json.getJSONArray("Branches");
                for (int i = 0; i < branches.length(); i++) {
                    JSONObject eachBranch = branches.getJSONObject(i);
                    if (!hmEstablishment.containsKey(eachBranch.getString("estab_id"))) {
                        JSONObject eachEstab = MapprEstablishment.getEstabJSONbyId(estabs, eachBranch.getString("estab_id"));
                        hmEstablishment.put(eachEstab.getString("id"), MapprEstablishment.instantiateJSONEstablishment(eachEstab));
                    }
                    publishProgress(eachBranch.toString());
                }

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
            progressDialog.dismiss();
            if (branchesString != null) {
                try {
                    Log.i("poop", branchesString + " onpsot");
                    JSONArray branches = new JSONArray(branchesString);
                    if (branches.length() < 1) {
                        Toast.makeText(MapActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONObject firstBranch = branches.getJSONObject(0);
                    Log.i("poop", "length: " + branches.length());
                    Log.i("poop", "branhesString: " + branchesString);
                    //kind of a hack
                    if (branchesString.equals("[]")) {
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
                                implementSearchHistory(eachBranch, branchID);
                                options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.blue_pin)));
                                ll = new LatLng(Double.parseDouble(eachBranch.getString("lat")), Double.parseDouble(eachBranch.getString("lng")));
                                qrMarkerId = eachBranch.getString("id");
                            }
                        }

                        //mMap.addMarker(options);
                    }
                    //for debugging
//                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 10);
//                    mMap.moveCamera(update);
                    if (mapperOpt.equals(CYM_Utility.OPT_BY_QRCODE)) {
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 10);
                        mMap.moveCamera(update);
                        if (mLastLocation != null) {
                            try {
                                DirectionRouter directionRouter = new DirectionRouter(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                                        ll.latitude, ll.longitude, qrMarkerId);
                                directionRouter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(MapActivity.this, "No results found", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            try {

                JSONObject eachBranch = new JSONObject(values[0]);
                LatLng latlng = new LatLng(Double.parseDouble(eachBranch.getString("lat")), Double.parseDouble(eachBranch.getString("lng")));
                MarkerOptions options = new MarkerOptions()
                        .title(eachBranch.getString("id"))
                        .position(latlng)
                        .snippet(eachBranch.getString("estab_id"));
//                        .icon(BitmapDescriptorFactory.fromBitmap(MapprCategory.getBitmapById(eachBranch.getString("category_id"))));

                MapprEstablishment estab = hmEstablishment.get(eachBranch.getString("estab_id"));
                options.icon(BitmapDescriptorFactory.fromBitmap(MapprCategory.getBitmapById(estab.getCategory_id())));

                if (mapperOpt.equals(CYM_Utility.OPT_BY_QRCODE)) {
                    if (eachBranch.getString("id").equals(branchID)) {
                        implementSearchHistory(eachBranch, branchID);
                        options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.blue_pin)));
                    }
                }

                mMap.addMarker(options);
            } catch (JSONException e) {
                e.printStackTrace();
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

    public void implementSearchHistory(JSONObject eachBranch, String branchID) {
        if (!implementedQrSearch) {
            MapprBranch mapprBranch = MapprBranch.instantiateBranch(eachBranch, (HashMap) hmEstablishment);
            MapprJSONSearch mapprJSONSearch = new MapprJSONSearch(CYM_Utility.OPT_BY_QRCODE, branchID);
            mapprJSONSearch.setMapprBranch(mapprBranch);
            mapprJSONSearch.setDisplayValue(mapprBranch.getMapprEstablishment().getName());
            mapprJSONSearch.saveSearchRequest(getApplicationContext());
            implementedQrSearch = true;
        }

    }

    class DirectionRouter extends AsyncTask<String, String, String> {

        private double sourceLat;
        private double sourceLng;
        private double destLat;
        private double destLng;
        private String markerId;
        ProgressDialog progressDialog;

        public DirectionRouter(double sourceLat, double sourceLng, double destLat, double destlng, String markerId) {
            this.sourceLat = sourceLat;
            this.sourceLng = sourceLng;
            this.destLat = destLat;
            this.destLng = destlng;
            this.markerId = markerId;
            progressDialog = new ProgressDialog(MapActivity.this);
            progressDialog.setMessage("Routing Destination...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            if (line != null) {
                line.remove();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String directionURL = DirectionActivity.makeURL(sourceLat, sourceLng, destLat, destLng);
                JSONObject json = JSONParser.getJSONfromURL(directionURL);
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                try {
                    final JSONObject json = new JSONObject(result);
                    JSONArray routeArray = json.getJSONArray("routes");
                    JSONObject routes = routeArray.getJSONObject(0);
                    JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
                    String encodedString = overviewPolylines.getString("points");
                    List<LatLng> list = DirectionActivity.decodePoly(encodedString);
                    line = mMap.addPolyline(new PolylineOptions()
                                    .addAll(list)
                                    .width(12)
                                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                    .geodesic(true)
                    );

                    lastMarkerId = markerId;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class AverageRatingLoader extends AsyncTask<String, String, String> {

        private String branchID;

        public AverageRatingLoader(String branchID) {
            this.branchID = branchID;
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("branch_id", branchID));
                JSONObject json = JSONParser.makeHttpRequest(RATING_LOADER_URL, "GET", params);
                //JSONObject json = JSONParser.getJSONfromURL(RATING_LOADER_URL + "?branch_id" + branchID);
                Log.i("poop", RATING_LOADER_URL);
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("poop", "json result: " + result);
            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    //CYM_Utility.setRatingBarRate(MapActivity.this, R.id.ratingInfoWindow, Float.parseFloat(json.getString("average_rating")));
                    //Log.i("poop", "average rating: " + json.getString("average_rating"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(MapActivity.this, MainActivity.class);
//        startActivity(intent);
        String mappr_opt = getIntent().getStringExtra(CYM_Utility.MAPPR_OPT);
        if (mappr_opt.equals(CYM_Utility.OPT_BY_QRCODE)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            setResult(RESULT_OK);
            Log.i("poop", "finishes activity from qrcode scanner");
        }

        finish();
    }
}
