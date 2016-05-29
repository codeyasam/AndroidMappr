package com.example.codeyasam.mappr;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FeaturedCategory extends AppCompatActivity {

    private static final String CATEGORY_URL = "http://192.168.42.84/thesis/tests/featuredCategoryTests.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_featured_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        new CategorySearcher().execute();
    }

    class CategorySearcher extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... args) {

            try {
                List<NameValuePair> params = new ArrayList<>();
                JSONObject json = JSONParser.makeHttpRequest(CATEGORY_URL, "GET", params);
                JSONArray featuredCategories = json.getJSONArray("Categories");
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
                    for (int i = 0; i < categoryArray.length(); i++) {
                        JSONObject eachCategory = categoryArray.getJSONObject(i);
                        TextView tvSample = new TextView(FeaturedCategory.this);
                        tvSample.setText(eachCategory.getString("name"));
                        categoryContainer.addView(tvSample);
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
