package com.zoomers.flowerfinder.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.zoomers.flowerfinder.R;

/**
 * The activity that runs when a history item is clicked/tapped
 * to show the map in which where the item was detected
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "MapActivity";
    private double flowerLong;
    private double flowerLat;
    private String flowerClass;
    private MapView mMapView;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //back button
        backButton = findViewById(R.id.mapBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            //closes out of current activity and goes back to previous.
            public void onClick(View view) {
                finish();
            }
        });

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = findViewById(R.id.mapView2);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        //getting params from previous activity
        Intent intent = getIntent();
        flowerLat = intent.getDoubleExtra("flowerLat", 0);
        flowerLong = intent.getDoubleExtra("flowerLong", 0);
        flowerClass = intent.getStringExtra("flowerClass");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng flowerPos = new LatLng(flowerLat, flowerLong);
        googleMap.addMarker(new MarkerOptions().position(flowerPos).title(flowerClass));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(flowerPos, 15));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}