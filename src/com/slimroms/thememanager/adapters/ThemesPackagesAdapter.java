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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.slimroms.themecore.Theme;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.helpers.PackageIconLoader;

import java.util.ArrayList;
import java.util.List;

public class ThemesPackagesAdapter extends RecyclerView.Adapter<ThemesPackagesAdapter.ViewHolder> {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final List<Theme> mItems;
    private ThemeClickListener mClickListener;

    public ThemesPackagesAdapter(Context context, ThemeClickListener clickListener) {
        mContext = context;
        mClickListener = clickListener;
        mInflater = LayoutInflater.from(context);
        mItems = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.item_theme, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        final Theme theme = mItems.get(adapterPosition);
        holder.themeName.setText(theme.name);
        holder.themeDeveloper.setText(theme.themeAuthor);
        holder.themeVersion.setText(theme.themeVersion);
        holder.themeType.setText(theme.themeType);
        holder.clickContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onThemeClick(theme);
            }
        });
        if (theme.themeLogo != null) {
            holder.logo.setImageBitmap(theme.themeLogo);
        } else {
            // load target package icon
            PackageIconLoader.load(mContext, holder.logo, theme.packageName);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setData(List<Theme> themes) {
        synchronized (mItems) {
            mItems.clear();
            mItems.addAll(themes);
            notifyDataSetChanged();
        }
    }

    public interface ThemeClickListener {
        void onThemeClick(Theme theme);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView themeName;
        TextView themeDeveloper;
        ImageView logo;
        ViewGroup clickContainer;
        TextView themeVersion;
        TextView themeType;

        ViewHolder(View itemView) {
            super(itemView);
            themeName = (TextView) itemView.findViewById(R.id.lbl_theme_name);
            themeDeveloper = (TextView) itemView.findViewById(R.id.lbl_theme_author);
            themeVersion = (TextView) itemView.findViewById(R.id.lbl_theme_version);
            clickContainer = (ViewGroup) itemView.findViewById(R.id.click_container);
            logo = (ImageView) itemView.findViewById(R.id.img_logo);
            themeType = (TextView) itemView.findViewById(R.id.lbl_theme_type);
        }
    }
}
