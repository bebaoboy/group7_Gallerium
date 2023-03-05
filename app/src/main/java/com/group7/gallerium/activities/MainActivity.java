package com.group7.gallerium.activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group7.gallerium.R;
import com.group7.gallerium.adapters.ViewPagerAdapter;
import com.group7.gallerium.fragments.AlbumFragment;
import com.group7.gallerium.fragments.MediaFragment;
import com.group7.gallerium.utilities.BottomNavigationViewBehavior;
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
            @Override
            public void ifCancelledAndCanRequest(Activity activity) {
                permission.checkAndRequestPermissions(activity);
                super.ifCancelledAndCanRequest(activity);
            }
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

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottom_nav.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationViewBehavior());

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            Toast.makeText(this, "change orientation", Toast.LENGTH_LONG).show();
            Log.d("orientation-changing", "landscape");
            if(view_pager.getCurrentItem() == 0){
                var myFragment = (MediaFragment)this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                assert myFragment != null;
                myFragment.changeOrientation(6);
            }

        }else {
            Toast.makeText(this, "change orientation", Toast.LENGTH_LONG).show();
            Log.d("orientation-changing", "portrait");
            if(view_pager.getCurrentItem() == 0){
                var myFragment = (MediaFragment)this.getSupportFragmentManager().findFragmentByTag("f" + view_pager.getCurrentItem());
                assert myFragment != null;
                myFragment.changeOrientation(3);
            }
        }
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
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED || grantResults[2] == PackageManager.PERMISSION_DENIED) {
                Log.d("permission", "permission denied - requesting it");
                // String[] permissions2 = {android.Manifest.permission.READ_MEDIA_AUDIO, android.Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO};
                // requestPermissions(permissions2, 1);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                permission.checkResult(requestCode, permissions, grantResults);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            grantResults[0] = grantResults[1] = grantResults[2] = PackageManager.PERMISSION_GRANTED;
            permission.checkResult(requestCode, permissions, grantResults);
        }
    }
}

