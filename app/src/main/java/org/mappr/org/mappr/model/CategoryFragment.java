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
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.message.BasicNameValuePair;
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
    private static final String PARENT_CATEGORY_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/getParentCategories.php";
    private static final String CHILD_CATEGORY_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/getChildCategories.php";

    private CategorySearcher categorySearcher;
    private View view;
    private GridView categoryGrid;
    private TextView progressBar;
    private SwipeRefreshLayout swipeLayout;
    private boolean enableRefresh = false;

    private ExpandableListView expandableParentCategories;
    private ParentCategoryAdapter expandableAdapter;

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
        setupExpandableParentCategories();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // previous version of categories
//        try {
//            boolean result = CYM_Utility.isOnline(getActivity().getApplicationContext());
//            if (result) {
//                Log.i("poop", MainActivity.categoryList.size() + ": ");
//                if (MainActivity.categoryList.isEmpty()) {
//                    categorySearcher = new CategorySearcher();
//                    categorySearcher.execute();
//                } else {
//                    CategoryAdapter categoryAdapter = new CategoryAdapter(getActivity().getApplicationContext(), MainActivity.categoryList);
//                    progressBar.setVisibility(View.GONE);
//                    categoryGrid.setVisibility(View.VISIBLE);
//                    categoryGrid.setAdapter(categoryAdapter);
//                    categoryGrid.setOnItemClickListener(customOnClickMethod());
//                }
//            } else {
//                progressBar.setText("No Internet Connection");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            boolean result = CYM_Utility.isOnline(getActivity().getApplicationContext());
            if (result) {
                if (MainActivity.parentCategoryList.isEmpty()) {
                    new ParentCategoryLoader().execute();
                    new ChildCategoryLoader().execute();
                }
            } else {
                progressBar.setText("No Internet Connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupExpandableParentCategories() {
        expandableParentCategories = (ExpandableListView) view.findViewById(R.id.expandableParentCateg);
        ViewTreeObserver vto = expandableParentCategories.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int myLeft = expandableParentCategories.getWidth() - (int) CYM_Utility.dipToPixels(getContext(), 40);
                int myRight = expandableParentCategories.getWidth();
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    expandableParentCategories.setIndicatorBounds(myLeft, myRight);
                } else {
                    expandableParentCategories.setIndicatorBoundsRelative(myLeft, myRight);
                }
            }
        });

        expandableParentCategories.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

            }
        });

        expandableAdapter = new ParentCategoryAdapter(getActivity().getApplicationContext(),
                MainActivity.parentCategoryList, MainActivity.hmMapprCategory, getActivity());
        expandableParentCategories.setAdapter(expandableAdapter);
    }

    private boolean listIsAtTop() {
        if(categoryGrid.getChildCount() == 0) return true;
        return categoryGrid.getChildAt(0).getTop() == 0;
    }

    public static void searchByCategory(FragmentActivity activity, MapprCategory mapprCategory) {
        //implementSearchHistory(activity, mapprCategory);
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

        public CategorySearcher() {
            MainActivity.categoryList = new ArrayList<>();
            categoryAdapter = new CategoryAdapter(getActivity().getApplicationContext(), MainActivity.categoryList);
            categoryGrid.setAdapter(categoryAdapter);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            categoryGrid. setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<MapprCategory> doInBackground(String... args) {
            try {
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

    private class ParentCategoryLoader extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                JSONObject json = JSONParser.getJSONfromURL(PARENT_CATEGORY_URL);
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
                    JSONObject json = new JSONObject(result);
                    JSONArray jsonArray = json.getJSONArray("ParentCategories");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        new EachParentCategoryLoader(jsonArray.getJSONObject(i)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class EachParentCategoryLoader extends AsyncTask<String, String, MapprCategory> {

        private MapprCategory eachParentCategory;
        private JSONObject categoryJSON;
        private boolean isGreater;

        public EachParentCategoryLoader(JSONObject categoryJSON) {
            this.categoryJSON = categoryJSON;
            isGreater = true;
        }

        @Override
        protected MapprCategory doInBackground(String... args) {
            try {
                return MapprCategory.instantiateJSONCategory(categoryJSON);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MapprCategory eachParentCategory) {
            super.onPostExecute(eachParentCategory);
            if (eachParentCategory != null) {
                try {
                    Log.i("poop", "each category on post");
                    progressBar.setVisibility(View.GONE);

                    for (int i = 0; i < MainActivity.parentCategoryList.size(); i++) {
                        MapprCategory mapprCategory = MainActivity.parentCategoryList.get(i);
                        if (Integer.parseInt(eachParentCategory.getCateg_order()) <= Integer.parseInt(mapprCategory.getCateg_order())) {
                            MainActivity.parentCategoryList.add(i, eachParentCategory);
                            isGreater = false;
                            break;
                        }
                    }

                    if (MainActivity.parentCategoryList.isEmpty() || isGreater) MainActivity.parentCategoryList.add(eachParentCategory);
                    expandableAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ChildCategoryLoader extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                JSONObject json = JSONParser.getJSONfromURL(CHILD_CATEGORY_URL);
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
                    JSONObject json = new JSONObject(result);
                    JSONArray jsonArray = json.getJSONArray("ChildFeaturedCategories");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        new EachChildCategoryLoader(jsonArray.getJSONObject(i)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class EachChildCategoryLoader extends AsyncTask<String, String, MapprCategory> {

        private JSONObject categoryJSON;

        public EachChildCategoryLoader(JSONObject categoryJSON) {
            Log.i("poop", "Each child on constructor");
            this.categoryJSON = categoryJSON;
        }

        @Override
        protected MapprCategory doInBackground(String... args) {
            try {
                return MapprCategory.instantiateJSONCategory(categoryJSON);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MapprCategory mapprCategory) {
            super.onPostExecute(mapprCategory);
            if (mapprCategory != null) {
                try {
                    Log.i("poop", "added child to " + mapprCategory.getParent_category_id());
                    if (MainActivity.hmMapprCategory.containsKey(mapprCategory.getParent_category_id())) {
                        MainActivity.hmMapprCategory.get(mapprCategory.getParent_category_id()).add(mapprCategory);
                        int mSize = MainActivity.hmMapprCategory.get(mapprCategory.getParent_category_id()).size();
                        Log.i("poop", "a parent size: " + mSize);
                    } else {
                        MainActivity.hmMapprCategory.put(mapprCategory.getParent_category_id(), new ArrayList<MapprCategory>());
                        MainActivity.hmMapprCategory.get(mapprCategory.getParent_category_id()).add(mapprCategory);
                        int mSize = MainActivity.hmMapprCategory.get(mapprCategory.getParent_category_id()).size();
                        Log.i("poop", "a parent size: " + mSize);
                    }

                    MainActivity.categoryList.add(mapprCategory);
                    expandableAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private AdapterView.OnItemClickListener customOnClickMethod() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MapprCategory category = MainActivity.categoryList.get(position);
                Log.i("poop", "category click id: " + category.getId());
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
