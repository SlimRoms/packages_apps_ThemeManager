package com.slimroms.thememanager.fragments;

import android.support.v7.widget.RecyclerView;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.adapters.BootAnimationGroupAdapter;

/**
 * Created by gmillz on 2/9/17.
 */

public class BootAnimationGroupFragment extends AbstractGroupFragment {

    private String mThemePackage;

    public static BootAnimationGroupFragment newInstance(OverlayGroup group, String themePackage) {
        final BootAnimationGroupFragment fragment = new BootAnimationGroupFragment();
        fragment.mOverlayGroup = group;
        fragment.mThemePackage = themePackage;
        return fragment;
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return new BootAnimationGroupAdapter(getContext(), mOverlayGroup, mThemePackage);
    }
}
