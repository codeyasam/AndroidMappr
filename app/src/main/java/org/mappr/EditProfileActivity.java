package org.mappr;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.codeyasam.mappr.MapprSession;
import com.example.codeyasam.mappr.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.mappr.org.mappr.model.CYM_Utility;
import org.mappr.org.mappr.model.JSONParser;
import org.mappr.org.mappr.model.MapprEndUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private static final String REGISTER_LOADER_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/androidLoadProfile.php";
    private static final String PROFILE_EDITOR_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/androidEditProfile.php";
    private static final int SELECT_FILE = 888;
    private static final int REQUEST_CAMERA = 777;

    private MapprEndUser endUser;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        endUser = new MapprEndUser();
        endUser.setId(settings.getString(MapprSession.LOGGED_USER_ID, ""));
        MapprEndUser.setDefaultImage(EditProfileActivity.this, R.id.displayPicture, R.drawable.defaultavatar);
        ProfileLoader profileLoader = new ProfileLoader();
        profileLoader.execute();
    }

    public void editProfile(View v) {
        String firstName = CYM_Utility.getText(EditProfileActivity.this, R.id.firstNameTxt);
        String lastName = CYM_Utility.getText(EditProfileActivity.this, R.id.lastNameTxt);

        if (firstName.isEmpty() || lastName.isEmpty()) {
            CYM_Utility.mAlertDialog("Fill all required fields", EditProfileActivity.this);
            return;
        }

        endUser.setFirstName(firstName);
        endUser.setLastName(lastName);
        new ProfileEditor().execute();
    }

    class ProfileLoader extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EditProfileActivity.this);
            progressDialog.setMessage("Loading Profile...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("userId", endUser.getId()));
                params.add(new BasicNameValuePair("getUser", "true"));
                JSONObject json = JSONParser.makeHttpRequest(REGISTER_LOADER_URL, "GET", params);
                endUser = MapprEndUser.instantiateJSONUser(json, 100, 100);
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                try {
                    Bitmap bmp = CYM_Utility.getRoundedCornerBitmap(endUser.getDisplay_picture());
                    CYM_Utility.setText(EditProfileActivity.this, R.id.firstNameTxt, endUser.getFirstName());
                    CYM_Utility.setText(EditProfileActivity.this, R.id.lastNameTxt, endUser.getLastName());
                    CYM_Utility.setImageOnView(EditProfileActivity.this, R.id.displayPicture, bmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ProfileEditor extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EditProfileActivity.this);
            progressDialog.setMessage("Saving Changes...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                if (endUser.getDisplay_picture() != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    endUser.getDisplay_picture().compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    String encodedImage = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
                    params.add(new BasicNameValuePair("image", encodedImage));
                } else {
                    params.add(new BasicNameValuePair("display_picture", endUser.getDisplay_picture_path()));
                }

                params.add(new BasicNameValuePair("firstName", endUser.getFirstName()));
                params.add(new BasicNameValuePair("lastName", endUser.getLastName()));
                params.add(new BasicNameValuePair("userId", endUser.getId()));
                params.add(new BasicNameValuePair("submit", "true"));
                JSONObject json = JSONParser.makeHttpRequest(PROFILE_EDITOR_URL, "POST", params);
                return json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            Log.i("poop", result);
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getString("success").equals("true")) {
                        CYM_Utility.mAlertDialog("Profile Successfully Updated", EditProfileActivity.this);
                    } else {
                        CYM_Utility.mAlertDialog("Failed. Something wen't wrong.", EditProfileActivity.this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private Uri imageUri;
    public void choosePicture(View v) {
        final CharSequence[] items = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CHOOSE PHOTO FROM");
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("Camera")) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                    EditProfileActivity.this.imageUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[which].equals("Gallery")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_FILE);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });

        builder.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    handleRequestCamera();
                    break;
                case SELECT_FILE:
                    handleSelectFile(data);
                    break;
                default:

            }
        }
    }

    private void handleRequestCamera() {
        int dimenDP = (int) CYM_Utility.dipToPixels(EditProfileActivity.this, 150);
        String imagePath = CYM_Utility.getPath(imageUri, EditProfileActivity.this);
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
        bmp = Bitmap.createScaledBitmap(bmp, dimenDP, dimenDP, false);
        processImage(bmp, imagePath);
    }

    private void handleSelectFile(Intent data) {
        int dimenDP = (int) CYM_Utility.dipToPixels(EditProfileActivity.this, 250);
        Uri imageUri = data.getData();
        String imagePath = CYM_Utility.getPath(imageUri, EditProfileActivity.this);
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
        bmp = Bitmap.createScaledBitmap(bmp, dimenDP, dimenDP, false);
        processImage(bmp, imagePath);
    }

    private void processImage(Bitmap bmp, String imagePath) {
        try {
            Matrix matrix = CYM_Utility.getMatrixAngle(imagePath);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            endUser.setDisplay_picture(bmp);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            bmp = CYM_Utility.getRoundedCornerBitmap(bmp);
            CYM_Utility.setImageOnView(EditProfileActivity.this, R.id.displayPicture, bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
