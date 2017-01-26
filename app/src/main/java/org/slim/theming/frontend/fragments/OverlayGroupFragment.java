package org.slim.theming.frontend.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.slimroms.themecore.OverlayGroup;
import org.slim.theming.frontend.R;
import org.slim.theming.frontend.adapters.OverlayGroupAdapter;
import org.slim.theming.frontend.views.LineDividerItemDecoration;

public class OverlayGroupFragment extends Fragment {
    private OverlayGroup mOverlayGroup;

    public static OverlayGroupFragment newInstance(OverlayGroup group) {
        final OverlayGroupFragment fragment = new OverlayGroupFragment();
        fragment.mOverlayGroup = group;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView recycler = (RecyclerView) view.findViewById(R.id.list);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.addItemDecoration(new LineDividerItemDecoration(getContext()));
        recycler.setItemAnimator(new DefaultItemAnimator());
        final OverlayGroupAdapter adapter = new OverlayGroupAdapter(getContext(), mOverlayGroup);
        recycler.setAdapter(adapter);
    }
}
