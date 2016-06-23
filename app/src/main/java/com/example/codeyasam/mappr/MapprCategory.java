package com.example.codeyasam.mappr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapprCategory extends AppCompatActivity {

    private static final String CATEGORY_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/featuredCategoryTests.php";
    private static final String QUERY_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/searchByQuery.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_featured_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new CategorySearcher().execute();
    }

    public void bookmarkClick(View v) {
        Intent intent = new Intent(MapprCategory.this, MapprFavorites.class);
        startActivity(intent);
    }


    public void searchClick(View v) {
        Intent intent = new Intent(MapprCategory.this, MapprPlotter.class);
        intent.putExtra(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_STRING);
        intent.putExtra("searchString", CYM_Utility.getText(MapprCategory.this, R.id.searchTxt));
        startActivity(intent);
    }

    public void scanQrCode(View v) {
        Log.i("poop", "poop");
        Intent intent = new Intent(MapprCategory.this, MapprQrCode.class);
        startActivity(intent);
    }

    class CategorySearcher extends AsyncTask<String, String, String> {

        private Map<String, Bitmap> hmCategIcons = new HashMap<>();

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... args) {

            try {
                List<NameValuePair> params = new ArrayList<>();
                JSONObject json = JSONParser.makeHttpRequest(CATEGORY_URL, "GET", params);
                JSONArray featuredCategories = json.getJSONArray("Categories");
                for (int i = 0; i < featuredCategories.length(); i++) {
                    JSONObject eachCategory = featuredCategories.getJSONObject(i);
                    String categId = eachCategory.getString("id");
                    String categDp = eachCategory.getString("display_picture");
                    Bitmap bm = CYM_Utility.loadImageFromServer(CYM_Utility.MAPPR_PUBLIC_URL + categDp);
                    hmCategIcons.put(categId, bm);
                }
                return featuredCategories.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String categories) {
            if (categories != null) {
                try {
                    JSONArray categoryArray = new JSONArray(categories);
                    LinearLayout categoryContainer = (LinearLayout)findViewById(R.id.categoryContainer);
                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

                    for (int i = 0; i < categoryArray.length();) {
                        LinearLayout innerRow = new LinearLayout(MapprCategory.this);
                        innerRow.setOrientation(LinearLayout.HORIZONTAL);
                        innerRow.setGravity(Gravity.CENTER_HORIZONTAL);

                        int limit = i + 3 > categoryArray.length() ? categoryArray.length() - 3 : 3;
                        for (int j = i; j < i + limit; j++) {

                            JSONObject eachCategory = categoryArray.getJSONObject(j);
                            TextView tvCategoryName = new TextView(MapprCategory.this);
                            ImageView iconContainer = new ImageView(MapprCategory.this);
                            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                                (int)CYM_Utility.dipToPixels(MapprCategory.this, 80),
                                (int)CYM_Utility.dipToPixels(MapprCategory.this, 80),1
                            );

                            //Bitmap bmp = CYM_Utility.getRoundedCornerBitmap(hmCategIcons.get(eachCategory.get("id")));
                            iconContainer.setImageBitmap(hmCategIcons.get(eachCategory.getString("id")));
                            tvCategoryName.setText(eachCategory.getString("name"));
                            tvCategoryName.setLayoutParams(lParams);
                            iconContainer.setLayoutParams(imgParams);
                            //tvCategoryName.setBackgroundColor(Color.GREEN);

                            LinearLayout wrapper = new LinearLayout(MapprCategory.this);

                            lParams.setMargins(10, 5, 10, 5);
                            wrapper.setLayoutParams(lParams);
                            wrapper.setOrientation(LinearLayout.VERTICAL);
                            wrapper.setGravity(Gravity.CENTER_HORIZONTAL);
                            wrapper.setBackgroundColor(Color.CYAN);
                            final String categoryId = eachCategory.getString("id");
                            wrapper.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.i("poop", "category click id: " + categoryId);
                                    Intent intent = new Intent(MapprCategory.this, MapprPlotter.class);
                                    intent.putExtra(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_CATEGORY);
                                    intent.putExtra("categoryID", categoryId);
                                    startActivity(intent);
                                }
                            });

                            wrapper.addView(iconContainer);
                            wrapper.addView(tvCategoryName);
                            innerRow.addView(wrapper);

                            Log.i("poop", eachCategory.toString());
                        }
                        categoryContainer.addView(innerRow);
                        i+= limit;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    class QuerySearcher extends AsyncTask<String, String, String> {

        private String searchString;

        public QuerySearcher(String searchString) {
            this.searchString = searchString;
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("searchString", searchString));
                JSONObject json = JSONParser.makeHttpRequest(QUERY_URL, "GET", params);
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

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
