package com.example.codeyasam.mappr;

import android.graphics.Bitmap;

import org.json.JSONObject;

/**
 * Created by codeyasam on 6/23/16.
 */
public class ReviewHolder {

    private float rating;
    private String comment;
    private String submitDate;
    private MapprEndUser endUser;

    public static ReviewHolder instantiateJSONReview(JSONObject userObj, JSONObject reviewObj) {
        try {
//            JSONObject userObj = jsonReview.getJSONObject("Users");
//            JSONObject reviewObj = jsonReview.getJSONObject("Reviews");
            ReviewHolder rh = new ReviewHolder();
            rh.setEndUser(MapprEndUser.instantiateJSONUser(userObj));
            rh.setRating(Float.parseFloat(reviewObj.getString("rating")));
            rh.setComment(reviewObj.getString("comment"));
            rh.setSubmitDate(reviewObj.getString("submit_date"));

            return rh;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public MapprEndUser getEndUser() {
        return endUser;
    }

    public void setEndUser(MapprEndUser endUser) {
        this.endUser = endUser;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(String submitDate) {
        this.submitDate = submitDate;
    }

}
