package org.mappr.org.mappr.model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mappr.MainActivity;
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
public class CategoryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String CATEGORY_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/featuredCategoryTests.php";
    private CategorySearcher categorySearcher;
    private View view;
    private GridView categoryGrid;
    private TextView progressBar;
    private SwipeRefreshLayout swipeLayout;
    private boolean enableRefresh = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_main_category, container, false);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshCategs);
        swipeLayout.setOnRefreshListener(this);
        categoryGrid = (GridView) view.findViewById(R.id.categoryGrid);
        categoryGrid.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem == 0 && !swipeLayout.isRefreshing() && enableRefresh){
                    swipeLayout.setEnabled(true);
                } else {
                    swipeLayout.setEnabled(false);
                }
            }
        });

        progressBar = (TextView) view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            boolean result = CYM_Utility.isOnline(getActivity().getApplicationContext());
            if (result) {
                Log.i("poop", MainActivity.categoryList.size() + ": ");
                if (MainActivity.categoryList.isEmpty()) {
                    categorySearcher = new CategorySearcher();
                    categorySearcher.execute();
                } else {
                    CategoryAdapter categoryAdapter = new CategoryAdapter(getActivity().getApplicationContext(), MainActivity.categoryList);
                    progressBar.setVisibility(View.GONE);
                    categoryGrid.setVisibility(View.VISIBLE);
                    categoryGrid.setAdapter(categoryAdapter);
                    categoryGrid.setOnItemClickListener(customOnClickMethod());
                }
            } else {
                //CYM_Utility.mAlertDialog("No Internet Connectivity", getActivity());
                progressBar.setText("No Internet Connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean listIsAtTop() {
        if(categoryGrid.getChildCount() == 0) return true;
        return categoryGrid.getChildAt(0).getTop() == 0;
    }

    public static void searchByCategory(FragmentActivity activity, MapprCategory mapprCategory) {
        implementSearchHistory(activity, mapprCategory);
        Intent intent = new Intent(activity.getApplicationContext(), MapActivity.class);
        intent.putExtra(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_CATEGORY);
        intent.putExtra("categoryID", mapprCategory.getId());
        activity.startActivity(intent);
    }

    //private static List<MapprCategory> categoryList = new ArrayList<>();

    @Override
    public void onRefresh() {
        categorySearcher = new CategorySearcher();
        categorySearcher.execute();
    }

    class CategorySearcher extends AsyncTask<String, String, List<MapprCategory>> {

        private CategoryAdapter categoryAdapter;
        //private ProgressBar progressBar;

        public CategorySearcher() {
            MainActivity.categoryList = new ArrayList<>();
            categoryAdapter = new CategoryAdapter(getActivity().getApplicationContext(), MainActivity.categoryList);
            categoryGrid.setAdapter(categoryAdapter);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            categoryGrid. setVisibility(View.GONE);
            //progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<MapprCategory> doInBackground(String... args) {
            try {
//                if (!categoryList.isEmpty()) {
//                    categoryAdapter = new CategoryAdapter(getActivity().getApplicationContext(), categoryList);
//                    return categoryList;
//                }
                List<NameValuePair> params = new ArrayList<>();
                JSONObject json = JSONParser.makeHttpRequest(CATEGORY_URL, "GET", params);
                JSONArray featuredCategories = json.getJSONArray("Categories");
                MainActivity.categoryList = getCategoryList(featuredCategories);
                categoryAdapter = new CategoryAdapter(getActivity().getApplicationContext(), MainActivity.categoryList);
                return MainActivity.categoryList;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(List<MapprCategory> categories) {
            if (swipeLayout.isRefreshing()) {
                swipeLayout.setRefreshing(false);
            }
            if (categories != null && !categories.isEmpty()) {
                try {
                    progressBar.setVisibility(View.GONE);
                    categoryGrid.setVisibility(View.VISIBLE);
                    categoryGrid.setAdapter(categoryAdapter);
                    categoryGrid.setOnItemClickListener(customOnClickMethod());
                    enableRefresh = true;
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


    }

    private AdapterView.OnItemClickListener customOnClickMethod() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MapprCategory category = MainActivity.categoryList.get(position);
                Log.i("poop", "category click id: " + category.getId());
//                    Intent intent = new Intent(getActivity().getApplicationContext(), MapActivity.class);
//                    intent.putExtra(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_CATEGORY);
//                    intent.putExtra("categoryID", category.getId());
//                    startActivity(intent);
                searchByCategory(getActivity(), category);
            }
        };
    }

    public static void implementSearchHistory(FragmentActivity activity, MapprCategory mapprCategory) {
        MapprJSONSearch mapprJSONSearch = new MapprJSONSearch(CYM_Utility.OPT_BY_CATEGORY, mapprCategory.getId());
        mapprJSONSearch.setDisplayValue(mapprCategory.getName());
        mapprJSONSearch.saveSearchRequest(activity.getApplicationContext());
    }

    @Override
    public void onDestroyView() {
        view = null;
        categoryGrid = null;
        super.onDestroyView();
    }
}
