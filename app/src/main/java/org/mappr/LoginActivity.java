package org.mappr;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.codeyasam.mappr.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.mappr.org.mappr.model.CYM_Utility;
import org.mappr.org.mappr.model.JSONParser;
import org.mappr.org.mappr.model.MapprSession;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String LOGIN_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/androidlogin.php";
    public static final String ACTIVITY_STRING = "LoginForm";

    private String branchId;
    private String last_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappr_login);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("branch_id")) {
                branchId = extras.getString("branch_id");
            }

            if (extras.containsKey(CYM_Utility.MAPPR_FORM)) {
                last_activity = extras.getString(CYM_Utility.MAPPR_FORM);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!MapprSession.isLoggedIn) {  //for debugging
            // if (settings.getString(MapprSession.LOGGED_USER_ID, "").isEmpty()) {
            Log.i("poop", "hindi ka pa nakalogin");
        } else {
            finish();
        }
    }

    public void loginClick(View v) {
        Log.i("poop", "login clicked");
        //finish();
        if (last_activity.equals(CYM_Utility.FROM_DETAILS)) {
            new LoginConnector(EstablishmentDetails.class).execute();
        } else if (last_activity.equals(CYM_Utility.FROM_FAVORITES)) {
            new LoginConnector(FavoritesActivity.class).execute();
        }
    }

    public void registerClick(View v) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        intent.putExtra(CYM_Utility.MAPPR_OPT, ACTIVITY_STRING);
        intent.putExtra("username", CYM_Utility.getText(LoginActivity.this, R.id.usernameTxt));
        intent.putExtra("password", CYM_Utility.getText(LoginActivity.this, R.id.passwordTxt));
        if (branchId != null) {
            intent.putExtra("branch_id", branchId);
        }
        startActivity(intent);
    }

    class LoginConnector extends AsyncTask<String, String, String> {

        Class destinationClass;

        public LoginConnector(Class destinationClass) {
            this.destinationClass = destinationClass;
        }


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("username", CYM_Utility.getText(LoginActivity.this, R.id.usernameTxt)));
                params.add(new BasicNameValuePair("password", CYM_Utility.getText(LoginActivity.this, R.id.passwordTxt)));
                JSONObject json = JSONParser.makeHttpRequest(LOGIN_URL, "GET", params);
                return json.toString();
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getString("result").equals("true")) {
                        MapprSession.isLoggedIn = true;
                        //Intent intent = new Intent(MapprLogin.this, MapprDetails.class);
//                        Intent intent = new Intent(LoginActivity.this, destinationClass);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                        if (branchId != null) {
//                            //intent.putExtra("branch_id", branchId);
//                        }
//                        MapprSession.logUser(LoginActivity.this, json.getString("user_id"));
//                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {

                }
            }
        }
    }
}
