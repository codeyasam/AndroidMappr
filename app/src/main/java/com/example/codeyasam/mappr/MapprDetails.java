package com.example.codeyasam.mappr;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MapprDetails extends AppCompatActivity {

    private static final String DETAILS_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/getFullDetails.php";
    private MapprTour mapprTour = new MapprTour();

    private SharedPreferences settings;
    private MenuItem bookmarkMenu;
    private Button loginBtn;
    private RatingBar ratingBar;

    private ListView listview;

    private String branchLat;
    private String branchLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappr_details);
        listview = (ListView)findViewById(R.id.listView);
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        final String branchId = getIntent().getStringExtra("branch_id");

        loginBtn = (Button) findViewById(R.id.loginBtn);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Log.i("poop", "value: " + rating + " user: " + fromUser + " branch_id: " + branchId);
                Intent intent = new Intent(MapprDetails.this, MapprReview.class);
                intent.putExtra("branch_id", branchId);
                intent.putExtra("branch_rate", String.valueOf(rating));
                intent.putExtra(CYM_Utility.MAPPR_FORM, getIntent().getStringExtra(CYM_Utility.MAPPR_FORM));
                startActivity(intent);
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();

        DetailLauncher task = new DetailLauncher();
        final String branchId = getIntent().getStringExtra("branch_id");
        final String userId = settings.getString(MapprSession.LOGGED_USER_ID, "");
        task.setBranchId(branchId);
        task.setUserId(userId);
        task.execute();

        if (!MapprSession.isLoggedIn) {  //for debugging
            //if (settings.getString(MapprSession.LOGGED_USER_ID, "").isEmpty()) {
            loginBtn.setVisibility(View.VISIBLE);
            ratingBar.setVisibility(View.GONE);
        } else {
            loginBtn.setVisibility(View.GONE);
            ratingBar.setVisibility(View.VISIBLE);
        }
    }

    public void loginClick(View v) {
        Intent intent = new Intent(MapprDetails.this, MapprLogin.class);
        intent.putExtra("branch_id", getIntent().getStringExtra("branch_id"));
        intent.putExtra(CYM_Utility.MAPPR_FORM, CYM_Utility.FROM_DETAILS);
        startActivity(intent);
    }

//    public void gotoGmaps(View v) {
//        StringBuilder uri = new StringBuilder("geo:");
//        uri.append("?q=");
//        uri.append(mapprTour.getLat());
//        uri.append(",");
//        uri.append(mapprTour.getLng());
//        uri.append("&z=10");
//        //uri.append("?z=10");
//        //uri.append("&q=" + URLEncoder.encode(mapprTour.getMarkerText()));
//
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));
//        startActivity(intent);
//    }

    public void gotoGmaps(View v) {
        Intent intent = new Intent(MapprDetails.this, MapprDirections.class);
        intent.putExtra("destLat", branchLat);
        intent.putExtra("destLng", branchLng);
        startActivity(intent);
    }

    class DetailLauncher extends AsyncTask<String, String, String> {

        private String branchId;
        private String userId;
        private List<Bitmap> listBranchGallery = new ArrayList<>();
        private MapprEstablishment establishment;
        private List<ReviewHolder> reviewHolderList;

        public DetailLauncher() {
            reviewHolderList = new ArrayList<>();
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

        private void setReviewHolder(JSONObject json) {
            try {
                if (json.getString("hasReview").equals("true")) {
                    JSONArray userObjArr = json.getJSONArray("Users");
                    JSONArray reviewObjArr = json.getJSONArray("Reviews");
                    Log.i("poop", reviewObjArr.toString());
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
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
//                String user_id = settings.getString(MapprSession.LOGGED_USER_ID, "");
                if (!userId.isEmpty()) {
                    params.add(new BasicNameValuePair("user_id", userId));
                }

                params.add(new BasicNameValuePair("branch_id", branchId));
                JSONObject json = JSONParser.makeHttpRequest(DETAILS_URL, "GET", params);
                JSONArray gallery = json.getJSONArray("Gallery");
                JSONObject estab = json.getJSONObject("estab");
                JSONObject branch = json.getJSONObject("branch");
                branchLat = branch.getString("lat");
                branchLng = branch.getString("lng");
                establishment = MapprEstablishment.instantiateJSONEstablishment(estab);
                Log.i("poop", "branch_id: " + branchId);
                Log.i("poop", "gallery length: " + gallery.length());

                for (int i = 0; i < gallery.length(); i++) {
                    JSONObject eachGal = gallery.getJSONObject(i);
                    String url = CYM_Utility.MAPPR_PUBLIC_URL + eachGal.getString("gallery_pic");
                    listBranchGallery.add(CYM_Utility.loadImageFromServer(url));
                }

                setReviewHolder(json);

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
                    JSONObject branch = json.getJSONObject("branch");
                    mapprTour.setLat(branch.getString("lat"));
                    mapprTour.setLng(branch.getString("lng"));
                    mapprTour.setMarkerText(establishment.getName());
                    CYM_Utility.setImageOnView(MapprDetails.this, R.id.estabLogo, establishment.getDisplay_picture());
                    CYM_Utility.displayText(MapprDetails.this, R.id.estabName, establishment.getName());
                    CYM_Utility.displayText(MapprDetails.this, R.id.branchAddress, branch.getString("address"));
                    CYM_Utility.setRatingBarRate(MapprDetails.this, R.id.branchRating, Float.parseFloat(json.getString("average_rating")));
                    LinearLayout galleryContainer = (LinearLayout) findViewById(R.id.galleryContainer);
                    for (Bitmap bmp : listBranchGallery) {
                        ImageView iv = new ImageView(MapprDetails.this);
                        iv.setImageBitmap(bmp);
                        galleryContainer.addView(iv);
                        float height = CYM_Utility.dipToPixels(MapprDetails.this, 100);
                        float width = CYM_Utility.dipToPixels(MapprDetails.this, 100);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)height, (int)width);
                        iv.setLayoutParams(lp);
                    }

                    String bookmarkState = json.getString("isBookmarked").equals("true") ? "BOOKMARKED" : "BOOKMARK";
                    bookmarkMenu.setTitle(bookmarkState);
                    ArrayAdapter<ReviewHolder> reviewHolderArrayAdapter = new ReviewAdapter(MapprDetails.this, reviewHolderList);
                    listview.setAdapter(reviewHolderArrayAdapter);
                    listview.setOnTouchListener(new View.OnTouchListener() {
                        // Setting on Touch Listener for handling the touch inside ScrollView
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            // Disallow the touch request for parent scroll on touch of child view
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            return false;
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            CYM_Utility.callYesNoMessage("You must be logged in", MapprDetails.this, customOnClickListener());
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
                Intent intent = new Intent(MapprDetails.this, MapprLogin.class);
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
        Class destinationAct = null;
        if (getIntent().getStringExtra(CYM_Utility.MAPPR_FORM).equals(CYM_Utility.FROM_FAVORITES)) {
            destinationAct = MapprFavorites.class;
        } else {
            destinationAct = MapprPlotter.class;
        }

        Intent intent = new Intent(MapprDetails.this, destinationAct);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
