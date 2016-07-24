package org.mappr;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.codeyasam.mappr.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mappr.org.mappr.model.JSONParser;
import org.mappr.org.mappr.model.MapprRoute;
import org.mappr.org.mappr.model.RouteAdapter;

import java.util.ArrayList;
import java.util.List;

public class DirectionActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private ListView listViewDirection;
    private Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        listViewDirection = (ListView) findViewById(R.id.listViewDirections);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        //new DirectionRouter("14.860120", "120.838729", "14.857732", "120.828720").execute();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public static String makeURL (double sourcelat, double sourcelng, double destlat, double destlng ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString( sourcelng));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString( destlng));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyDDpPDWu9z820FMYyOVsAphuy0ryz4kt2o");
        return urlString.toString();
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
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            LatLng ll = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 10);
            String destLat = getIntent().getStringExtra("destLat");
            String destLng = getIntent().getStringExtra("destLng");
            String sourceLat = String.valueOf(mLastLocation.getLatitude());
            String sourceLng = String.valueOf(mLastLocation.getLongitude());
            new DirectionRouter(sourceLat, sourceLng, destLat, destLng).execute();
            LatLng destLatlng = new LatLng(Double.parseDouble(destLat), Double.parseDouble(destLng));
            mMap.addMarker(new MarkerOptions().position(destLatlng).title("Your destination"));
            mMap.moveCamera(update);
        }

    }

    private List<MapprRoute> mapprRoutes;

    class DirectionRouter extends AsyncTask<String, String, String> {

        private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";
        private String sourceLat;
        private String sourceLng;
        private String destLat;
        private String destLng;
        private ProgressDialog progressDialog;

        public DirectionRouter(String sourceLat, String sourceLng, String destLat, String destLng) {
            this.sourceLat = sourceLat;
            this.sourceLng = sourceLng;
            this.destLat = destLat;
            this.destLng = destLng;
            mapprRoutes = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            progressDialog = new ProgressDialog(DirectionActivity.this);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("origin", sourceLat + "," + sourceLng));
                params.add(new BasicNameValuePair("destination", destLat + "," + destLng));
                params.add(new BasicNameValuePair("sensor", "false"));
                params.add(new BasicNameValuePair("mode", "driving"));
                params.add(new BasicNameValuePair("alternatives", "true"));
                params.add(new BasicNameValuePair("key", "AIzaSyDDpPDWu9z820FMYyOVsAphuy0ryz4kt2o"));
                JSONObject json = JSONParser.makeHttpRequest(DIRECTIONS_API_URL, "GET", params);
                setupRouteList(json);
                return json.toString();
            } catch (Exception e) {

            }
            return null;
        }

        private void setupRouteList(JSONObject json) {
            try {
                JSONArray routeArray = json.getJSONArray("routes");
                for (int i = 0; i < routeArray.length(); i++) {
                    JSONObject route = routeArray.getJSONObject(i);
                    mapprRoutes.add(MapprRoute.instantiateJSON(route));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.hide();
            if (result != null) {
                Log.i("poop", result);
                try {
                    //Tranform the string into a json object
                    final JSONObject json = new JSONObject(result);
                    JSONArray routeArray = json.getJSONArray("routes");
                    JSONObject routes = routeArray.getJSONObject(0);
                    JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
                    String encodedString = overviewPolylines.getString("points");
                    List<LatLng> list = decodePoly(encodedString);
                    line = mMap.addPolyline(new PolylineOptions()
                                    .addAll(list)
                                    .width(12)
                                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                    .geodesic(true)
                    );


           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */

                    ArrayAdapter<MapprRoute> adapter = new RouteAdapter(getApplicationContext(), mapprRoutes, mMap, line);
                    listViewDirection.setAdapter(adapter);

                } catch (JSONException e) {

                }
            }
        }

    }



    public static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
