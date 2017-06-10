package com.slimroms.thememanager.fragments;

import android.support.v7.widget.RecyclerView;

import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.adapters.SoundsGroupAdapter;

/**
 * Created by gmillz on 6/9/17.
 */

public class SoundsGroupFragment extends AbstractGroupFragment {

    public static SoundsGroupFragment newInstance(OverlayGroup group) {
        SoundsGroupFragment fragment = new SoundsGroupFragment();
        fragment.mOverlayGroup = group;
        return fragment;
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return new SoundsGroupAdapter(getActivity(), mOverlayGroup);
    }
}
