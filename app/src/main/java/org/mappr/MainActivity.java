package org.mappr;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.codeyasam.mappr.JSONParser;
import com.example.codeyasam.mappr.MapprPlotter;
import com.example.codeyasam.mappr.R;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mappr.org.mappr.model.CYM_Utility;
import org.mappr.org.mappr.model.CategoryAdapter;
import org.mappr.org.mappr.model.MapprCategory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String CATEGORY_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/featuredCategoryTests.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new CategorySearcher().execute();
    }

    //search by string
    public void searchClick(View v) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_STRING);
        intent.putExtra("searchString", CYM_Utility.getText(this, R.id.searchTxt));
        startActivity(intent);
    }

    class CategorySearcher extends AsyncTask<String, String, List<MapprCategory>> {

        private CategoryAdapter categoryAdapter;
        private List<MapprCategory> categoryList;

        @Override
        protected List<MapprCategory> doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                JSONObject json = JSONParser.makeHttpRequest(CATEGORY_URL, "GET", params);
                JSONArray featuredCategories = json.getJSONArray("Categories");
                categoryList = getCategoryList(featuredCategories);
                categoryAdapter = new CategoryAdapter(MainActivity.this, categoryList);
                return categoryList;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<MapprCategory> categories) {
            if (!categories.isEmpty()) {
                try {
                    GridView categoryGrid = (GridView) findViewById(R.id.categoryGrid);
                    categoryGrid.setAdapter(categoryAdapter);
                    categoryGrid.setOnItemClickListener(customOnClickMethod());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private List<MapprCategory> getCategoryList(JSONArray jsonArray) {
            List<MapprCategory> categoryList = new ArrayList<>();
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    categoryList.add(MapprCategory.instantiateJSONCategory(jsonObject));
                }
                return categoryList;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        private AdapterView.OnItemClickListener customOnClickMethod() {
            return new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MapprCategory category = categoryList.get(position);
                    Log.i("poop", "category click id: " + category.getId());
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_CATEGORY);
                    intent.putExtra("categoryID", category.getId());
                    startActivity(intent);
                }
            };
        }
    }
}
