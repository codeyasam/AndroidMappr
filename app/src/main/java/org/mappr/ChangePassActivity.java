package org.mappr;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.codeyasam.mappr.MapprSession;
import com.example.codeyasam.mappr.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.mappr.org.mappr.model.CYM_Utility;
import org.mappr.org.mappr.model.JSONParser;

import java.util.ArrayList;
import java.util.List;

public class ChangePassActivity extends AppCompatActivity {

    private static final String CHANGE_PASS_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/androidChangePass.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void changePass(View v) {
        String oldPass = CYM_Utility.getText(ChangePassActivity.this, R.id.oldPass);
        String newPass = CYM_Utility.getText(ChangePassActivity.this, R.id.newPass);
        String confPass = CYM_Utility.getText(ChangePassActivity.this, R.id.confPass);
        if (oldPass.isEmpty() || newPass.isEmpty() || confPass.isEmpty()) {
            CYM_Utility.mAlertDialog("Fill up all required fields", ChangePassActivity.this);
            return;
        } else if (!newPass.equals(confPass)) {
            CYM_Utility.mAlertDialog("Passwords don't match", ChangePassActivity.this);
            return;
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userId = settings.getString(MapprSession.LOGGED_USER_ID, "");

        ChangePassExecutor task = new ChangePassExecutor(oldPass, newPass, userId);
        task.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    class ChangePassExecutor extends AsyncTask<String, String, String> {

        private String oldPass;
        private String newPass;
        private String userId;
        ProgressDialog progressDialog;

        public ChangePassExecutor(String oldPass, String newPass, String userId) {
            this.oldPass = oldPass;
            this.newPass = newPass;
            this.userId = userId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ChangePassActivity.this);
            progressDialog.setMessage("Loading... Please wait.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("oldPass", oldPass));
                params.add(new BasicNameValuePair("newPass", newPass));
                params.add(new BasicNameValuePair("userId", userId));
                params.add(new BasicNameValuePair("submit", "true"));
                JSONObject json = JSONParser.makeHttpRequest(CHANGE_PASS_URL, "POST", params);
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
                    JSONObject json = new JSONObject(result);
                    //Log.i("poop", "Change pass success: " + json.getString("success"));
                    Log.i("poop", result);
                    if (json.getString("success").equals("true")) {
                        CYM_Utility.mAlertDialog("Successfully changed the password", ChangePassActivity.this);
                        CYM_Utility.setText(ChangePassActivity.this, R.id.oldPass, "");
                        CYM_Utility.setText(ChangePassActivity.this, R.id.newPass, "");
                        CYM_Utility.setText(ChangePassActivity.this, R.id.confPass, "");
                    } else {
                        if (json.has("msg")) {
                            CYM_Utility.mAlertDialog(json.getString("msg"), ChangePassActivity.this);
                        } else {
                            CYM_Utility.mAlertDialog("Something wen't wrong. Try again later.", ChangePassActivity.this);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.i("poop", "the result is null");
            }
        }
    }
}
