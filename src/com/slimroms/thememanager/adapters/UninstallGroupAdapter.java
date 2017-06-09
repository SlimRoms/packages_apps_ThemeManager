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
package com.slimroms.thememanager.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayFlavor;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.OverlayThemeInfo;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.helpers.PackageIconLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class UninstallGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SlimTM-OverlayGroupAdapter";

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checked;
        TextView overlayName;
        TextView overlayTargetPackage;
        ImageView overlayImage;
        LinearLayout overlayFlavors;
        ViewGroup clickContainer;
        TextView overlayTheme;
        TextView overlayUpdate;

        ViewHolder(View itemView) {
            super(itemView);
            checked = (CheckBox) itemView.findViewById(R.id.checkbox);
            overlayName = (TextView) itemView.findViewById(R.id.overlay_name);
            overlayTargetPackage = (TextView) itemView.findViewById(R.id.overlay_package);
            overlayImage = (ImageView) itemView.findViewById(R.id.overlay_image);
            overlayFlavors = (LinearLayout) itemView.findViewById(R.id.spinner_layout);
            clickContainer = (ViewGroup) itemView.findViewById(R.id.click_container);
            overlayTheme = (TextView) itemView.findViewById(R.id.overlay_theme);
            overlayUpdate = (TextView) itemView.findViewById(R.id.overlay_update);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

    private class Item {
        boolean isHeader = false;
        Overlay overlay;
        String title;
    }

    private LayoutInflater mInflater;
    private Context mContext;
    private final ColorStateList mDefaultTextColors;
    private final int mEnabledTextColor;
    private final int mDisabledTextColor;
    private final int mSpinnerPadding;

    private ArrayList<Item> mItems = new ArrayList<>();

    private HashMap<String, String> mThemeNames = new HashMap<>();

    public UninstallGroupAdapter(Context context, OverlayThemeInfo info) {
        mInflater = LayoutInflater.from(context);
        mContext = context;

        for (String key : info.groups.keySet()) {
            Item header = new Item();
            header.isHeader = true;
            switch (key) {
                case OverlayGroup.OVERLAYS:
                    header.title = context.getString(R.string.group_title_overlays);
                    break;
                case OverlayGroup.FONTS:
                    header.title = context.getString(R.string.group_title_fonts);
                    break;
                case OverlayGroup.BOOTANIMATIONS:
                    header.title = context.getString(R.string.group_title_bootanimations);
                    break;
                case OverlayGroup.WALLPAPERS:
                    header.title = context.getString(R.string.group_title_wallpapers);
                    break;
                default:
                    header.title = key;
                    break;
            }
            mItems.add(header);
            for (Overlay overlay : info.groups.get(key).overlays) {
                Item overl = new Item();
                overl.overlay = overlay;
                mItems.add(overl);
            }
        }

        mEnabledTextColor = ContextCompat.getColor(context, R.color.overlay_enabled);
        mDisabledTextColor = ContextCompat.getColor(context, R.color.overlay_disabled);
        final TextView dummyTextView = new TextView(context);
        mDefaultTextColors = dummyTextView.getTextColors();
        mSpinnerPadding = context.getResources().getDimensionPixelSize(R.dimen.margin_small);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(mInflater.inflate(R.layout.item_header, parent, false));
        } else {
            return new ViewHolder(mInflater.inflate(R.layout.item_overlay, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position).isHeader) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.title.setText(mItems.get(position).title);
            return;
        }
        final ViewHolder viewHolder = (ViewHolder) holder;
        final Overlay overlay = mItems.get(position).overlay;
        viewHolder.overlayName.setText(overlay.overlayName);

        // installed overlays
        viewHolder.overlayTargetPackage.setText(overlay.overlayPackage);
        viewHolder.overlayTheme.setText((!TextUtils.isEmpty(overlay.overlayVersion))
                ? getAppName(overlay.themePackage)
                + " (" + overlay.overlayVersion + ")"
                : getAppName(overlay.themePackage));
        viewHolder.overlayTheme.setVisibility(View.VISIBLE);

        viewHolder.overlayFlavors.setVisibility(View.GONE);

        if (overlay.isOverlayInstalled) {
            viewHolder.overlayName.setTextColor(overlay.isOverlayEnabled ? mEnabledTextColor : mDisabledTextColor);
            viewHolder.overlayName.setEnabled(true);
            viewHolder.overlayTargetPackage.setEnabled(true);
            viewHolder.overlayTheme.setEnabled(true);
            viewHolder.overlayUpdate.setVisibility(View.GONE);
        } else {
            viewHolder.overlayName.setTextColor(mDefaultTextColors);
            viewHolder.overlayName.setEnabled(overlay.isTargetPackageInstalled);
            viewHolder.overlayTargetPackage.setEnabled(overlay.isTargetPackageInstalled);
            viewHolder.overlayTheme.setEnabled(overlay.isTargetPackageInstalled);
            viewHolder.overlayUpdate.setVisibility(View.GONE);
        }

        if (overlay.overlayImage != null) {
            viewHolder.overlayImage.setImageBitmap(overlay.overlayImage);
        } else {
            // load target package icon
            PackageIconLoader.load(mContext, viewHolder.overlayImage, overlay.targetPackage);
        }
        viewHolder.checked.setChecked(overlay.checked);
        viewHolder.checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay.checked = ((CheckBox) v).isChecked();
            }
        });
        viewHolder.clickContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean newValue = !overlay.checked;
                viewHolder.checked.setChecked(newValue);
                viewHolder.checked.callOnClick();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
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
