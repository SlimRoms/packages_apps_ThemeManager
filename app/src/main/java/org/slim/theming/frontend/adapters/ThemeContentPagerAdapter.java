package org.slim.theming.frontend.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.slimroms.themecore.OverlayThemeInfo;
import org.slim.theming.frontend.fragments.OverlayGroupFragment;

public class ThemeContentPagerAdapter extends FragmentPagerAdapter {
    private OverlayThemeInfo mOverlayInfo;

    public ThemeContentPagerAdapter(FragmentManager fm, OverlayThemeInfo overlayInfo) {
        super(fm);
        mOverlayInfo = overlayInfo;
    }

    @Override
    public Fragment getItem(int position) {
        // page with overlays
        return OverlayGroupFragment.newInstance(mOverlayInfo.groups.get(position));
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
