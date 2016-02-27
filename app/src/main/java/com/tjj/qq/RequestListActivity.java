package com.tjj.qq;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

public class RequestListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.request_list_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_request_list, menu);
        return true;
    }

//    @Override
//    public boolean onMenuOpened(int featureId, Menu menu) {
//        Intent intent = new Intent(this, InputTabActivity.class);
//        startActivity(intent);
//        return true;
//    }
}
