package com.slimroms.thememanager.fragments;

import android.support.v7.widget.RecyclerView;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.adapters.BootAnimationAdapter;

/**
 * Created by gmillz on 2/9/17.
 */

public class BootAnimationFragment extends AbstractGroupFragment {

    private String mThemePackage;

    public static BootAnimationFragment newInstance(OverlayGroup group, String themePackage) {
        BootAnimationFragment fragment = new BootAnimationFragment();
        fragment.mOverlayGroup = group;
        fragment.mThemePackage = themePackage;
        return fragment;
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return new BootAnimationAdapter(getContext(), mOverlayGroup, mThemePackage);
    }
}
