package com.example.codeyasam.mappr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by codeyasam on 6/10/16.
 */
public class MapprEndUser {

    private Activity activity;

    public MapprEndUser(Activity activity) {
        this.activity = activity;
        //debugging purposes
        //setDefaultDp(activity, R.drawable.defaultavatar);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getDisplay_picture_path() {
        return display_picture_path;
    }

    public void setDisplay_picture_path(String display_picture_path) {
        this.display_picture_path = display_picture_path;
    }

    public Bitmap getDisplay_picture() {
        return display_picture;
    }

    public void setDisplay_picture(Bitmap display_picture) {
        this.display_picture = display_picture;
    }

    public void setDefaultDp(Activity activity, int drawable) {
        Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), drawable);
        setDisplay_picture(bm);
    }

    public static void setDefaultImage(Activity activity, int id, int drawable) {
        ImageView ivImage = (ImageView) activity.findViewById(id);
        Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), drawable);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        ivImage.setImageBitmap(CYM_Utility.getRoundedCornerBitmap(bm));
    }

    public void registerUser() {
        new RegisterExecuter().execute();
    }

    public void registerUser(String branchId) {
        RegisterExecuter task = new RegisterExecuter();
        task.setBranchId(branchId);
        task.execute();
    }

    private String id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String contact;
    private String hometown;
    private String display_picture_path = "DISPLAY_PICTURES/defaultavatar.png";


    private Bitmap display_picture;
    private String user_type = "USER";

    class RegisterExecuter extends AsyncTask<String, String, String> {

        private static final String MAPPR_TESTS = CYM_Utility.MAPPR_ROOT_URL + "tests/base64img.php";

        public String getBranchId() {
            return branchId;
        }

        public void setBranchId(String branchId) {
            this.branchId = branchId;
        }

        private String branchId;

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                if (display_picture != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    display_picture.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    String encodedImage = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
                    params.add(new BasicNameValuePair("image", encodedImage));
                } else {
                    params.add(new BasicNameValuePair("display_picture", display_picture_path));
                }
                params.add(new BasicNameValuePair("first_name", firstName));
                params.add(new BasicNameValuePair("last_name", lastName));
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));
                params.add(new BasicNameValuePair("email", email));
                params.add(new BasicNameValuePair("submit", "true"));

                JSONObject json = JSONParser.makeHttpRequest(MAPPR_TESTS, "POST", params);
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    Log.i("poop", json.getString("success"));
                    if (json.getString("success").equals("true")) {
                        MapprSession.isLoggedIn = true;
                        Intent intent = new Intent(activity, MapprDetails.class);
                        if (branchId != null) {
                            intent.putExtra("branch_id", branchId);
                        }
                        MapprSession.logUser(activity, json.getString("id"));
                        activity.startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

}
