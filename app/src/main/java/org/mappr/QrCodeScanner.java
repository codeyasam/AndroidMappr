package org.mappr;

import android.content.Intent;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.example.codeyasam.mappr.R;

import org.mappr.org.mappr.model.CYM_Utility;

public class QrCodeScanner extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    public static final int QR_SEARCH = 777;

    private QRCodeReaderView mydecoderview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_scanner);
        mydecoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        mydecoderview.setOnQRCodeReadListener(this);
        mydecoderview.getCameraManager().startPreview();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_estab_details, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("poop", "qrcode scanner onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mydecoderview.getCameraManager().stopPreview();
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        MapActivity.implementedQrSearch = false;
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(CYM_Utility.MAPPR_OPT, CYM_Utility.OPT_BY_QRCODE);
        intent.putExtra("branchID", text);
        startActivityForResult(intent, QR_SEARCH);
    }

    @Override
    public void cameraNotFound() {

    }

    @Override
    public void QRCodeNotFoundOnCamImage() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_SEARCH  && resultCode == RESULT_OK) {
            Log.i("poop", "finishes the qrcode scanner activity");
            finish();
        }
        Log.i("poop", "dumaan sa onActivityResult");
    }
}
