package com.richo.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

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
            Intent intent = new Intent(ItemListActivity.this, ItemDetailActivity.class);
            intent.putExtra("item_id", itemId);
            startActivity(intent);
        });

    }
}
