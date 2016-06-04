package com.example.codeyasam.mappr;

import android.graphics.Bitmap;

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

}
