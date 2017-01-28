package org.slim.theming.frontend.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.OverlayThemeInfo;
import org.slim.theming.frontend.fragments.OverlayGroupFragment;

import java.util.ArrayList;
import java.util.Collections;

public class ThemeContentPagerAdapter extends FragmentPagerAdapter {
    private OverlayThemeInfo mOverlayInfo;

    private ArrayList<String> mKeys = new ArrayList<>();

    public ThemeContentPagerAdapter(FragmentManager fm, OverlayThemeInfo overlayInfo) {
        super(fm);
        mOverlayInfo = overlayInfo;

        mKeys.addAll(mOverlayInfo.groups.keySet());
        Collections.sort(mKeys);
        if (mKeys.contains(OverlayGroup.OVERLAYS)) {
            Collections.swap(mKeys, mKeys.indexOf(OverlayGroup.OVERLAYS), 0);
        }
    }

    @Override
    public Fragment getItem(int position) {
        // page with overlays
        return OverlayGroupFragment.newInstance(mOverlayInfo.groups.get(mKeys.get(position)));
    }

    @Override
    public int getCount() {
        return mKeys.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mOverlayInfo.groups.get(mKeys.get(position)).title;
    }
}
