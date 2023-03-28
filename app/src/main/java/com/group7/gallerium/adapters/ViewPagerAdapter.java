package com.group7.gallerium.adapters;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.group7.gallerium.fragments.AlbumFragment;
import com.group7.gallerium.fragments.FavoriteFragment;
import com.group7.gallerium.fragments.MediaFragment;
import com.group7.gallerium.fragments.SecureFragment;
import com.group7.gallerium.models.Media;

import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter  {
    private List<Media> data;
    private Context context;

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new MediaFragment();
            case 1 -> new AlbumFragment();
            case 2 -> new SecureFragment();
            case 3 -> new FavoriteFragment();
            default -> null;
        };
    }

    @Override
    public int getItemCount() {
        return 4;
    }

}
