package org.slim.theming.frontend.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayGroup;
import org.slim.theming.frontend.R;
import org.slim.theming.frontend.adapters.OverlayGroupAdapter;
import org.slim.theming.frontend.helpers.MenuTintHelper;
import org.slim.theming.frontend.views.LineDividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;

public class OverlayGroupFragment extends Fragment {
    private OverlayGroup mOverlayGroup;
    private OverlayGroupAdapter mAdapter;

    public static OverlayGroupFragment newInstance(OverlayGroup group) {
        final OverlayGroupFragment fragment = new OverlayGroupFragment();
        fragment.mOverlayGroup = group;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        if (mOverlayGroup.styles.size() > 0) {
            String def = mOverlayGroup.styles.get("type3");
            if (def != null) {
                mOverlayGroup.styles.remove("type3");
            }
            ArrayList<String> array = new ArrayList<>();
            array.addAll(mOverlayGroup.styles.values());
            Collections.sort(array);
            array.add(0, def);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.item_flavor, array);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arrayAdapter);
            spinner.setVisibility(View.VISIBLE);
        }


        final RecyclerView recycler = (RecyclerView) view.findViewById(R.id.list);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.addItemDecoration(new LineDividerItemDecoration(getContext()));
        recycler.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new OverlayGroupAdapter(getContext(), mOverlayGroup);
        recycler.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overlay_group, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuTintHelper.tintMenu(menu, ContextCompat.getColor(getContext(), android.R.color.white));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_all:
                Boolean newValue = null;
                for (Overlay overlay : mOverlayGroup.overlays) {
                    if (newValue == null)
                        newValue = !overlay.checked;
                    overlay.checked = newValue;
                }
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
