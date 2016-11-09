package org.mappr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.codeyasam.mappr.R;

import org.mappr.org.mappr.model.CYM_Utility;
import org.mappr.org.mappr.model.MapprEstablishment;

public class GalleryImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_image);
        Intent intent = getIntent();
        int position = intent.getIntExtra("imageUrl", 0);
        Bitmap bmp = EstablishmentDetails.listBranchGallery.get(position);
        CYM_Utility.setImageOnView(GalleryImage.this, R.id.galleryImage, bmp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
