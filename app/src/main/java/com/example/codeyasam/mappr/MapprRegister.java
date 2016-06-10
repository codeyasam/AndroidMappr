package com.example.codeyasam.mappr;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Arrays;

public class MapprRegister extends AppCompatActivity implements View.OnClickListener{

    private CallbackManager callbackManager;
    private MapprEndUser endUser;

    private static final int SELECT_FILE = 888;
    private static final int REQUEST_CAMERA = 777;

    private String branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFacebookButton();
        setContentView(R.layout.activity_mappr_register);
        endUser = new MapprEndUser(MapprRegister.this);
        Button connectFbBtn = (Button) findViewById(R.id.connectFbBtn);
        connectFbBtn.setOnClickListener(this);

        try {
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey(CYM_Utility.MAPPR_OPT)) {
                    String lastActivity = getIntent().getStringExtra(CYM_Utility.MAPPR_OPT);
                    if (lastActivity.equals(MapprLogin.ACTIVITY_STRING)) {
                        String username = getIntent().getStringExtra("username");
                        String password = getIntent().getStringExtra("password");

                        CYM_Utility.setText(MapprRegister.this, R.id.usernameTxt, username);
                        CYM_Utility.setText(MapprRegister.this, R.id.passwordTxt, password);
                    }
                }

                if (extras.containsKey("branch_id")) {
                    branchId = extras.getString("branch_id");
                }
            }


        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        MapprEndUser.setDefaultImage(MapprRegister.this, R.id.displayPicture, R.drawable.cameralogo);
    }

    private void setupFacebookButton() {
        //call these before setContentView
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logOut();
    }

    public static Bitmap getFacebookProfilePicture(String url){
        try {
            URL facebookProfileURL= new URL(url);
            Bitmap bitmap = BitmapFactory.decodeStream(facebookProfileURL.openConnection().getInputStream());
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connectFbBtn:
                customFacebookLogin();
                break;
        }
    }

    public void registerUser(View v) {
        endUser.setUsername(CYM_Utility.getText(MapprRegister.this, R.id.usernameTxt));
        endUser.setPassword(CYM_Utility.getText(MapprRegister.this, R.id.passwordTxt));
        endUser.registerUser(branchId);
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
                    MapprRegister.this.imageUri = getContentResolver().insert(
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
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    handleRequestCamera();
                    break;
                case SELECT_FILE:
                    break;
                default:

            }
        }
    }

    private void handleRequestCamera() {
        int dimenDP = (int) CYM_Utility.dipToPixels(MapprRegister.this, 150);
        String imagePath = CYM_Utility.getPath(imageUri, MapprRegister.this);
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
        bmp = Bitmap.createScaledBitmap(bmp, dimenDP, dimenDP, false);
        try {
            Matrix matrix = CYM_Utility.getMatrixAngle(imagePath);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            endUser.setDisplay_picture(bmp);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            CYM_Utility.setImageOnView(MapprRegister.this, R.id.displayPicture, bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSelectFile(Intent data) {
        int dimenDP = (int) CYM_Utility.dipToPixels(MapprRegister.this, 250);
        Uri imageUri = data.getData();
        String imagePath = CYM_Utility.getPath(imageUri, MapprRegister.this);

    }

    private void customFacebookLogin() {
        Log.i("poop", "here");
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());

                                        // Application code
                                        try {
                                            endUser.setFirstName(object.getString("first_name"));
                                            endUser.setLastName(object.getString("last_name"));
                                            final String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                            endUser.setDisplay_picture_path("//graph.facebook.com/" + object.getString("id") + "/picture?type=large");
                                            endUser.setDisplay_picture(null);
                                            endUser.setEmail(object.getString("email"));
                                            RegisterLoader task = new RegisterLoader();
                                            task.setProfilePicUrl(profilePicUrl);
                                            task.execute();

                                            CYM_Utility.setText(MapprRegister.this, R.id.firstNameTxt, endUser.getFirstName());
                                            CYM_Utility.setText(MapprRegister.this, R.id.lastNameTxt, endUser.getLastName());
                                            CYM_Utility.setText(MapprRegister.this, R.id.emailTxt, endUser.getEmail());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {
                                            LoginManager.getInstance().logOut();
                                        }
                                        // 01/31/1980 format
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, first_name, last_name, gender, email, picture.type(large)");
                        request.setParameters(parameters);
                        request.executeAsync();
                        Log.i("poop", "right here");
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.i("poop", exception.toString());
                    }
                });
    }

    class RegisterLoader extends AsyncTask<String, String, String> {

        private static final String RESPONSE_COMPLETE = "success";
        private String profilePicUrl;
        private ByteArrayOutputStream stream;
        private Bitmap fbBMP;

        public void setProfilePicUrl(String profilePicUrl) {
            this.profilePicUrl = profilePicUrl;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Bitmap profilePic = getFacebookProfilePicture(profilePicUrl);
                stream = new ByteArrayOutputStream();
                profilePic.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Log.i("debuggin", profilePicUrl);
                fbBMP = profilePic;
                return RESPONSE_COMPLETE;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Bitmap bmp = CYM_Utility.getRoundedCornerBitmap(fbBMP);
                CYM_Utility.setImageOnView(MapprRegister.this, R.id.displayPicture, bmp);
            }
        }
    }



}
