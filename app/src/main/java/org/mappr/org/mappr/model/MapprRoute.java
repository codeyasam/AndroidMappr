package org.mappr.org.mappr.model;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by codeyasam on 7/24/16.
 */
public class MapprRoute {

    private String estimatedTimeArrival;
    private String viaSummary;
    private String distance;
    private String encodedString;

    public static MapprRoute instantiateJSON(JSONObject routes) {
        try {
            MapprRoute mapprRoute = new MapprRoute();
//            JSONArray routeArray = json.getJSONArray("routes");
//            JSONObject routes = routeArray.getJSONObject(0);
            JSONArray legs = routes.getJSONArray("legs");
            JSONObject steps = legs.getJSONObject(0);
            JSONObject distance = steps.getJSONObject("distance");
            JSONObject duration = steps.getJSONObject("duration");
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            mapprRoute.setDistance(distance.getString("text"));
            mapprRoute.setEstimatedTimeArrival(duration.getString("text"));
            mapprRoute.setViaSummary(routes.getString("summary"));
            mapprRoute.setEncodedString(overviewPolylines.getString("points"));
            return mapprRoute;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getEncodedString() {
        return encodedString;
    }

    public void setEncodedString(String encodedString) {
        this.encodedString = encodedString;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getEstimatedTimeArrival() {
        return estimatedTimeArrival;
    }

    public void setEstimatedTimeArrival(String estimatedTimeArrival) {
        this.estimatedTimeArrival = estimatedTimeArrival;
    }

    public String getViaSummary() {
        return viaSummary;
    }

    public void setViaSummary(String viaSummary) {
        this.viaSummary = viaSummary;
    }

}
