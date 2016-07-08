package com.example.codeyasam.mappr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MapprReview extends Activity {

    private SharedPreferences settings;
    private static final String GET_CURRENT_USER_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/getCurrentUser.php";

    private boolean hasReview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappr_review);

        float branchRate = Float.parseFloat(getIntent().getStringExtra("branch_rate"));
        RatingBar branchRating = (RatingBar) findViewById(R.id.mapprRating);
        branchRating.setRating(branchRate);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        String user_id = settings.getString(MapprSession.LOGGED_USER_ID, "");
        CurrentUserLoader task = new CurrentUserLoader(user_id);
        task.execute();
    }

    public void btnClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cancelBtn:
                finish();
                break;
            case R.id.publishBtn:
                publishReview();
                break;
        }
    }

    //Add some error handling
    public void publishReview() {
        RatingBar branchRating = (RatingBar) findViewById(R.id.mapprRating);
        String branchId = getIntent().getStringExtra("branch_id");
        String branchRate = String.valueOf(branchRating.getRating());
        String userId = settings.getString(MapprSession.LOGGED_USER_ID, "");
        String comment = CYM_Utility.getText(MapprReview.this, R.id.reviewTxt);
        String mappr_from = getIntent().getStringExtra(CYM_Utility.MAPPR_FORM);
        Log.i("poop", "branchID: " + branchId);
        new ReviewPublisher(branchId, branchRate, userId, comment, mappr_from).execute();
    }

    class ReviewPublisher extends AsyncTask<String, String, String> {

        private static final String POST_REVIEW_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/makeReview.php";

        private String branchId;
        private String userId;
        private String branchRate;
        private String comment;
        private String mappr_from;

        public ReviewPublisher(String branchId, String branchRate, String userId, String comment, String mappr_from) {
            this.branchId = branchId;
            this.userId = userId;
            this.branchRate = branchRate;
            this.comment = comment;
            this.mappr_from = mappr_from;
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("branch_id", branchId));
                params.add(new BasicNameValuePair("user_id", userId));
                params.add(new BasicNameValuePair("rating", branchRate));
                params.add(new BasicNameValuePair("comment", comment));
                params.add(new BasicNameValuePair("submit", "true"));
                if (hasReview) {
                    params.add(new BasicNameValuePair("hasReview", "true"));
                } else  {
                    params.add(new BasicNameValuePair("hasReview", "false"));
                }

                JSONObject json = JSONParser.makeHttpRequest(POST_REVIEW_URL, "POST", params);
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("poop", result);
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getString("success").equals("true")) {
                        Intent intent = new Intent(MapprReview.this, MapprDetails.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        //intent.putExtra("branch_id", branchId);
                        //intent.putExtra(CYM_Utility.MAPPR_FORM, mappr_from);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CurrentUserLoader extends AsyncTask<String, String, String> {

        private String user_id;
        private Bitmap displayPicture;

        public CurrentUserLoader(String user_id) {
            this.user_id = user_id;
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("user_id", user_id));
                JSONObject json = JSONParser.makeHttpRequest(GET_CURRENT_USER_URL, "GET", params);
                displayPicture = CYM_Utility.loadImageFromServer(json.getString("display_picture"));
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected  void onPostExecute(String result) {
            if (result != null) {
                try {
                    Log.i("poop", result);
                    JSONObject userJson = new JSONObject(result);
                    String fullName = userJson.getString("first_name") + " " + userJson.getString("last_name");
                    String prompt = "Publicly posting in web as \n" + fullName;
                    TextView tv = (TextView)findViewById(R.id.promptMessage);
                    tv.setSingleLine(false);
                    tv.setText(prompt);
                    Bitmap bmp = CYM_Utility.getRoundedCornerBitmap(displayPicture);
                    CYM_Utility.setImageOnView(MapprReview.this, R.id.displayPicture, bmp);
                    if (userJson.getString("hasReview").equals("true")) {
                        float rating = Float.parseFloat(userJson.getString("rating"));
                        RatingBar branchRating = (RatingBar) findViewById(R.id.mapprRating);
                        branchRating.setRating(rating);
                        CYM_Utility.setText(MapprReview.this, R.id.reviewTxt, userJson.getString("comment"));
                        hasReview = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
