package com.slimroms.thememanager.fragments;

import android.support.v7.widget.RecyclerView;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.adapters.WallpaperGroupAdapter;

public class WallpaperGroupFragment extends AbstractGroupFragment {

    public static WallpaperGroupFragment newInstance(OverlayGroup group) {
        final WallpaperGroupFragment fragment = new WallpaperGroupFragment();
        fragment.mOverlayGroup = group;
        return fragment;
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return new WallpaperGroupAdapter(getContext(), mOverlayGroup);
    }
}
