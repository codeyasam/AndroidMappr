package com.example.codeyasam.mappr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFacebookButton();
        setContentView(R.layout.activity_mappr_register);
        Button connectFbBtn = (Button) findViewById(R.id.connectFbBtn);
        connectFbBtn.setOnClickListener(this);

        String lastActivity = getIntent().getStringExtra(CYM_Utility.MAPPR_OPT);
        if (lastActivity.equals(MapprLogin.ACTIVITY_STRING)) {
            String username = getIntent().getStringExtra("username");
            String password = getIntent().getStringExtra("password");

            CYM_Utility.setText(MapprRegister.this, R.id.usernameTxt, username);
            CYM_Utility.setText(MapprRegister.this, R.id.passwordTxt, password);

        }


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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void customFacebookLogin() {
        Log.i("poop", "here");
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
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
                                            String fbFirstName = object.getString("first_name");
                                            String fbLastName = object.getString("last_name");
                                            String fbGender = object.getString("gender");
                                            final String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                            //Bitmap profilePic = getFacebookProfilePicture(profilePicUrl);
                                            //final List<Bitmap> bmp = new ArrayList<Bitmap>();
                                            RegisterLoader task = new RegisterLoader();
                                            task.setProfilePicUrl(profilePicUrl);
                                            task.execute();

                                            CYM_Utility.setText(MapprRegister.this, R.id.firstNameTxt, fbFirstName);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {
                                            LoginManager.getInstance().logOut();
                                        }
                                        // 01/31/1980 format
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, first_name, last_name, gender, picture.type(large)");
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
