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
import org.apache.commons.io.FileUtils;

import com.slimroms.thememanager.App;
import com.slimroms.thememanager.R;
import com.slimroms.thememanager.views.BootAnimationImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class BootAnimationAdapter extends RecyclerView.Adapter<BootAnimationAdapter.ViewHolder> {

    private static final String TAG = BootAnimationAdapter.class.getSimpleName();

    private static final String CACHED_SUFFIX = "_bootanimation.zip";

    private Context mContext;
    private OverlayGroup mGroup;
    private String mThemePackage;
    private LayoutInflater mInflater;

    public BootAnimationAdapter(Context context, OverlayGroup group, String themePackage) {
        mContext = context;
        mGroup = group;
        mThemePackage = themePackage;

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
        holder.icon.setImageBitmap(overlay.overlayImage);
        holder.setPackage(overlay.targetPackage);
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

        public void setPackage(final String bootanimation) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new PreviewBootanimation().execute(bootanimation);
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
        protected ZipFile doInBackground(String... boot) {
            ZipFile zip;
            // check if the bootanimation is cached
            File bootanimFile = new File(mContext.getCacheDir(), mThemePackage + CACHED_SUFFIX);
            if (App.isDebug()) {
                Log.d("TEST", "f=" + bootanimFile.getAbsolutePath());
            }
            if (bootanimFile.exists()) {
                bootanimFile.delete();
            }
            if (!bootanimFile.exists()) {
                // go easy on cache storage and clear out any previous boot animations
                clearBootAnimationCache();
                try {
                    Context themeContext = mContext.createPackageContext(mThemePackage, 0);
                    if (App.isDebug()) {
                        Log.d("TEST", "name=" + boot[0]);
                    }
                    InputStream is = themeContext.getAssets().open("bootanimation/" + boot[0]);
                    try {
                        FileUtils.copyInputStreamToFile(is, bootanimFile);
                    }
                    finally {
                        is.close();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Unable to load boot animation", e);
                    return null;
                }
            }
            try {
                zip = new ZipFile(bootanimFile);
            } catch (IOException e) {
                Log.w(TAG, "Unable to load boot animation", e);
                return null;
            }
            return zip;
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

    private void clearBootAnimationCache() {
        File cache = mContext.getCacheDir();
        if (cache.exists()) {
            for(File f : cache.listFiles()) {
                // volley stores stuff in cache so don't delete the volley directory
                if(!f.isDirectory() && f.getName().endsWith(CACHED_SUFFIX)) {
                    if (!f.delete()) {
                        Log.e(TAG, "Can't delete " + f.getAbsolutePath());
                    }
                }
            }
        }
    }
}
