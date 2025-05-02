package com.richo.lostandfound;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.richo.lostandfound.DatabaseHelper;

public class AddItemActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDate, etLocation, etContact;
    private Button btnSave;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_activity);

        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etDate = findViewById(R.id.et_date);
        etLocation = findViewById(R.id.et_location);
        etContact = findViewById(R.id.et_contact);
        btnSave = findViewById(R.id.btn_save);


        dbHelper = new DatabaseHelper(this);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString();
                String description = etDescription.getText().toString();
                String date = etDate.getText().toString();
                String location = etLocation.getText().toString();
                String contact = etContact.getText().toString();

                boolean inserted = dbHelper.insertItem(title, description, date, location, contact);
                if (inserted) {
                    Toast.makeText(AddItemActivity.this, "Advert saved!", Toast.LENGTH_SHORT).show();
                    finish(); // go back to main screen
                } else {
                    Toast.makeText(AddItemActivity.this, "Failed to save advert.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}