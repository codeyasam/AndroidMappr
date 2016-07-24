package org.mappr.org.mappr.model;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by codeyasam on 7/17/16.
 */
public class MapprCategory {



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeatured_category() {
        return featured_category;
    }

    public void setFeatured_category(String featured_category) {
        this.featured_category = featured_category;
    }

    public Bitmap getDisplay_picture() {
        return display_picture;
    }

    public void setDisplay_picture(Bitmap display_picture) {
        this.display_picture = display_picture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static MapprCategory instantiateJSONCategory(JSONObject jsonCategory) {
        try {
            MapprCategory category = new MapprCategory();
            category.setId(jsonCategory.getString("id"));
            category.setName(jsonCategory.getString("name"));
            category.setDescription(jsonCategory.getString("description"));
            category.setDisplay_picture(CYM_Utility.loadImageFromServer(CYM_Utility.MAPPR_PUBLIC_URL + jsonCategory.getString("display_picture"), 50, 50));
            category.setFeatured_category(jsonCategory.getString("featured_category"));
            return category;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String id;
    private String name;
    private String featured_category;
    private Bitmap display_picture;
    private String description;
}