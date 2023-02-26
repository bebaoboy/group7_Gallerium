package com.group7.gallerium.adapters;


import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.group7.gallerium.fragments.AlbumFragment;
import com.group7.gallerium.fragments.FavoriteFragment;
import com.group7.gallerium.fragments.PhotoFragment;
import com.group7.gallerium.fragments.SecureFragment;
import com.group7.gallerium.models.Media;
import com.group7.gallerium.utilities.AccessMediaFile;

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
        switch (position) {
            case 0:
                return new PhotoFragment(context);
            case 1:
                return new AlbumFragment();
            case 2:
                return new SecureFragment();
            case 3:
                return new FavoriteFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

}
