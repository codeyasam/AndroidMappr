package org.mappr.org.mappr.model;

import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;

import com.example.codeyasam.mappr.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by codeyasam on 6/13/16.
 */
public class MapprBookmark {

    private String user_id;
    private String branch_id;
    private MenuItem bookmarkMenu;

    public MapprBookmark(String user_id, String branch_id, MenuItem bookmarkMenu) {
        this.user_id = user_id;
        this.branch_id = branch_id;
        this.bookmarkMenu = bookmarkMenu;
    }

    public void manageBookmark() {
        Log.i("poop", "bookmard click3");
        new BookmarkManager().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class BookmarkManager extends AsyncTask<String, String, String> {

        private static final String BOOKMARKED_URL = CYM_Utility.MAPPR_ROOT_URL + "tests/setBookmark.php";

        @Override
        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("user_id", user_id));
                params.add(new BasicNameValuePair("branch_id", branch_id));
                params.add(new BasicNameValuePair("submit", "true"));

                JSONObject json = JSONParser.makeHttpRequest(BOOKMARKED_URL, "POST", params);
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
                    Log.i("poop", "bookmard click4");
                    JSONObject json = new JSONObject(result);
                    if (json.getString("success").equals("create")) {
                        Log.i("poop", "bookmarked created/deleted");
                        bookmarkMenu.setIcon(R.drawable.bookmarked);
//                        bookmarkMenu.setTitle("BOOKMARKED");
                    } else if (json.getString("success").equals("delete")) {
//                        bookmarkMenu.setTitle("BOOKMARK");
                        bookmarkMenu.setIcon(R.drawable.add_bookmark);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
