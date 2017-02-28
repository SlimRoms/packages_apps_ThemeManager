package com.slimroms.thememanager.fragments;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.TextView;
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
        final TextView emptyView = (TextView) view.findViewById(R.id.empty_view);
        emptyView.setVisibility((mAdapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
    }

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
