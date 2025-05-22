package com.richo.lostandfound;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

public class AddItemActivity extends AppCompatActivity {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1001;

    private EditText etTitle, etDescription, etDate, etLocation, etContact;
    private Button btnGetLocation, btnSave;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper dbHelper;

    private double lat = 0.0, lng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_activity);

        // Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Bind UI
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etDate = findViewById(R.id.et_date);
        etLocation = findViewById(R.id.et_location);
        etContact = findViewById(R.id.et_contact);
        btnGetLocation = findViewById(R.id.btn_get_location);
        btnSave = findViewById(R.id.btn_save);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Autocomplete click
        etLocation.setFocusable(false);
        etLocation.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        // Get current GPS location
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());

        // Save item
        btnSave.setOnClickListener(v -> saveItem());
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                etLocation.setText("Lat: " + lat + ", Lng: " + lng);
            } else {
                Toast.makeText(this, "Unable to get location. Make sure GPS is enabled.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveItem() {
        String title = etTitle.getText().toString();
        String description = etDescription.getText().toString();
        String date = etDate.getText().toString();
        String contact = etContact.getText().toString();

        if (title.isEmpty() || date.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.insertItem(title, description, date, contact, lat, lng);
        Toast.makeText(this, "Item saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                etLocation.setText(place.getAddress());

                if (place.getLatLng() != null) {
                    lat = place.getLatLng().latitude;
                    lng = place.getLatLng().longitude;
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Autocomplete error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
