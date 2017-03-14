/*
 * Copyright (C) 2017 SlimRoms Project
 * Copyright (C) 2017 Victor Lapin
 * Copyright (C) 2017 Griffin Millender
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.slimroms.thememanager.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextUtils;
import android.widget.*;
import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayFlavor;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.helpers.PackageIconLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class OverlayGroupAdapter extends RecyclerView.Adapter<OverlayGroupAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checked;
        TextView overlayName;
        TextView overlayTargetPackage;
        ImageView overlayImage;
        LinearLayout overlayFlavors;
        ViewGroup clickContainer;
        TextView overlayTheme;

        ViewHolder(View itemView) {
            super(itemView);
            checked = (CheckBox) itemView.findViewById(R.id.checkbox);
            overlayName = (TextView) itemView.findViewById(R.id.overlay_name);
            overlayTargetPackage = (TextView) itemView.findViewById(R.id.overlay_package);
            overlayImage = (ImageView) itemView.findViewById(R.id.overlay_image);
            overlayFlavors = (LinearLayout) itemView.findViewById(R.id.spinner_layout);
            clickContainer = (ViewGroup) itemView.findViewById(R.id.click_container);
            overlayTheme = (TextView) itemView.findViewById(R.id.overlay_theme);
        }
    }

    private LayoutInflater mInflater;
    private OverlayGroup mOverlayGroup;
    private Context mContext;
    private final ColorStateList mDefaultTextColors;
    private final int mEnabledTextColor;
    private final int mDisabledTextColor;
    private final int mSpinnerPadding;
    private boolean mIsThemeGroup;

    private HashMap<String, String> mThemeNames = new HashMap<>();

    public OverlayGroupAdapter(Context context, OverlayGroup proxy, boolean isThemeGroup) {
        mInflater = LayoutInflater.from(context);
        mOverlayGroup = proxy;
        mContext = context;
        mIsThemeGroup = isThemeGroup;

        mEnabledTextColor = ContextCompat.getColor(context, R.color.overlay_enabled);
        mDisabledTextColor = ContextCompat.getColor(context, R.color.overlay_disabled);
        final TextView dummyTextView = new TextView(context);
        mDefaultTextColors = dummyTextView.getTextColors();
        mSpinnerPadding = context.getResources().getDimensionPixelSize(R.dimen.margin_small);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.item_overlay, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Overlay overlay = mOverlayGroup.overlays.get(position);
        holder.overlayName.setText(overlay.overlayName);
        holder.overlayTargetPackage.setText(overlay.targetPackage);
        if (!mIsThemeGroup) {
            holder.overlayTheme.setText((!TextUtils.isEmpty(overlay.overlayVersion))
                    ? getAppName(overlay.themePackage)
                    + " (" + overlay.overlayVersion + ")"
                    : getAppName(overlay.themePackage)
            );
            holder.overlayTheme.setVisibility(View.VISIBLE);
        } else {
            holder.overlayTheme.setVisibility(View.GONE);
        }
        if (overlay.flavors.size() > 0) {
            holder.overlayFlavors.removeAllViewsInLayout();
            holder.overlayFlavors.setVisibility(View.VISIBLE);
            for (final OverlayFlavor flavor : overlay.flavors.values()) {
                Spinner spinner = (Spinner) View.inflate(mContext, R.layout.flavor_spinner, null);
                spinner.setTag(flavor);
                spinner.setPadding(0, mSpinnerPadding, 0, mSpinnerPadding);
                final ArrayList<String> array = new ArrayList<>();
                array.addAll(flavor.flavors.values());
                Collections.sort(array);
                array.add(0, flavor.name);
                final ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(mContext, R.layout.item_flavor, array);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        overlay.clearSelectedFlavors();
                        OverlayFlavor flavor1 = (OverlayFlavor) adapterView.getTag();
                        flavor1.selected = array.get(i);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                spinner.setSelection(array.indexOf(flavor.selected), true);
                holder.overlayFlavors.addView(spinner);
            }
        }
        else {
            holder.overlayFlavors.setVisibility(View.GONE);
        }

        if (overlay.isOverlayInstalled) {
            holder.overlayName.setTextColor(overlay.isOverlayEnabled ? mEnabledTextColor : mDisabledTextColor);
        }
        else {
            holder.overlayName.setTextColor(mDefaultTextColors);
            holder.overlayName.setEnabled(overlay.isTargetPackageInstalled);
            holder.overlayTargetPackage.setEnabled(overlay.isTargetPackageInstalled);
            holder.overlayTheme.setEnabled(overlay.isTargetPackageInstalled);
        }

        if (overlay.overlayImage != null) {
            holder.overlayImage.setImageBitmap(overlay.overlayImage);
        } else {
            // load target package icon
            PackageIconLoader.load(mContext, holder.overlayImage, overlay.targetPackage);
        }
        holder.checked.setChecked(overlay.checked);
        holder.checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay.checked = ((CheckBox) v).isChecked();
            }
        });
        holder.clickContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean newValue = !overlay.checked;
                holder.checked.setChecked(newValue);
                holder.checked.callOnClick();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mOverlayGroup == null)
            return 0;
        return mOverlayGroup.overlays.size();
    }

    private String getAppName(String packageName) {
        if (mThemeNames.containsKey(packageName)) {
            return mThemeNames.get(packageName);
        }

        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(packageName, 0);
            String appName = info.loadLabel(mContext.getPackageManager()).toString();
            mThemeNames.put(packageName, appName);
            return appName;
        } catch (Exception e) {
            return packageName;
        }
    }
}
