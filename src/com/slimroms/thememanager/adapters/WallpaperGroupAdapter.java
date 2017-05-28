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

import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class WallpaperGroupAdapter extends RecyclerView.Adapter<WallpaperGroupAdapter.ViewHolder>{

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView overlayName;
        ImageView overlayImage;
        ViewGroup clickContainer;

        ViewHolder(View itemView) {
            super(itemView);
            overlayName = (TextView) itemView.findViewById(R.id.overlay_name);
            overlayImage = (ImageView) itemView.findViewById(R.id.overlay_image);
            clickContainer = (ViewGroup) itemView.findViewById(R.id.click_container);
        }
    }

    private Context mContext;
    private LayoutInflater mInflater;
    private OverlayGroup mOverlayGroup;

    public WallpaperGroupAdapter(Context context, OverlayGroup proxy) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mOverlayGroup = proxy;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.item_wallpaper, parent, false);
        return new WallpaperGroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Overlay overlay = mOverlayGroup.overlays.get(position);
        holder.overlayName.setText(overlay.overlayName);
        if (overlay.overlayImage != null) {
            holder.overlayImage.setImageBitmap(overlay.overlayImage);
        } else if (overlay.tag != null) {
            Glide.with(mContext.getApplicationContext())
                    .load(overlay.tag).into(holder.overlayImage);
        }
        holder.clickContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.overlayImage.getDrawable() != null) {
                    new AlertDialog.Builder(mContext)
                            .setMessage(R.string.apply_wallpaper)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final WallpaperManager wpmgr = WallpaperManager.getInstance(mContext);
                                    final Drawable wallpaper = holder.overlayImage.getDrawable();
                                    Bitmap bitmap = null;
                                    if (wallpaper instanceof GlideBitmapDrawable) {
                                        bitmap = ((GlideBitmapDrawable) wallpaper).getBitmap();
                                    } else if (wallpaper instanceof BitmapDrawable) {
                                        bitmap = ((BitmapDrawable) wallpaper).getBitmap();
                                    }

                                    if (bitmap != null) {
                                        final File bmpFile = new File(
                                                mContext.getApplicationContext().getCacheDir(),
                                                UUID.randomUUID().toString() + ".jpg");
                                        try {
                                            bmpFile.createNewFile();
                                            final FileOutputStream fos = new FileOutputStream(bmpFile);
                                            try {
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                            }
                                            finally {
                                                fos.close();
                                            }
                                            final Uri uri = FileProvider.getUriForFile(
                                                    mContext.getApplicationContext(),
                                                    mContext.getPackageName() + ".fileprovider", bmpFile);
                                            final Intent intent = new Intent();
                                            intent.setClassName("com.slimroms.thememanager",
                                                    "com.android.wallpaperpicker.WallpaperCropActivity");
                                            intent.setData(uri);
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            ActivityCompat.startActivity(mContext, intent, null);
                                        }
                                        catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            })
                            .show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mOverlayGroup.overlays.size();
    }
}
