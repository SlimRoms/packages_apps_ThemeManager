package org.slim.theming.frontend.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayGroup;
import org.slim.theming.frontend.R;

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

    private LayoutInflater mInflater;
    private OverlayGroup mOverlayGroup;

    public WallpaperGroupAdapter(Context context, OverlayGroup proxy) {
        mInflater = LayoutInflater.from(context);
        mOverlayGroup = proxy;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.item_wallpaper, parent, false);
        return new WallpaperGroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Overlay overlay = mOverlayGroup.overlays.get(position);
        holder.overlayName.setText(overlay.overlayName);
        holder.overlayImage.setImageBitmap(overlay.overlayImage);
        holder.clickContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mOverlayGroup.overlays.size();
    }
}
