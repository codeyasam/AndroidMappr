package org.mappr;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;


import com.example.codeyasam.mappr.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mappr.org.mappr.model.CYM_Utility;
import org.mappr.org.mappr.model.JSONParser;
import org.mappr.org.mappr.model.MapprBookmark;
import org.mappr.org.mappr.model.MapprEstablishment;
import org.mappr.org.mappr.model.MapprSession;
import org.mappr.org.mappr.model.MapprTour;
import org.mappr.org.mappr.model.ReviewAdapter;
import org.mappr.org.mappr.model.ReviewHolder;
import org.mappr.org.mappr.model.ScheduleAdapter;
import org.mappr.org.mappr.model.ScheduleHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EstablishmentDetails extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String DETAILS_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/getFullDetails.php";
    private MapprTour mapprTour = new MapprTour();

    private SharedPreferences settings;
    private MenuItem bookmarkMenu;
    private Button loginBtn;
    private RatingBar ratingBar;

    private GoogleApiClient mGoogleApiClient;
    private double sourceLat;
    private double sourceLng;

    private String branchLat;
    private String branchLng;

    //release references
    private ListView listview;
    private List<Bitmap> listBranchGallery = new ArrayList<>();
    private MapprEstablishment establishment;
    private List<ReviewHolder> reviewHolderList;
    private LinearLayout galleryContainer;
    private ExpandableListView scheduleView;
    private List<ScheduleHolder> scheduleHolderList;
    private List<ScheduleHolder> mHeader;

    private int expandClickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_establishment_details);
        listview = (ListView) findViewById(R.id.listView);
        CYM_Utility.setListViewHeightBasedOnChildren(listview);
        galleryContainer = (LinearLayout) findViewById(R.id.galleryContainer);
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final String branchId = getIntent().getStringExtra("branch_id");
        //loginBtn = (Button) findViewById(R.id.loginBtn);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating == 0) return;
                //if (!MapprSession.isLoggedIn) {  //for debugging
                if (settings.getString(MapprSession.LOGGED_USER_ID, "").isEmpty()) {
                    Log.i("poop", "is not logged in");
                    CYM_Utility.callYesNoMessage("You must be logged in", EstablishmentDetails.this, customOnClickListener());
                } else {
                    Log.i("poop", "value: " + rating + " user: " + fromUser + " branch_id: " + branchId);
                    Intent intent = new Intent(getApplicationContext(), ReviewActivty.class);
                    intent.putExtra("branch_id", branchId);
                    intent.putExtra("branch_rate", String.valueOf(rating));
                    intent.putExtra(CYM_Utility.MAPPR_FORM, getIntent().getStringExtra(CYM_Utility.MAPPR_FORM));
                    startActivity(intent);
                }
            }
        });

        scheduleView = (ExpandableListView) findViewById(R.id.expandableSchedule);
        //CYM_Utility.setListViewHeightBasedOnChildren(scheduleView);
        scheduleView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                int height = 0;
                for (int i = 0; i < scheduleView.getChildCount(); i++) {
                    height += scheduleView.getChildAt(i).getMeasuredHeight();
                    height += scheduleView.getDividerHeight();
                }
                expandClickCount++;
                if (expandClickCount == 1) {
                    int times = groupPosition == 0 ? 3 : 5;
                    int heightVal = (height + 6) * times;
                    scheduleView.getLayoutParams().height = heightVal;
                } else {
                    scheduleView.getLayoutParams().height = 700;
                }
            }
        });

        // Listview Group collapsed listener
        scheduleView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                expandClickCount--;
                if (expandClickCount == 0) {
                    scheduleView.getLayoutParams().height = 122;
                } else {
                    int heightVal = groupPosition == 0 ? 530 : 300;
                    scheduleView.getLayoutParams().height = heightVal;
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        ratingBar.setRating(0);
        DetailLauncher task = new DetailLauncher();
        final String branchId = getIntent().getStringExtra("branch_id");
        final String userId = settings.getString(MapprSession.LOGGED_USER_ID, "");
        task.setBranchId(branchId);
        task.setUserId(userId);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        buildGoogleApiClient();
//        if (!MapprSession.isLoggedIn) {  //for debugging
//            //if (settings.getString(MapprSession.LOGGED_USER_ID, "").isEmpty()) {
//            ratingBar.setVisibility(View.GONE);
//        } else {
//            ratingBar.setVisibility(View.VISIBLE);
//        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void loginClick(View v) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.putExtra("branch_id", getIntent().getStringExtra("branch_id"));
        intent.putExtra(CYM_Utility.MAPPR_FORM, CYM_Utility.FROM_DETAILS);
        startActivity(intent);
    }

    public void gotoGmaps(View v) {
        Intent intent = new Intent(getApplicationContext(), DirectionActivity.class);
        intent.putExtra("destLat", branchLat);
        intent.putExtra("destLng", branchLng);
        startActivity(intent);
    }
    private String branchContactNo = "none";
    public void callBtnClick(View v) {

        if (!branchContactNo.equals("none")) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + branchContactNo));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivity(callIntent);
        }
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
    public void onConnected(Bundle bundle) {
        Log.i("poop", "estabDetails mLocation");
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
            sourceLat = mLastLocation.getLatitude();
            sourceLng = mLastLocation.getLongitude();
            Log.i("poop", "lat: " + sourceLat);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    class DetailLauncher extends AsyncTask<String, String, String> {

        private String branchId;
        private String userId;
        List<ScheduleHolder> prompts = new ArrayList<>();

        public DetailLauncher() {
            establishment = MapprEstablishment.preservedEstablishment;
            Log.i("poop", "tinawag ung onResume");
            scheduleHolderList = new ArrayList<>();
            mHeader = new ArrayList<>();
            CYM_Utility.setImageOnView(EstablishmentDetails.this, R.id.estabLogo, establishment.getDisplay_picture());
            CYM_Utility.displayText(EstablishmentDetails.this, R.id.estabName, establishment.getName());
        }

        public void setBranchId(String branchId) {
            this.branchId = branchId;
        }

        public String getBranchId() {
            return branchId;
        }

        public void setUserId(String userId) { this.userId = userId; }
        public String getUserId() { return userId; }
        @Override
        protected void onPreExecute() {
            //testing purposes
            //this.branchId = "1";
        }


        private void setCurrentDayOpenHours(JSONObject json) {
            try {
                DateTime dateTime = new DateTime();
                JSONArray scheduleObjArr = json.getJSONArray("BranchHours");
                Log.i("poop", scheduleObjArr.toString());
                if (scheduleObjArr.length() < 1) return;
                for (int i = 0; i < scheduleObjArr.length(); i++) {
                    ScheduleHolder scheduleHolder = ScheduleHolder.instantiateJSONSchedule(scheduleObjArr.getJSONObject(i));
                    if (dateTime.getDayOfWeek() - 1 == Integer.parseInt(scheduleHolder.getDay_no())) {
                        mHeader.add(scheduleHolder);
                    } else {
                        scheduleHolderList.add(scheduleHolder);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
//                String user_id = settings.getString(MapprSession.LOGGED_USER_ID, "");
                if (!userId.isEmpty()) {
                    params.add(new BasicNameValuePair("user_id", userId));
                }

                params.add(new BasicNameValuePair("branch_id", branchId));
                JSONObject json = JSONParser.makeHttpRequest(DETAILS_URL, "GET", params);
                mHeader.add(new ScheduleHolder("ABOUT"));
                setCurrentDayOpenHours(json);
                JSONObject branch = json.getJSONObject("branch");
                branchLat = branch.getString("lat");
                branchLng = branch.getString("lng");
                branchContactNo = branch.getString("contact_number");

                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    Log.i("poop", result);
                    JSONObject json = new JSONObject(result);

                    String bookmarkState = json.getString("isBookmarked").equals("true") ? "BOOKMARKED" : "BOOKMARK";
                    bookmarkMenu.setTitle(bookmarkState);

                    JSONObject branch = json.getJSONObject("branch");
                    CYM_Utility.setRatingBarRate(EstablishmentDetails.this, R.id.branchRating, Float.parseFloat(json.getString("average_rating")));

                    HashMap<ScheduleHolder, List<ScheduleHolder>> listChildData = new HashMap<>();
                    String description = branch.getString("description");
                    byte[] bytes = description.getBytes("ISO-8859-1");
                    prompts.add(new ScheduleHolder("Address: " + branch.getString("address")));
                    prompts.add(new ScheduleHolder(new String(bytes, "UTF-8")));


                    if (!scheduleHolderList.isEmpty()) {
                        listChildData.put(mHeader.get(1), scheduleHolderList);
                    } else {
                        prompts.add(new ScheduleHolder("No open hours"));
                    }

                    if (!branchContactNo.equals("none")) {
                        Button callBtn = (Button) findViewById(R.id.callBtn);
                        callBtn.setVisibility(View.VISIBLE);
                    }

                    listChildData.put(mHeader.get(0), prompts);
                    ScheduleAdapter scheduleHolderArrayAdapter = new ScheduleAdapter(getApplicationContext(), mHeader, listChildData);
                    scheduleView.setAdapter(scheduleHolderArrayAdapter);
                    scheduleView.expandGroup(0);
                    scheduleView.getLayoutParams().height = 300;
                    String leastDistanceUrl = DirectionActivity.makeURL(sourceLat, sourceLng, Double.parseDouble(branchLat), Double.parseDouble(branchLng));
                    new LeastDistanceCalculator(leastDistanceUrl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new GalleryLoader(json).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new ReviewLoader(json).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    class ReviewLoader extends AsyncTask<String, String, String> {

        private JSONObject json;

        public ReviewLoader(JSONObject json) {
            reviewHolderList = new ArrayList<>();
            this.json = json;
        }

        private void setReviewHolder(JSONObject json) {
            try {
                if (json.getString("hasReview").equals("true")) {
                    JSONArray userObjArr = json.getJSONArray("Users");
                    JSONArray reviewObjArr = json.getJSONArray("Reviews");
                    Log.i("poop", "nagset ng review" + reviewObjArr.toString());
                    if (reviewObjArr.toString().equals("[{}]")) return;
                    for (int i = 0; i < reviewObjArr.length(); i++) {
                        reviewHolderList.add(ReviewHolder.instantiateJSONReview(userObjArr.getJSONObject(i), reviewObjArr.getJSONObject(i)));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                setReviewHolder(json);
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    if (reviewHolderList.isEmpty()) {
                        listview.setVisibility(View.GONE);
                    } else {
                        listview.setVisibility(View.VISIBLE);
                    }

                    ArrayAdapter<ReviewHolder> reviewHolderArrayAdapter = new ReviewAdapter(getApplicationContext(), reviewHolderList);
                    listview.setAdapter(reviewHolderArrayAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class LeastDistanceCalculator extends AsyncTask<String, String, String> {

        private String url;

        public LeastDistanceCalculator(String url) {
            this.url = url;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                JSONObject json = JSONParser.getJSONfromURL(url);
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    final JSONObject json = new JSONObject(result);
                    JSONArray routeArray = json.getJSONArray("routes");
                    JSONObject routes = routeArray.getJSONObject(0);
                    JSONArray legs = routes.getJSONArray("legs");
                    JSONObject steps = legs.getJSONObject(0);
                    JSONObject distance = steps.getJSONObject("distance");
                    Log.i("poop", "distance: " + distance.getString("text"));
                    CYM_Utility.displayText(EstablishmentDetails.this, R.id.distanceTxt, "Distance: " + distance.getString("text"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class GalleryLoader extends AsyncTask<String, String, String> {

        private JSONObject json;
        private ProgressBar progressBar;

        public GalleryLoader(JSONObject json) {
            this.json = json;
            listBranchGallery = new ArrayList<>();
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                JSONArray gallery = json.getJSONArray("Gallery");
                for (int i = 0; i < gallery.length(); i++) {
                    JSONObject eachGal = gallery.getJSONObject(i);
                    String url = CYM_Utility.MAPPR_PUBLIC_URL + eachGal.getString("gallery_pic");
                    listBranchGallery.add(CYM_Utility.loadImageFromServer(url, 75, 75));
                }
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            galleryContainer.removeAllViews();
            galleryContainer.invalidate();

            if (result != null) {
                try {
                    for (Bitmap bmp : listBranchGallery) {
                        ImageView iv = new ImageView(EstablishmentDetails.this);
                        iv.setImageBitmap(CYM_Utility.getResizedBitmap(bmp, 75, 75));
                        iv.setPadding(5, 5, 5, 5);
                        galleryContainer.addView(iv);
                        float height = CYM_Utility.dipToPixels(getApplicationContext(), 75);
                        float width = CYM_Utility.dipToPixels(getApplicationContext(), 75);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)height, (int)width);
                        iv.setLayoutParams(lp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_estab_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_bookmark:
                bookmarkBranch();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void bookmarkBranch() {
        Log.i("poop", "bookmard click");
        //if (!MapprSession.isLoggedIn) {  //for debugging
        if (settings.getString(MapprSession.LOGGED_USER_ID, "").isEmpty()) {
            CYM_Utility.callYesNoMessage("You must be logged in", EstablishmentDetails.this, customOnClickListener());
        } else {
            Log.i("poop", "bookmard click2");
            String user_id = settings.getString(MapprSession.LOGGED_USER_ID, "");
            String branch_id = getIntent().getStringExtra("branch_id");
            MapprBookmark bookmark = new MapprBookmark(user_id, branch_id, bookmarkMenu);
            bookmark.manageBookmark();
        }
    }

    private DialogInterface.OnClickListener customOnClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("poop", "gustong mag login");
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                String branch_id = getIntent().getStringExtra("branch_id");
                intent.putExtra("branch_id", branch_id);
                intent.putExtra(CYM_Utility.MAPPR_FORM, CYM_Utility.FROM_DETAILS);
                startActivity(intent);
            }
        };
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        bookmarkMenu = menu.findItem(R.id.action_bookmark);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onBackPressed() {
        finish();
    }

}
