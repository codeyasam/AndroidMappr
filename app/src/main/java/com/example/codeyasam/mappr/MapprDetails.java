package com.example.codeyasam.mappr;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappr_details);
        DetailLauncher task = new DetailLauncher();
        final String branchId = getIntent().getStringExtra("branch_id");
        task.setBranchId(branchId);
        task.execute();

        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Log.i("poop", "value: " + rating + " user: " + fromUser);
                Intent intent = new Intent(MapprDetails.this, MapprReview.class);
                intent.putExtra("branch_id", branchId);
                intent.putExtra("branch_rate", String.valueOf(rating));
                startActivity(intent);
            }
        });


        if (!MapprSession.isLoggedIn) {
            loginBtn.setVisibility(View.VISIBLE);
        } else {
            ratingBar.setVisibility(View.VISIBLE);
        }
    }

    public void loginClick(View v) {
        Intent intent = new Intent(MapprDetails.this, MapprLogin.class);
        intent.putExtra("branch_id", getIntent().getStringExtra("branch_id"));
        startActivity(intent);
    }

    public void gotoGmaps(View v) {
        StringBuilder uri = new StringBuilder("geo:");
        uri.append("?q=");
        uri.append(mapprTour.getLat());
        uri.append(",");
        uri.append(mapprTour.getLng());
        uri.append("&z=10");
        //uri.append("?z=10");
        //uri.append("&q=" + URLEncoder.encode(mapprTour.getMarkerText()));

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));
        startActivity(intent);
    }

    class DetailLauncher extends AsyncTask<String, String, String> {

        private String branchId;
        private List<Bitmap> listBranchGallery = new ArrayList<>();
        private MapprEstablishment establishment;

        public void setBranchId(String branchId) {
            this.branchId = branchId;
        }

        public String getBranchId() {
            return branchId;
        }

        @Override
        protected void onPreExecute() {
            //testing purposes
            this.branchId = "1";
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("branch_id", branchId));
                JSONObject json = JSONParser.makeHttpRequest(DETAILS_URL, "GET", params);
                JSONArray gallery = json.getJSONArray("Gallery");
                JSONObject estab = json.getJSONObject("estab");
                establishment = MapprEstablishment.instantiateJSONEstablishment(estab);
                Log.i("poop", "branch_id: " + branchId);
                Log.i("poop", "gallery length: " + gallery.length());
                for (int i = 0; i < gallery.length(); i++) {
                    JSONObject eachGal = gallery.getJSONObject(i);
                    String url = CYM_Utility.MAPPR_PUBLIC_URL + eachGal.getString("gallery_pic");
                    listBranchGallery.add(CYM_Utility.loadImageFromServer(url));
                }

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
                    JSONObject json = new JSONObject(result);
                    JSONObject branch = json.getJSONObject("branch");
                    mapprTour.setLat(branch.getString("lat"));
                    mapprTour.setLng(branch.getString("lng"));
                    mapprTour.setMarkerText(establishment.getName());
                    CYM_Utility.setImageOnView(MapprDetails.this, R.id.estabLogo, establishment.getDisplay_picture());
                    CYM_Utility.displayText(MapprDetails.this, R.id.estabName, establishment.getName());
                    CYM_Utility.displayText(MapprDetails.this, R.id.branchAddress, branch.getString("address"));
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
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
