package org.mappr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.codeyasam.mappr.R;

import org.mappr.org.mappr.model.CYM_Utility;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void searchClick(View v) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_STRING);
        intent.putExtra("searchString", CYM_Utility.getText(this, R.id.searchTxt));
        startActivity(intent);
    }
}
