package com.slimroms.thememanager.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
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
        holder.setPath(overlay.tag);
    }

    @Override
    public int getItemCount() {
        return mGroup.overlays.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        ViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.overlay_image);
            name = (TextView) view.findViewById(R.id.overlay_name);
        }

        void setPath(final String path) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new PreviewBootanimation().execute(path);
                }
            });
        }
    }

    private class PreviewBootanimation extends AsyncTask<String, Void, ZipFile> {
        private ProgressDialog mProgress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgress = new ProgressDialog(mContext);
            mProgress.setIndeterminate(true);
            mProgress.setMessage(mContext.getString(R.string.loading_short));
            mProgress.show();
        }

        @Override
        protected ZipFile doInBackground(String... path) {
            try {
                return new ZipFile(new File(path[0]));
            } catch (IOException e) {
                Log.w(TAG, "Unable to load boot animation: " + path[0], e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ZipFile file) {
            BootAnimationImageView animationView = new BootAnimationImageView(mContext);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                    .setView(animationView)
                    .setTitle(mContext.getString(R.string.preview));
            mProgress.dismiss();
            builder.setNegativeButton(android.R.string.cancel, null);
            if (file != null) {
                builder.setPositiveButton(mContext.getString(R.string.apply), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mGroup.selectedStyle = file.getName();
                        dialogInterface.dismiss();
                    }
                });
                animationView.setBootAnimation(file);
                animationView.start();
            }
            builder.show();
        }
    }
}
