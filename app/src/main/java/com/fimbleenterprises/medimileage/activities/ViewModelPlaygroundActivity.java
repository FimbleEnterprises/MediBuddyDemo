package com.fimbleenterprises.medimileage.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.viewmodels.ViewModelPlaygroundPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class ViewModelPlaygroundActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_model_playground);
        ViewModelPlaygroundPagerAdapter viewPagerAdapter =
                new ViewModelPlaygroundPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }
}