package org.mappr.org.mappr.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.mappr.MapActivity;
import com.example.codeyasam.mappr.R;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by codeyasam on 7/18/16.
 */
public class CategoryFragment extends Fragment {

    private static final String CATEGORY_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/featuredCategoryTests.php";
    private CategorySearcher categorySearcher;
    private View view;
    private GridView categoryGrid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_main_category, container, false);
        categoryGrid = (GridView) view.findViewById(R.id.categoryGrid);
        categorySearcher = new CategorySearcher();
        categorySearcher.execute();
        return view;
    }


    class CategorySearcher extends AsyncTask<String, String, List<MapprCategory>> {

        private CategoryAdapter categoryAdapter;
        private List<MapprCategory> categoryList;

        public CategorySearcher() {
        }

        @Override
        protected List<MapprCategory> doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                JSONObject json = JSONParser.makeHttpRequest(CATEGORY_URL, "GET", params);
                JSONArray featuredCategories = json.getJSONArray("Categories");
                categoryList = getCategoryList(featuredCategories);
                categoryAdapter = new CategoryAdapter(getActivity().getApplicationContext(), categoryList);
                return categoryList;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<MapprCategory> categories) {
            if (categories != null && !categories.isEmpty()) {
                try {
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
                    Intent intent = new Intent(getActivity().getApplicationContext(), MapActivity.class);
                    intent.putExtra(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_CATEGORY);
                    intent.putExtra("categoryID", category.getId());
                    startActivity(intent);
                }
            };
        }

    }

    @Override
    public void onDestroyView() {
        view = null;
        categoryGrid = null;
        super.onDestroyView();
    }
}
