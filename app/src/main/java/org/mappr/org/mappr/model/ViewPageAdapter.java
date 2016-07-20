package org.mappr.org.mappr.model;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.mappr.org.mappr.model.CategoryFragment;

/**
 * Created by codeyasam on 7/19/16.
 */
public class ViewPageAdapter extends FragmentStatePagerAdapter {

    public ViewPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new CategoryFragment();
            case 1: return new FavoritesFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
