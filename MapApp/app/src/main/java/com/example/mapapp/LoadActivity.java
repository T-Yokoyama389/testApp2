package com.example.mapapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.List;
import java.util.Map;

public class LoadActivity extends AppCompatActivity {
    MapView mMapView;
    MapActivity mapActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        mapActivity = MapActivity.getInstance();
        mMapView = mapActivity.getmMapView();
        SetupOfflineMap setupOfflineMap = new SetupOfflineMap(mMapView, mapActivity, this);
        setupOfflineMap.start();
    }

    public void onFinish(){
        finish();
    }

    public void setText(){
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("地図ファイルが破損しています\n再ダウンロードを行ってください");
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(LoadActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
