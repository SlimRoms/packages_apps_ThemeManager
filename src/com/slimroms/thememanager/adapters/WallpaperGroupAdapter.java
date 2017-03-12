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
                                                mContext.getApplicationContext().getFilesDir(),
                                                UUID.randomUUID().toString() + ".jpg");
                                        try {
                                            final FileOutputStream fos = new FileOutputStream(bmpFile);
                                            try {
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                            }
                                            finally {
                                                fos.close();
                                            }
                                            Uri uri = FileProvider.getUriForFile(
                                                    mContext.getApplicationContext(),
                                                    mContext.getPackageName(), bmpFile);
                                            final Intent intent = wpmgr.getCropAndSetWallpaperIntent(uri);
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            ActivityCompat.startActivity(mContext, intent, null);
                                        }
                                        catch (IOException ex) {
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
