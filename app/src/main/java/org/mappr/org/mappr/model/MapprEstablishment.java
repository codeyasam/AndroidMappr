package org.mappr.org.mappr.model;

import android.graphics.Bitmap;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by codeyasam on 6/3/16.
 */
public class MapprEstablishment {

    private String id;
    private String owner_id;
    private String category_id;
    private String name;
    private String description;
    private Bitmap display_picture;
    private String tags;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Bitmap getDisplay_picture() {
        return display_picture;
    }

    public void setDisplay_picture(Bitmap display_picture) {
        this.display_picture = display_picture;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public static MapprEstablishment instantiateJSONEstablishment(JSONObject eachEstab) {

        try {
            MapprEstablishment estab = new MapprEstablishment();
            estab.setId(eachEstab.getString("id"));
            estab.setName(eachEstab.getString("name"));
            estab.setCategory_id(eachEstab.getString("category_id"));
            estab.setDescription(eachEstab.getString("description"));
            estab.setOwner_id(eachEstab.getString("owner_id"));
            Bitmap bmp = CYM_Utility.loadImageFromServer(CYM_Utility.MAPPR_PUBLIC_URL + eachEstab.getString("display_picture"));
            estab.setDisplay_picture(bmp);
            return estab;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
