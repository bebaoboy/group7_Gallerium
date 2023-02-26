package com.group7.gallerium.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group7.gallerium.R;
import com.group7.gallerium.adapters.ViewPagerAdapter;
import com.karan.churi.PermissionManager.PermissionManager;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottom_nav;
    ViewPager2 view_pager;

    PermissionManager permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        bottom_nav = findViewById(R.id.bottom_navigation);
        view_pager = findViewById(R.id.view_pager);

        initializeViewPager();

        permission=new PermissionManager() {

        };
        permission.checkAndRequestPermissions(this);

        bottom_nav.setOnItemSelectedListener(navItem -> {
            switch (navItem.getItemId()) {
                case R.id.photo_nav_item -> view_pager.setCurrentItem(0);

                case R.id.album_nav_item -> view_pager.setCurrentItem(1);

                case R.id.secured_nav_item -> view_pager.setCurrentItem(2);

                case R.id.fav_nav_item -> view_pager.setCurrentItem(3);

                default -> throw new IllegalStateException("Unexpected value: " + navItem.getItemId());
            }
            return true;
        });
    }

    private void initializeViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPagerAdapter.setContext(getApplicationContext());
        view_pager.setAdapter(viewPagerAdapter);

        view_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0->
                        bottom_nav.getMenu().findItem(R.id.photo_nav_item).setChecked(true);
                    case 1->
                        bottom_nav.getMenu().findItem(R.id.album_nav_item).setChecked(true);
                    case 2 ->
                        bottom_nav.getMenu().findItem(R.id.secured_nav_item).setChecked(true);
                    case 3->
                        bottom_nav.getMenu().findItem(R.id.fav_nav_item).setChecked(true);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permission.checkResult(requestCode, permissions, grantResults);
    }


}