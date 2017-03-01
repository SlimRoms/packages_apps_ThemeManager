package com.slimroms.thememanager.fragments;

import android.support.v7.widget.RecyclerView;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.adapters.FontGroupAdapter;

public class FontGroupFragment extends AbstractGroupFragment {

    public static FontGroupFragment newInstance(OverlayGroup group) {
        final FontGroupFragment fragment = new FontGroupFragment();
        fragment.mOverlayGroup = group;
        return fragment;
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return new FontGroupAdapter(getContext(), mOverlayGroup);
    }
}
