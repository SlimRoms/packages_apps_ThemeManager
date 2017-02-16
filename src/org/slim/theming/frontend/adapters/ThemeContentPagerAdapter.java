package org.slim.theming.frontend.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.ArrayMap;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.themecore.Theme;

import org.slim.theming.frontend.R;
import org.slim.theming.frontend.fragments.BootAnimationFragment;
import org.slim.theming.frontend.fragments.OverlayGroupFragment;
import org.slim.theming.frontend.fragments.WallpaperGroupFragment;

import java.util.ArrayList;
import java.util.Collections;

public class ThemeContentPagerAdapter extends FragmentPagerAdapter {
    private OverlayThemeInfo mOverlayInfo;
    private Theme mTheme;

    private ArrayList<String> mKeys = new ArrayList<>();
    private ArrayMap<String, String> mTitles = new ArrayMap<>();

    public ThemeContentPagerAdapter(FragmentManager fm,
                                    OverlayThemeInfo overlayInfo, Theme theme, Context context) {
        super(fm);
        mOverlayInfo = overlayInfo;
        mTheme = theme;

        mKeys.addAll(mOverlayInfo.groups.keySet());
        Collections.sort(mKeys);
        if (mKeys.contains(OverlayGroup.OVERLAYS)) {
            Collections.swap(mKeys, mKeys.indexOf(OverlayGroup.OVERLAYS), 0);
        }

        for (String key : mKeys) {
            String title;
            switch (key) {
                case OverlayGroup.OVERLAYS:
                    title = context.getString(R.string.group_title_overlays);
                    break;
                case OverlayGroup.FONTS:
                    title = context.getString(R.string.group_title_fonts);
                    break;
                case OverlayGroup.BOOTANIMATIONS:
                    title = context.getString(R.string.group_title_bootanimations);
                    break;
                case OverlayGroup.WALLPAPERS:
                    title = context.getString(R.string.group_title_wallpapers);
                    break;
                default:
                    title = key;
                    break;
            }
            mTitles.put(key, title);
        }


    }

    @Override
    public Fragment getItem(int position) {
        final String key = mKeys.get(position);
        final OverlayGroup group = mOverlayInfo.groups.get(key);
        switch (key) {
            case OverlayGroup.WALLPAPERS:
                //page with wallpapers
                return WallpaperGroupFragment.newInstance(group);
            case OverlayGroup.BOOTANIMATIONS:
                // page with boot animations
                return BootAnimationFragment.newInstance(group, mTheme.packageName);
            default:
                // page with overlays
                return OverlayGroupFragment.newInstance(group);
        }
    }

    @Override
    public int getCount() {
        return mKeys.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(mKeys.get(position));
    }
}
