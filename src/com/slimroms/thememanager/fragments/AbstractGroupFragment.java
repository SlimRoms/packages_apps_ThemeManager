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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import com.slimroms.themecore.Broadcast;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.views.LineDividerItemDecoration;

public abstract class AbstractGroupFragment extends Fragment {
    protected OverlayGroup mOverlayGroup;
    protected RecyclerView.Adapter mAdapter;
    private WindowManager mWindowManager;

    public abstract RecyclerView.Adapter getAdapter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("group")) {
            mOverlayGroup = savedInstanceState.getParcelable("group");
        }
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable("group", mOverlayGroup);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView recycler = (RecyclerView) view.findViewById(R.id.list);
        recycler.setLayoutManager(new ExpandedLinearLayoutManager(getContext(), mWindowManager));
        recycler.addItemDecoration(new LineDividerItemDecoration(getContext()));
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setHasFixedSize(true);
        mAdapter = getAdapter();
        recycler.setAdapter(mAdapter);
        final ViewGroup emptyView = (ViewGroup) view.findViewById(R.id.empty_view);
        emptyView.setVisibility((mAdapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mRedrawReceiver,
                Broadcast.getRedrawFilter());
    }

    @Override
    public void onDetach() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mRedrawReceiver);
        super.onDetach();
    }

    private BroadcastReceiver mRedrawReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private class ExpandedLinearLayoutManager extends LinearLayoutManager {
        private WindowManager mWindowManager;

        ExpandedLinearLayoutManager(Context context, WindowManager manager) {
            super(context);
            this.mWindowManager = manager;
        }

        @Override
        protected int getExtraLayoutSpace(RecyclerView.State state) {
            // return screen height for smooth scrolling
            final Display display = mWindowManager.getDefaultDisplay();
            final Point size = new Point();
            display.getSize(size);
            return size.y;
        }
    }
}
