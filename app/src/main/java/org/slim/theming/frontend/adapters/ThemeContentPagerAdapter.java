package org.slim.theming.frontend.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.slimroms.themecore.OverlayThemeInfo;

public class ThemeContentPagerAdapter extends FragmentPagerAdapter {
    private OverlayThemeInfo mOverlayInfo;

    public ThemeContentPagerAdapter(FragmentManager fm, OverlayThemeInfo overlayInfo) {
        super(fm);
        mOverlayInfo = overlayInfo;
    }

    @Override
    public Fragment getItem(int position) {
        return new Fragment();
    }

    @Override
    public int getCount() {
        return mOverlayInfo.groups.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mOverlayInfo.groups.get(position).title;
    }
}
