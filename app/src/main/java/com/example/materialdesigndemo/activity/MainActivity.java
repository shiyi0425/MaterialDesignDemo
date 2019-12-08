package com.example.materialdesigndemo.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.materialdesigndemo.R;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private Button btnRecycler;
    private Button btnTalLayout;
    private Button btnNavigation;
    private Button btnCoorinator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar=findViewById(R.id.tool_bar);
        toolbar.setTitle("Android 5.0新特性");
        toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Material Design控件使用");
        setSupportActionBar(toolbar);

        btnNavigation = findViewById(R.id.btn_navigation);
        btnRecycler = findViewById(R.id.btn_recycler);
        btnTalLayout = findViewById(R.id.btn_tablayout);
        btnCoorinator=findViewById(R.id.btn_coorinator);

        btnTalLayout.setOnClickListener(this);
        btnNavigation.setOnClickListener(this);
        btnRecycler.setOnClickListener(this);
        btnCoorinator.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_recycler:
                Intent intent = new Intent(MainActivity.this, RecyclerViewActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_coorinator:
                intent = new Intent(MainActivity.this, CoorinatorActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_tablayout:
                intent = new Intent(MainActivity.this, TabLayoutActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_navigation:
                intent = new Intent(MainActivity.this, DrawerNavigationActivity.class);
                startActivity(intent);
                break;
        }
    }
}
