package com.richo.lostandfound;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


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
                    Toast.makeText(ItemDetailActivity.this, "Item deleted.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ItemDetailActivity.this, "Failed to delete.", Toast.LENGTH_SHORT).show();
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
