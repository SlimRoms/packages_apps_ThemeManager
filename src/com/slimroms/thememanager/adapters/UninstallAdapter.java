package com.slimroms.thememanager.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.Theme;

import com.slimroms.thememanager.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gmillz on 2/15/17.
 */

public class UninstallAdapter extends RecyclerView.Adapter<UninstallAdapter.ViewHolder> {

    private LayoutInflater mInflater;

    private List<Object> mInstalledOverlays = new ArrayList<>();

    private List<Overlay> mSelected = new ArrayList<>();

    private static final int ITEM_TYPE_THEME = 0;
    private static final int ITEM_TYPE_OVERLAY = 1;

    private class InstalledOverlayInfo {
        String name;
        String packageName;
        String targetPackage;
        String themeName;
        Bitmap image;

        boolean checked;

        private InstalledOverlayInfo(String name, String themeName, String packageName, String targetPackage, Bitmap bitmap) {
            this.name = name;
            this.themeName = themeName;
            this.packageName = packageName;
            this.targetPackage = targetPackage;
            this.image = bitmap;
        }
    }

    public UninstallAdapter(Context context, List<Object> installed) {
        mInflater = LayoutInflater.from(context);
        if (installed != null) {
            mInstalledOverlays.addAll(installed);
        } else {
            Log.d("TEST", "installed is null");
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ITEM_TYPE_OVERLAY:
                view = mInflater.inflate(R.layout.item_overlay, parent, false);
                return new ViewHolder(view, viewType);
            case ITEM_TYPE_THEME:
                view = mInflater.inflate(R.layout.item_about_category, parent, false);
                return new ViewHolder(view, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Object object = mInstalledOverlays.get(position);
        if (object instanceof Overlay) {
            final Overlay overlay = (Overlay) object;
            holder.overlayName.setText(overlay.overlayName);
            holder.themeName.setText(overlay.targetPackage);
            holder.overlayImage.setImageBitmap(overlay.overlayImage);
            holder.checked.setChecked(mSelected.contains(overlay));
            holder.checked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelected.contains(overlay)) {
                        mSelected.remove(overlay);
                    } else {
                        mSelected.add(overlay);
                    }
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean newValue = !overlay.checked;
                    holder.checked.setChecked(newValue);
                    holder.checked.callOnClick();
                }
            });
        } else if (object instanceof Theme) {
            holder.name.setText(((Theme) object).name);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object object = mInstalledOverlays.get(position);
        if (object instanceof Theme) {
            return ITEM_TYPE_THEME;
        } else {
            return ITEM_TYPE_OVERLAY;
        }
    }

    @Override
    public int getItemCount() {
        return mInstalledOverlays.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox checked;
        TextView overlayName;
        TextView themeName;
        ImageView overlayImage;

        TextView name;

        ViewHolder(View v, int viewType) {
            super(v);
            if (viewType == ITEM_TYPE_OVERLAY) {
                checked = (CheckBox) v.findViewById(R.id.checkbox);
                overlayName = (TextView) v.findViewById(R.id.overlay_name);
                themeName = (TextView) v.findViewById(R.id.overlay_package);
                overlayImage = (ImageView) v.findViewById(R.id.overlay_image);
            } else {
                name = (TextView) v.findViewById(R.id.lbl_name);
            }
        }
    }

    public List<Overlay> getSelected() {
        return mSelected;
    }
}
