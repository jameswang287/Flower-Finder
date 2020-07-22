
package com.zoomers.flowerfinder;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.zoomers.flowerfinder.ui.main.DetectFragment;
import com.zoomers.flowerfinder.ui.main.HistoryFragment;
import com.zoomers.flowerfinder.ui.main.SectionsPagerAdapter;

/**
 * The main driver for the app, composed of multiple Fragments acting as pages.
 */
public class MainActivity extends AppCompatActivity implements DetectFragment.OnPhotoTakenListener {
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof DetectFragment) {
            DetectFragment detectFragment = (DetectFragment) fragment;
            detectFragment.setOnPhotoTakenListener(this);
        }
    }

    @Override
    public void onPhotoTaken() {
        // get reference to HistoryFragment
        HistoryFragment historyFragment = (HistoryFragment) sectionsPagerAdapter.instantiateItem(viewPager, 1);
        // refresh
        historyFragment.refreshListView();
    }

}