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
import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.adapters.OverlayGroupAdapter;
import com.slimroms.thememanager.helpers.MenuTint;

import java.util.ArrayList;

public class OverlayGroupFragment extends AbstractGroupFragment {

    public static OverlayGroupFragment newInstance(OverlayGroup group) {
        final OverlayGroupFragment fragment = new OverlayGroupFragment();
        fragment.mOverlayGroup = group;
        return fragment;
    }

    @Override
    public RecyclerView.Adapter getAdapter() {
        return new OverlayGroupAdapter(getContext(), mOverlayGroup);
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

        if (mOverlayGroup != null && mOverlayGroup.styles.size() > 0) {
            final Spinner stylesSpinner = (Spinner) view.findViewById(R.id.spinner);
            ArrayList<String> array = new ArrayList<>();
            array.addAll(mOverlayGroup.styles.values());
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.item_flavor, array);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            stylesSpinner.setAdapter(arrayAdapter);
            stylesSpinner.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mOverlayGroup.selectedStyle)) {
                stylesSpinner.setSelection(array.indexOf(mOverlayGroup.styles.get(mOverlayGroup.selectedStyle)));
            }
            stylesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    mOverlayGroup.selectedStyle =
                            mOverlayGroup.styles.keySet().toArray(new String[0])[i];
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
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
