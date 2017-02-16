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

import java.util.ArrayList;
import java.util.Collections;

public class OverlayGroupAdapter extends RecyclerView.Adapter<OverlayGroupAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checked;
        TextView overlayName;
        TextView overlayTargetPackage;
        ImageView overlayImage;
        LinearLayout overlayFlavors;
        ViewGroup clickContainer;

        ViewHolder(View itemView) {
            super(itemView);
            checked = (CheckBox) itemView.findViewById(R.id.checkbox);
            overlayName = (TextView) itemView.findViewById(R.id.overlay_name);
            overlayTargetPackage = (TextView) itemView.findViewById(R.id.overlay_package);
            overlayImage = (ImageView) itemView.findViewById(R.id.overlay_image);
            overlayFlavors = (LinearLayout) itemView.findViewById(R.id.spinner_layout);
            clickContainer = (ViewGroup) itemView.findViewById(R.id.click_container);
        }
    }

    private LayoutInflater mInflater;
    private OverlayGroup mOverlayGroup;
    private Context mContext;
    private final ColorStateList mDefaultTextColors;
    private final int mEnabledTextColor;
    private final int mDisabledTextColor;
    private final int mSpinnerPadding;

    public OverlayGroupAdapter(Context context, OverlayGroup proxy) {
        mInflater = LayoutInflater.from(context);
        mOverlayGroup = proxy;
        mContext = context;

        mEnabledTextColor = ContextCompat.getColor(context, R.color.overlay_enabled);
        mDisabledTextColor = ContextCompat.getColor(context, R.color.overlay_disabled);
        final TextView dummyTextView = new TextView(context);
        mDefaultTextColors = dummyTextView.getTextColors();
        mSpinnerPadding = context.getResources().getDimensionPixelSize(R.dimen.margin_small);
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
        if (overlay.flavors.size() > 0) {
            holder.overlayFlavors.removeAllViewsInLayout();
            holder.overlayFlavors.setVisibility(View.VISIBLE);
            for (final OverlayFlavor flavor : overlay.flavors.values()) {
                Spinner spinner = (Spinner) View.inflate(mContext, R.layout.flavor_spinner, null);
                spinner.setTag(flavor);
                spinner.setPadding(0, mSpinnerPadding, 0, mSpinnerPadding);
                final ArrayList<String> array = new ArrayList<>();
                array.addAll(flavor.flavors.values());
                Collections.sort(array);
                array.add(0, flavor.name);
                final ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(mContext, R.layout.item_flavor, array);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        overlay.clearSelectedFlavors();
                        OverlayFlavor flavor1 = (OverlayFlavor) adapterView.getTag();
                        flavor1.selected = array.get(i);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                spinner.setSelection(array.indexOf(flavor.selected), true);
                holder.overlayFlavors.addView(spinner);
            }
        }
        else {
            holder.overlayFlavors.setVisibility(View.GONE);
        }

        if (overlay.isOverlayInstalled) {
            holder.overlayName.setTextColor(overlay.isOverlayEnabled ? mEnabledTextColor : mDisabledTextColor);
        }
        else {
            holder.overlayName.setTextColor(mDefaultTextColors);
            holder.overlayName.setEnabled(overlay.isTargetPackageInstalled);
            holder.overlayTargetPackage.setEnabled(overlay.isTargetPackageInstalled);
        }

        holder.overlayImage.setImageBitmap(overlay.overlayImage);
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
