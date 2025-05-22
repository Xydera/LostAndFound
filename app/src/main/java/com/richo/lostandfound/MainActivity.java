package com.richo.lostandfound;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import android.content.Intent;
import android.widget.Button;


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
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(intent);
            }
        });

        btnViewItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open ItemListActivity
                Intent intent = new Intent(MainActivity.this, ItemListActivity.class);
                startActivity(intent);
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // Open MapActivity
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
}