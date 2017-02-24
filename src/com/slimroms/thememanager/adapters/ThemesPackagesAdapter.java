package com.slimroms.thememanager.adapters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.slimroms.themecore.Broadcast;
import com.slimroms.themecore.Theme;
import com.slimroms.thememanager.App;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.ThemeContentActivity;
import com.slimroms.thememanager.helpers.PackageIconLoader;

import java.util.ArrayList;
import java.util.List;

public class ThemesPackagesAdapter extends RecyclerView.Adapter<ThemesPackagesAdapter.ViewHolder> {

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

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final List<Theme> mItems;

    public ThemesPackagesAdapter(Context context) {
        mContext = context;
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
                final Theme theme = mItems.get(adapterPosition);
                final Intent intent = new Intent(App.getInstance().getApplicationContext(),
                        ThemeContentActivity.class);
                intent.putExtra(Broadcast.EXTRA_THEME_PACKAGE, theme.packageName);
                intent.putExtra(Broadcast.EXTRA_BACKEND_NAME, theme.backendName);
                ActivityCompat.startActivity(App.getInstance().getApplicationContext(), intent, null);
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

    public void addThemes(List<Theme> themes) {
        synchronized (mItems) {
            mItems.addAll(themes);
            notifyDataSetChanged();
        }
    }

    public void removeThemes(@NonNull ComponentName backend) {
        final List<Theme> themesToDelete = new ArrayList<>();
        synchronized (mItems) {
            for (Theme t : mItems) {
                if (t.backendName.equals(backend))
                    themesToDelete.add(t);
            }
            mItems.removeAll(themesToDelete);
            notifyDataSetChanged();
        }
    }
}
