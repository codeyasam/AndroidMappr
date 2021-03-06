package org.mappr.org.mappr.model;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONObject;
import org.mappr.MainActivity;

import java.util.ArrayList;
import java.util.List;

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
        //return name;
        return CYM_Utility.getDecodeString(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeatured_category() {
        return featured_category;
        //return CYM_Utility.getDecodeString(featured_category);
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
        //return description;
        return CYM_Utility.getDecodeString(description);
    }

    public String getParent_category_id() {
        return parent_category_id;
    }

    public void setParent_category_id(String parent_category_id) {
        this.parent_category_id = parent_category_id;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getCateg_order() {
        return categ_order;
    }

    public void setCateg_order(String categ_order) {
        this.categ_order = categ_order;
    }

    public static MapprCategory instantiateJSONCategory(JSONObject jsonCategory) {
        try {
            MapprCategory category = new MapprCategory();
            category.setId(jsonCategory.getString("id"));
            category.setName(jsonCategory.getString("name"));
            category.setDescription(jsonCategory.getString("description"));
            category.setDisplay_picture(CYM_Utility.loadImageFromServer(CYM_Utility.MAPPR_PUBLIC_URL + jsonCategory.getString("display_picture"), 50, 50));
            category.setFeatured_category(jsonCategory.getString("featured_category"));
            category.setParent_category_id(jsonCategory.getString("parent_category_id"));
            category.setCateg_order(jsonCategory.getString("categ_order"));
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
    private String parent_category_id;
    private String categ_order;

    public static Bitmap getBitmapById(String categId) {
        try {
            Log.i("pooping", "poop");
            for (MapprCategory eachCategory : MainActivity.categoryList) {
                Log.i("pooping", "eachCateg" + eachCategory.getName() + " id: " + eachCategory.getId());
                if (eachCategory.getId().equals(categId)) {
                    return eachCategory.getDisplay_picture();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
