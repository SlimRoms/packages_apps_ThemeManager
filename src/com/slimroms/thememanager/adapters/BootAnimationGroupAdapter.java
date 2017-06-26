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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.helpers.PackageIconLoader;
import com.slimroms.thememanager.views.BootAnimationImageView;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class BootAnimationGroupAdapter extends RecyclerView.Adapter<BootAnimationGroupAdapter.ViewHolder> {

    private static final String TAG = BootAnimationGroupAdapter.class.getSimpleName();
    private Context mContext;
    private OverlayGroup mGroup;
    private LayoutInflater mInflater;
    public BootAnimationGroupAdapter(Context context, OverlayGroup group) {
        mContext = context;
        mGroup = group;

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_bootanimation, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Overlay overlay = mGroup.overlays.get(position);
        holder.name.setText(overlay.overlayName);
        if (overlay.overlayImage != null) {
            holder.icon.setImageBitmap(overlay.overlayImage);
        } else {
            // load target package icon
            PackageIconLoader.load(mContext, holder.icon, overlay.targetPackage);
        }
        holder.clickContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PreviewBootanimation(overlay).execute();
            }
        });
        holder.check.setVisibility(overlay.checked ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mGroup.overlays.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        ViewGroup clickContainer;
        ImageView check;

        ViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.overlay_image);
            name = (TextView) view.findViewById(R.id.overlay_name);
            clickContainer = (ViewGroup) view.findViewById(R.id.click_container);
            check = (ImageView) view.findViewById(R.id.image_check);
        }
    }

    private class PreviewBootanimation extends AsyncTask<Void, Void, ZipFile> {
        private ProgressDialog mProgress;
        private Overlay mOverlay;

        PreviewBootanimation(@NonNull Overlay overlay) {
            super();
            mOverlay = overlay;
        }

        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(mContext);
            mProgress.setIndeterminate(true);
            mProgress.setMessage(mContext.getString(R.string.loading_short));
            mProgress.show();
        }

        @Override
        protected ZipFile doInBackground(Void... voids) {
            try {
                return new ZipFile(new File(mOverlay.tag));
            } catch (IOException e) {
                Log.w(TAG, "Unable to load boot animation: " + mOverlay.tag, e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ZipFile file) {
            mProgress.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                    .setTitle(mContext.getString(R.string.preview));
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setPositiveButton(mContext.getString(R.string.select), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mGroup.clearSelected();
                    mOverlay.checked = true;
                    notifyDataSetChanged();
                }
            });
            if (file != null) {
                BootAnimationImageView animationView = new BootAnimationImageView(mContext);
                builder.setView(animationView);
                animationView.setBootAnimation(file);
                animationView.start();
            }
            builder.show();
        }
    }
}
