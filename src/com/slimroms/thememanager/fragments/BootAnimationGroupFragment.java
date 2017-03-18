/*
 * Copyright (C) 2017 SlimRoms Project
 * Copyright (C) 2017 Victor Lapin
 * Copyright (C) 2017 Griffin Millender
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.slimroms.thememanager.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.adapters.BootAnimationGroupAdapter;
import com.slimroms.thememanager.helpers.MenuTint;

/**
 * Created by gmillz on 2/9/17.
 */

public class BootAnimationGroupFragment extends AbstractGroupFragment {

    public static BootAnimationGroupFragment newInstance(OverlayGroup group) {
        final BootAnimationGroupFragment fragment = new BootAnimationGroupFragment();
        fragment.mOverlayGroup = group;
        return fragment;
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return new BootAnimationGroupAdapter(getContext(), mOverlayGroup);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // use font group menu here, they're identical
        inflater.inflate(R.menu.font_group, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuTint.tintMenu(menu, ContextCompat.getColor(getContext(), android.R.color.white));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_none:
                mOverlayGroup.clearSelected();
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
