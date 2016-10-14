package org.mappr.org.mappr.model;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by codeyasam on 6/17/16.
 */
public class MapprBranch {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEstab_id() {
        return estab_id;
    }

    public void setEstab_id(String estab_id) {
        this.estab_id = estab_id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getAddress() {
        //return address;
        return CYM_Utility.getDecodeString(address);
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public MapprEstablishment getMapprEstablishment() {
        return mapprEstablishment;
    }

    public void setMapprEstablishment(MapprEstablishment mapprEstablishment) {
        this.mapprEstablishment = mapprEstablishment;
    }

    public static MapprBranch instantiateBranch(JSONObject branch, HashMap<String, MapprEstablishment> estabHm) {
        try {
            MapprBranch branchObj = new MapprBranch();
            branchObj.setId(branch.getString("id"));
            branchObj.setEstab_id(branch.getString("estab_id"));
            branchObj.setLat(branch.getString("lat"));
            branchObj.setLng(branch.getString("lng"));
            branchObj.setAddress(branch.getString("address"));
            branchObj.setMapprEstablishment(estabHm.get(branchObj.getEstab_id()));

            return branchObj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String id;
    private String estab_id;
    private String lat;
    private String lng;
    private String address;

    private MapprEstablishment mapprEstablishment;
}
