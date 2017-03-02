package com.slimroms.thememanager.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
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

public class FontGroupAdapter extends RecyclerView.Adapter<FontGroupAdapter.ViewHolder> {

    private static final String TAG = FontGroupAdapter.class.getSimpleName();

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

    private Context mContext;
    private OverlayGroup mGroup;
    private LayoutInflater mInflater;

    public FontGroupAdapter(Context context, OverlayGroup group) {
        mContext = context;
        mGroup = group;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // use bootanimation layout here, should be enough
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
                Typeface typeface = null;
                try {
                    typeface = Typeface.createFromFile(overlay.tag);
                }
                catch (RuntimeException ex) {
                    Log.w(TAG, "Unable to load font: " + overlay.tag, ex);
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                        .setTitle(mContext.getString(R.string.preview));
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton(mContext.getString(R.string.select), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        mGroup.clearSelected();
                        overlay.checked = true;
                        notifyDataSetChanged();
                    }
                });
                if (typeface != null) {
                    // show the preview
                    final View preview = mInflater.inflate(R.layout.preview_font, null);
                    final TextView txtLatin = (TextView) preview.findViewById(R.id.preview_latin);
                    txtLatin.setTypeface(typeface);
                    final TextView txtLatinBold = (TextView) preview.findViewById(R.id.preview_latin_bold);
                    txtLatinBold.setTypeface(typeface, Typeface.BOLD);
                    final TextView txtCyrillic = (TextView) preview.findViewById(R.id.preview_cyrillic);
                    txtCyrillic.setTypeface(typeface);
                    final TextView txtCyrillicBold = (TextView) preview.findViewById(R.id.preview_cyrillic_bold);
                    txtCyrillicBold.setTypeface(typeface, Typeface.BOLD);
                    builder.setView(preview);
                }
                builder.show();
            }
        });
        holder.check.setVisibility(overlay.checked ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mGroup.overlays.size();
    }
}
