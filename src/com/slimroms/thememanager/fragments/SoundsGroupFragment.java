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
