package com.example.codeyasam.mappr;

import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    private GoogleMap mMap;
    //private GoogleApiClient mGoogleApiClient;

   // private static final float DEFAULTZOOM = 15;
    private static String MY_URL = "http://192.168.42.235/thesis/testinglang.php";
    private static final String TAG_SUCCESS = "Branches";
    private static final String TAG_MESSAGE = "message";

    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        new Testing().execute();
//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//        }

    }

//    @Override
//    protected void onStart() {
//        mGoogleApiClient.connect();
//        super.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        mGoogleApiClient.disconnect();
//        super.onStop();
//    }

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
        //new Testing().execute();
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.setMyLocationEnabled(true);
    }


//
//    @Override
//    public void onConnected(Bundle bundle) {
//        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (currentLocation != null) {
//            LatLng latlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, DEFAULTZOOM);
//            mMap.moveCamera(update);
//        } else {
//            Toast.makeText(this, "Current location is not available", Toast.LENGTH_LONG).show();
//        }
//    }
//
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//
//    }

    class Testing extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            Log.i("POOP", "here it is 1");
        }

        @Override
        protected String doInBackground(String... args) {
            JSONArray success;


            try {
                Log.i("POOP", "tried it 1st");
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("estab_id", "3"));
                Log.i("POOP", "tried it 111st");
                JSONObject json = JSONParser.makeHttpRequest(MY_URL, "GET", params);
                Log.i("POOP", "tried it 1111");
                success = json.getJSONArray(TAG_SUCCESS);
                return success.toString();
//                for (int i = 0; i < success.length(); i++) {
//                    JSONObject eachBranch = success.getJSONObject(i);
//                    LatLng latlng = new LatLng(Double.parseDouble(eachBranch.getString("lat")), Double.parseDouble(eachBranch.getString("lng")));
//                    mMap.addMarker(new MarkerOptions().position(latlng).title(eachBranch.getString("address")));
//                }
                //JSONObject success0 = success.getJSONObject(0);
//                Log.i("POOP", "tried it 2nd");
//                if (success != null) {
//                    Log.i("POOP", "length: ");
//                    //Log.i("POOP", success0.getString("id"));
//                    Log.i("POOP", success.toString());
//                    return "here it is working";
//                } else {
//                    Log.i("POOP", "failed?");
//                    return "here it is failed";
//                }

            } catch (JSONException e) {
                Log.i("POOP", "error na dito");
                e.printStackTrace();
            } catch (Exception e) {
                Log.i("POOP", "dito na ung error");
                e.printStackTrace();
            }

            Log.i("POOP", "here it is 2");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("POOP", "exiting");
            if (s != null) {
                Toast.makeText(MapsActivity.this, "Message: " + s, Toast.LENGTH_LONG).show();
                try {
                    JSONArray success = new JSONArray(s);
                    for (int i = 0; i < success.length(); i++) {
                        JSONObject eachBranch = success.getJSONObject(i);
                        LatLng latlng = new LatLng(Double.parseDouble(eachBranch.getString("lat")), Double.parseDouble(eachBranch.getString("lng")));
                        mMap.addMarker(new MarkerOptions().position(latlng).title(eachBranch.getString("address")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
