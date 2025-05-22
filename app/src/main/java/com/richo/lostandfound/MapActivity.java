package com.richo.lostandfound;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Setup ActionBar back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Request location permission if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        loadItemsAndAddMarkers();
    }

    private void loadItemsAndAddMarkers() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_ITEMS, null);

        if (cursor != null && cursor.moveToFirst()) {
            boolean firstMarkerPlaced = false;
            LatLng firstLatLng = null;

            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TITLE));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LNG));

                // Skip items without valid coordinates
                if (lat == 0.0 && lng == 0.0) {
                    continue;
                }

                LatLng position = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(title)
                        .snippet(desc));

                if (!firstMarkerPlaced) {
                    firstLatLng = position;
                    firstMarkerPlaced = true;
                }

            } while (cursor.moveToNext());

            cursor.close();

            if (firstLatLng != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 12));
            }
        }
    }


    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    // Handle back button in ActionBar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
