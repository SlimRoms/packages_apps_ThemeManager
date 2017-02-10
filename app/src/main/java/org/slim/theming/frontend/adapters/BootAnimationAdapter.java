package org.slim.theming.frontend.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.slimroms.themecore.OverlayGroup;
import com.slimroms.themecore.Theme;

import org.apache.commons.io.FileUtils;
import org.slim.theming.frontend.BootAnimPreviewActivity;
import org.slim.theming.frontend.R;
import org.slim.theming.frontend.views.BootAnimationImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class BootAnimationAdapter extends RecyclerView.Adapter<BootAnimationAdapter.ViewHolder> {

    private static final String TAG = BootAnimationAdapter.class.getSimpleName();

    private static final String CACHED_SUFFIX = "_bootanimation.zip";

    private Context mContext;
    private OverlayGroup mGroup;
    private Theme mTheme;
    private LayoutInflater mInflater;

    public BootAnimationAdapter(Context context, OverlayGroup group, Theme theme) {
        mContext = context;
        mGroup = group;
        mTheme = theme;

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_bootanimation, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(mGroup.overlays.get(position).overlayName);
        holder.icon.setImageBitmap(mGroup.overlays.get(position).overlayImage);
        holder.setPackage(mGroup.overlays.get(position).targetPackage);
    }

    @Override
    public int getItemCount() {
        return mGroup.overlays.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        public ViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.iv_themeImage);
            name = (TextView) view.findViewById(R.id.txtName);
        }

        public void setPackage(final String bootanimation) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, BootAnimPreviewActivity.class);
                    intent.putExtra("theme_package", mTheme.packageName);
                    intent.putExtra("bootanimation", bootanimation);
                    intent.putExtra("backend", mTheme.backendName);
                    //mContext.startActivity(intent);

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
            mProgress.setMessage("Loading...");
            mProgress.show();
        }

        @Override
        protected ZipFile doInBackground(String... boot) {
            ZipFile zip;
            // check if the bootanimation is cached
            File f = new File(mContext.getCacheDir(), mTheme.packageName + CACHED_SUFFIX);
            Log.d("TEST", "f=" + f.getAbsolutePath());
            if (f.exists()) {
                f.delete();
            }
            if (!f.exists()) {
                // go easy on cache storage and clear out any previous boot animations
                clearBootAnimationCache();
                try {
                    Context themeContext = mContext.createPackageContext(mTheme.packageName, 0);
                    Log.d("TEST", "name=" + boot[0]);
                    InputStream is = themeContext.getAssets().open("bootanimation/" + boot[0]);
                    //Utils.copyAsset(am, "bootanimation/bootanimation.zip", f.getAbsolutePath());
                    //FileOutputStream fos = new FileOutputStream(f);
                    //IOUtils.copy(is, fos);
                    //fos.close();
                    FileUtils.copyInputStreamToFile(is, f);
                    is.close();
                } catch (Exception e) {
                    Log.w(TAG, "Unable to load boot animation", e);
                    return null;
                }
            }
            try {
                zip = new ZipFile(f);
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
                    .setTitle("Preview");
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mGroup.selectedStyle = file.getName();
                    dialogInterface.dismiss();
                }
            });
            mProgress.dismiss();
            animationView.setBootAnimation(file);
            builder.show();
            animationView.start();
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
