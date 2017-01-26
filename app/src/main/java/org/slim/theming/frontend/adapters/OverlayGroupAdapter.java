package org.slim.theming.frontend.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.slimroms.themecore.Overlay;
import com.slimroms.themecore.OverlayFlavor;
import com.slimroms.themecore.OverlayGroup;
import org.slim.theming.frontend.R;

public class OverlayGroupAdapter extends RecyclerView.Adapter<OverlayGroupAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checked;
        TextView overlayName;
        TextView overlayTargetPackage;
        ImageView overlayInstalled;
        Spinner overlayFlavors;
        ViewGroup clickContainer;

        ViewHolder(View itemView) {
            super(itemView);
            checked = (CheckBox) itemView.findViewById(R.id.checkbox);
            overlayName = (TextView) itemView.findViewById(R.id.overlay_name);
            overlayTargetPackage = (TextView) itemView.findViewById(R.id.overlay_package);
            overlayInstalled = (ImageView) itemView.findViewById(R.id.img_cellphone);
            overlayFlavors = (Spinner) itemView.findViewById(R.id.spinner);
            clickContainer = (ViewGroup) itemView.findViewById(R.id.click_container);
        }
    }

    private LayoutInflater mInflater;
    private OverlayGroup mOverlayGroup;
    private Context mContext;
    private final ColorStateList mDefaultTextColors;
    private final int mEnabledTextColor;
    private final int mDisabledTextColor;

    public OverlayGroupAdapter(Context context, OverlayGroup proxy) {
        mInflater = LayoutInflater.from(context);
        mOverlayGroup = proxy;
        mContext = context;

        mEnabledTextColor = ContextCompat.getColor(context, R.color.overlay_enabled);
        mDisabledTextColor = ContextCompat.getColor(context, R.color.overlay_disabled);
        final TextView dummyTextView = new TextView(context);
        mDefaultTextColors = dummyTextView.getTextColors();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.item_overlay, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Overlay overlay = mOverlayGroup.overlays.get(position);
        holder.overlayName.setText(overlay.overlayName);
        holder.overlayTargetPackage.setText(overlay.targetPackage);
        final ArrayAdapter<OverlayFlavor> adapter = new ArrayAdapter<>(mContext, R.layout.item_flavor, overlay.flavors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.overlayFlavors.setAdapter(adapter);
        if (overlay.flavors.size() > 1) {
            holder.overlayFlavors.setVisibility(View.VISIBLE);
            holder.overlayFlavors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    overlay.clearSelectedFlavors();
                    overlay.flavors.get(position).selected = true;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        else {
            holder.overlayFlavors.setVisibility(View.GONE);
            holder.overlayFlavors.setOnItemSelectedListener(null);
        }

        if (overlay.isOverlayInstalled) {
            holder.overlayInstalled.setVisibility(View.VISIBLE);
            holder.overlayName.setTextColor(overlay.isOverlayEnabled ? mEnabledTextColor : mDisabledTextColor);
        }
        else {
            holder.overlayInstalled.setVisibility(View.GONE);
            holder.overlayName.setTextColor(mDefaultTextColors);
            holder.overlayName.setEnabled(overlay.isTargetPackageInstalled);
            holder.overlayTargetPackage.setEnabled(overlay.isTargetPackageInstalled);
        }

        holder.checked.setChecked(overlay.checked);
        holder.checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay.checked = ((CheckBox) v).isChecked();
            }
        });
        holder.clickContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean newValue = !overlay.checked;
                holder.checked.setChecked(newValue);
                holder.checked.callOnClick();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mOverlayGroup.overlays.size();
    }
}
