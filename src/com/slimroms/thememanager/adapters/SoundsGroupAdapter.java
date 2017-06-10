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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayGroup;
import com.slimroms.thememanager.helpers.PackageIconLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.slimroms.thememanager.R;

/**
 * Created by gmillz on 6/9/17.
 */

public class SoundsGroupAdapter extends RecyclerView.Adapter<SoundsGroupAdapter.ViewHolder> {

    private Context mContext;
    private OverlayGroup mGroup;
    private LayoutInflater mInflater;

    public SoundsGroupAdapter(Context context, OverlayGroup group) {
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
                ArrayList<Sound> sounds = new ArrayList<>();
                listSounds(new File(overlay.tag), sounds);
                final ArrayAdapter<Sound> adapter = new ArrayAdapter<Sound>(
                        mContext, R.layout.item_sound, R.id.title, sounds) {
                    @Override
                    public View getView(final int position, @Nullable View convertView,
                                        @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Sound sound = getItem(position);

                                MediaPlayer player = new MediaPlayer();

                                player.setOnCompletionListener(
                                        new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        mp.reset();
                                        mp.release();
                                    }
                                });
                                FileInputStream fis = null;
                                try {
                                    fis = new FileInputStream(sound.path);
                                    player.setDataSource(fis.getFD());
                                    player.setAudioStreamType(AudioManager.STREAM_SYSTEM);
                                    player.prepare();
                                    player.start();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (fis != null) {
                                        try {
                                            fis.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });
                        return v;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setAdapter(adapter, null);
                builder.setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        overlay.checked = true;
                        dialog.dismiss();
                        notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        overlay.checked = false;
                        dialog.dismiss();
                        notifyDataSetChanged();
                    }
                });
                builder.show();
            }
        });
        holder.check.setVisibility(overlay.checked ? View.VISIBLE : View.GONE);
    }

    private class Sound {
        String name;
        String path;

        Sound(String name, String path) {
            this.name = name;
            this.path = path;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private void listSounds(File folder, ArrayList<Sound> sounds) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                listSounds(file, sounds);
            } else {
                sounds.add(new Sound(file.getName().substring(0, file.getName().lastIndexOf(".")),
                        file.getAbsolutePath()));
            }
        }
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
}
