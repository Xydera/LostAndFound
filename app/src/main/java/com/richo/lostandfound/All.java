package com.richo.lostandfound;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.richo.lostandfound.databinding.FragmentFirstBinding;
import com.richo.lostandfound.databinding.FragmentSecondBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class All {

    public class MainActivity extends AppCompatActivity {

        private Button btnCreateAdvert;
        private Button btnViewItems;
        private Button btnMap;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            btnCreateAdvert = findViewById(R.id.btn_create_advert);
            btnViewItems = findViewById(R.id.btn_view_items);
            btnMap = findViewById(R.id.btn_show_map);

            btnCreateAdvert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open AddItemActivity
                    Intent intent = new Intent(com.richo.lostandfound.MainActivity.this, AddItemActivity.class);
                    startActivity(intent);
                }
            });

            btnViewItems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open ItemListActivity
                    Intent intent = new Intent(com.richo.lostandfound.MainActivity.this, ItemListActivity.class);
                    startActivity(intent);
                }
            });

            btnMap.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    // Open MapActivity
                    Intent intent = new Intent(com.richo.lostandfound.MainActivity.this, MapActivity.class);
                    startActivity(intent);
                }
            });
        }
    }


    public class SecondFragment extends Fragment {

        private FragmentSecondBinding binding;

        @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState
        ) {

            binding = FragmentSecondBinding.inflate(inflater, container, false);
            return binding.getRoot();

        }

        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            binding.buttonSecond.setOnClickListener(v ->
                    NavHostFragment.findNavController(com.richo.lostandfound.SecondFragment.this)
                            .navigate(R.id.action_SecondFragment_to_FirstFragment)
            );
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }

    }

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
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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

    public class ItemListActivity extends AppCompatActivity {

        private ListView listViewItems;
        private DatabaseHelper dbHelper;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_item_list);

            listViewItems = findViewById(R.id.list_view_items);
            dbHelper = new DatabaseHelper(this);


            List<String> itemList = dbHelper.getAllItemsAsStringList();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    itemList
            );
            listViewItems.setAdapter(adapter);

            listViewItems.setOnItemClickListener((parent, view, position, id) -> {
                String selectedItem = (String) parent.getItemAtPosition(position);
                int itemId = Integer.parseInt(selectedItem.split(":")[0].trim());
                Intent intent = new Intent(com.richo.lostandfound.ItemListActivity.this, ItemDetailActivity.class);
                intent.putExtra("item_id", itemId);
                startActivity(intent);
            });

        }
    }

    public class ItemDetailActivity extends AppCompatActivity {

        private TextView tvTitle, tvDescription, tvDate, tvLocation, tvContact;
        private Button btnDelete;

        private DatabaseHelper dbHelper;
        private int itemId;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_item_detail);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            tvTitle = findViewById(R.id.tv_title);
            tvDescription = findViewById(R.id.tv_description);
            tvDate = findViewById(R.id.tv_date);
            tvLocation = findViewById(R.id.tv_location);
            tvContact = findViewById(R.id.tv_contact);
            btnDelete = findViewById(R.id.btn_delete);

            dbHelper = new DatabaseHelper(this);

            Intent intent = getIntent();
            itemId = intent.getIntExtra("item_id", -1);

            if (itemId != -1) {
                loadItemDetails(itemId);
            }

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean deleted = dbHelper.deleteItem(itemId);
                    if (deleted) {
                        Toast.makeText(com.richo.lostandfound.ItemDetailActivity.this, "Item deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(com.richo.lostandfound.ItemDetailActivity.this, "Failed to delete.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void loadItemDetails(int id) {
            Cursor cursor = dbHelper.getItemById(id);
            if (cursor != null && cursor.moveToFirst()) {
                tvTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TITLE)));
                tvDescription.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION)));
                tvDate.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE)));

                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LNG));
                tvLocation.setText("Lat: " + lat + ", Lng: " + lng);

                tvContact.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT)));
                cursor.close();
            }
        }

        @Override
        public boolean onSupportNavigateUp() {
            finish();
            return true;
        }
    }

    public class FirstFragment extends Fragment {

        private FragmentFirstBinding binding;

        @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState
        ) {

            binding = FragmentFirstBinding.inflate(inflater, container, false);
            return binding.getRoot();

        }

        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            binding.buttonFirst.setOnClickListener(v ->
                    NavHostFragment.findNavController(com.richo.lostandfound.FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment)
            );
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }

    }

    public class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "lostfound.db";
        private static final int DATABASE_VERSION = 3;

        public static final String TABLE_ITEMS = "items";
        public static final String COL_ID = "id";
        public static final String COL_TITLE = "title";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_DATE = "date";
        public static final String COL_CONTACT = "contact";
        public static final String COL_LAT = "latitude";
        public static final String COL_LNG = "longitude";


        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTable = "CREATE TABLE " + TABLE_ITEMS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TITLE + " TEXT, " +
                    COL_DESCRIPTION + " TEXT, " +
                    COL_DATE + " TEXT, " +
                    COL_CONTACT + " TEXT," +
                    COL_LAT + " REAL, " +
                    COL_LNG + " REAL" +
                    ")";
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
            onCreate(db);
        }

        public boolean insertItem(String title, String description, String date, String contact, double lat, double lng) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, title);
            values.put(COL_DESCRIPTION, description);
            values.put(COL_DATE, date);
            values.put(COL_CONTACT, contact);
            values.put(COL_LAT, lat);
            values.put(COL_LNG, lng);
            long result = db.insert(TABLE_ITEMS, null, values);
            return result != -1;
        }

        public List<String> getAllItemsAsStringList() {
            List<String> itemList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                    itemList.add(id + ": " + title);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return itemList;
        }

        public Cursor getItemById(int id) {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " WHERE " + COL_ID + " = ?", new String[]{String.valueOf(id)});
        }

        public boolean deleteItem(int id) {
            SQLiteDatabase db = this.getWritableDatabase();
            int result = db.delete(TABLE_ITEMS, COL_ID + " = ?", new String[]{String.valueOf(id)});
            return result > 0;
        }
    }

    public class AddItemActivity extends AppCompatActivity {

        private static final int AUTOCOMPLETE_REQUEST_CODE = 1001;

        private EditText etTitle, etDescription, etDate, etLocation, etContact;
        private Button btnGetLocation, btnSave;
        private FusedLocationProviderClient fusedLocationClient;
        private com.richo.lostandfound.DatabaseHelper dbHelper;

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

            dbHelper = new com.richo.lostandfound.DatabaseHelper(this);
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


}
