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
import android.text.TextUtils;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.adapters.OverlayGroupAdapter;
import com.slimroms.thememanager.adapters.UninstallGroupAdapter;
import com.slimroms.thememanager.helpers.MenuTint;

import java.util.ArrayList;

public class UninstallFragment extends AbstractGroupFragment {

    private OverlayThemeInfo mOverlayInfo;

    public static UninstallFragment newInstance(OverlayThemeInfo info) {
        final UninstallFragment fragment = new UninstallFragment();
        fragment.mOverlayInfo = info;
        return fragment;
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return new UninstallGroupAdapter(getContext(), mOverlayInfo);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overlay_group, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView emptyViewTitle = (TextView) view.findViewById(R.id.empty_view_title);
        emptyViewTitle.setText(R.string.no_installed_overlays_title);
        final TextView emptyViewDescription = (TextView) view.findViewById(R.id.empty_view_description);
        emptyViewDescription.setText(R.string.no_installed_overlays_description);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overlay_group, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuTint.tintMenu(menu, ContextCompat.getColor(getContext(), android.R.color.white));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_all:
                Boolean newValue = null;
                for (OverlayGroup group : mOverlayInfo.groups.values()) {
                    for (Overlay overlay : group.overlays) {
                        if (newValue == null)
                            newValue = !overlay.checked;
                        overlay.checked = newValue;
                    }
                }
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

