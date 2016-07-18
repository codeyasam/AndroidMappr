package org.mappr;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by codeyasam on 7/19/16.
 */
public class ViewPageAdapter extends FragmentStatePagerAdapter {

    public ViewPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return new CategoryFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }
}
