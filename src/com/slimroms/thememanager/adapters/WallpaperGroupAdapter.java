package com.slimroms.thememanager.adapters;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import java.io.IOException;

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
            Glide.with(mContext).load(overlay.tag).into(holder.overlayImage);
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
                                    final ProgressDialog progress = new ProgressDialog(mContext);
                                    progress.setIndeterminate(true);
                                    progress.setCancelable(false);
                                    progress.setMessage(mContext.getString(R.string.applying));
                                    progress.show();
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                final WallpaperManager wpmgr = WallpaperManager.getInstance(mContext);
                                                final Drawable wallpaper = holder.overlayImage.getDrawable();
                                                Bitmap bitmap = null;
                                                if (wallpaper instanceof GlideBitmapDrawable) {
                                                    bitmap = ((GlideBitmapDrawable) wallpaper).getBitmap();
                                                } else if (wallpaper instanceof BitmapDrawable) {
                                                    bitmap = ((BitmapDrawable) wallpaper).getBitmap();
                                                }

                                                if (bitmap != null) {
                                                    final int width = wpmgr.getDesiredMinimumWidth();
                                                    final int height = wpmgr.getDesiredMinimumHeight();
                                                    final Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                                                            width, height, false);
                                                    wpmgr.setBitmap(scaledBitmap);
                                                }
                                            }
                                            catch (IOException ex) {
                                                ex.printStackTrace();
                                            }
                                            finally {
                                                progress.dismiss();
                                            }
                                        }
                                    }).start();
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
