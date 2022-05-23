package com.example.covid19trackingandnews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FullView extends AppCompatActivity {
    // For RecyclerView
    private RecyclerView itemRV;
    private ArrayList<newsitem> newsItemArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_view);

        itemRV = findViewById(R.id.savedNewsView);
        newsItemArrayList = (ArrayList<newsitem>) getIntent().getSerializableExtra("NEWS");
        getNewsUpdateUI();
    }

    // Bundle savedInstanceState
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("NEWSLIST", newsItemArrayList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        newsItemArrayList = (ArrayList<newsitem>) savedInstanceState.getSerializable("NEWSLIST");
        getNewsUpdateUI();
    }

    public void goBack(View view)
    {
        super.onBackPressed();
    }

    private void getNewsUpdateUI(){
        if (newsItemArrayList != null)
        {
            ItemAdapter itemAdapter = new ItemAdapter(this, newsItemArrayList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            itemRV.setLayoutManager(linearLayoutManager);
            itemRV.setAdapter(itemAdapter);
        } else {
            Toast.makeText(this, "Cannot present list - ArrayList is null" , Toast.LENGTH_LONG).show();
        }
    }
}