package org.slim.theming.frontend.fragments;

import android.support.v7.widget.RecyclerView;

import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.Theme;

import org.slim.theming.frontend.adapters.BootAnimationAdapter;

/**
 * Created by gmillz on 2/9/17.
 */

public class BootAnimationFragment extends AbstractGroupFragment {

    private Theme mTheme;

    public static BootAnimationFragment newInstance(OverlayGroup group, Theme theme) {
        BootAnimationFragment fragment = new BootAnimationFragment();
        fragment.mOverlayGroup = group;
        fragment.mTheme = theme;
        return fragment;
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return new BootAnimationAdapter(getContext(), mOverlayGroup, mTheme);
    }
}
