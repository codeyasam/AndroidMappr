package org.mappr.org.mappr.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by codeyasam on 7/25/16.
 */
public class MapprJSONSearch {

    public static String SEARCH_REQUEST = "searchRequest";
    public boolean hasBranch;

    private String mappr_opt;
    private String search_value;
    private String displayValue;
    private MapprBranch mapprBranch = new MapprBranch();

    private JSONObject jsonObject = new JSONObject();


    public MapprJSONSearch(String mappr_opt, String search_value) {
        this.mappr_opt = mappr_opt;
        this.search_value = search_value;
        hasBranch = false;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public MapprBranch getMapprBranch() {
        return mapprBranch;
    }

    public void setMapprBranch(MapprBranch mapprBranch) {
        this.mapprBranch = mapprBranch;
        this.hasBranch = true;
        saveBranchDetails();
    }

    public String getSearch_value() {
        return search_value;
    }

    public void setSearch_value(String search_value) {
        this.search_value = search_value;
    }

    public String getMappr_opt() {
        return mappr_opt;
    }

    public void setMappr_opt(String mappr_opt) {
        this.mappr_opt = mappr_opt;
    }

    public void saveSearchRequest(Context context) {
        Log.i("poop", "search request executed");
        try {
            JSONArray jsonArray = getAllSearchRequest(context);
            jsonObject.put("mappr_opt", mappr_opt);
            jsonObject.put("search_value", search_value);
            jsonObject.put("display_value", displayValue);
            jsonArray.put(jsonObject);
            Log.i("poop", "dumaan dito 2 : " + jsonArray.toString());

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(SEARCH_REQUEST, jsonArray.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("poop", "dumaan dito 3");
        }
    }

    private void saveBranchDetails() {
        try {
            Log.i("poop", mapprBranch.getMapprEstablishment().getName() + " here here");
            jsonObject.put("branch_id", String.valueOf(mapprBranch.getId()));
            jsonObject.put("estab_name", mapprBranch.getMapprEstablishment().getName());
            jsonObject.put("branch_address", mapprBranch.getAddress());
            jsonObject.put("category_id", mapprBranch.getMapprEstablishment().getCategory_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONArray getAllSearchRequest(Context context) throws JSONException {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String searchRequest = settings.getString(SEARCH_REQUEST, "");
        return searchRequest.isEmpty() ? new JSONArray() : new JSONArray(searchRequest);
    }

    public static List<MapprJSONSearch> getSearchesList(Context context) {
        try {
            List<MapprJSONSearch> mapprJSONSearchList = new ArrayList<>();
            JSONArray jsonArray = getAllSearchRequest(context);
            Log.i("poop", jsonArray.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                MapprJSONSearch mapprJSONSearch = new MapprJSONSearch(jsonObj.getString("mappr_opt"), jsonObj.getString("search_value"));
                if (jsonObj.has("branch_id")) {
                    MapprBranch mapprBranch = new MapprBranch();
                    mapprBranch.setId(jsonObj.getString("branch_id"));
                    mapprBranch.setAddress(jsonObj.getString("branch_address"));
                    mapprBranch.setMapprEstablishment(new MapprEstablishment());
                    Log.i("poop", "here at branch_id " + jsonObj.getString("estab_name"));
                    mapprBranch.getMapprEstablishment().setName(jsonObj.getString("estab_name"));
                    mapprBranch.getMapprEstablishment().setCategory_id(jsonObj.getString("category_id"));
                    mapprJSONSearch.setMapprBranch(mapprBranch);
                }
                mapprJSONSearch.setDisplayValue(jsonObj.getString("display_value"));
                mapprJSONSearchList.add(mapprJSONSearch);
            }
            return mapprJSONSearchList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


}
