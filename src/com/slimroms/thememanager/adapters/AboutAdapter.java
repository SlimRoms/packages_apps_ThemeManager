package com.slimroms.thememanager.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.slimroms.thememanager.R;

import java.util.ArrayList;
import java.util.List;

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView description;
        ImageView picture;
        TextView signature;

        ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.lbl_name);
            description = (TextView) itemView.findViewById(R.id.lbl_description);
            picture = (ImageView) itemView.findViewById(R.id.img_picture);
            signature = (TextView) itemView.findViewById(R.id.lbl_signature);
        }
    }

    public static class Bean {
        public CharSequence name;
        public CharSequence description;
        public Drawable image;
        public int signatureCheckResult = -1000;
    }

    public static final String ITEM_VERSIONS = "ITEM_VERSIONS";
    public static final String ITEM_TEAM = "ITEM_TEAM";
    private static final int ITEM_TYPE_BEAN = 0;
    private static final int ITEM_TYPE_VERSIONS = 1;
    private static final int ITEM_TYPE_TEAM = 2;

    private final LayoutInflater mInflater;
    private final List<Object> mItems = new ArrayList<>();
    private final String strVersions;
    private final String strTeam;
    private final String strSigned;
    private final String strNotSigned;
    private final String strWrongSignature;

    public AboutAdapter(Context context, List<Object> items) {
        mInflater = LayoutInflater.from(context);
        strVersions = context.getString(R.string.versions);
        strTeam = context.getString(R.string.the_team);
        strSigned = context.getString(R.string.signed);
        strNotSigned = context.getString(R.string.not_signed);
        strWrongSignature = context.getString(R.string.wrong_signature);
        mItems.addAll(items);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case ITEM_TYPE_VERSIONS:
            case ITEM_TYPE_TEAM:
                view = mInflater.inflate(R.layout.item_about_category, parent, false);
                break;
            case ITEM_TYPE_BEAN:
                view = mInflater.inflate(R.layout.item_about_bean, parent, false);
                break;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case ITEM_TYPE_VERSIONS:
                holder.name.setText(strVersions);
                break;
            case ITEM_TYPE_TEAM:
                holder.name.setText(strTeam);
                break;
            case ITEM_TYPE_BEAN:
                final Bean bean = (Bean) mItems.get(position);
                holder.name.setText(bean.name);
                holder.description.setText(bean.description);
                holder.picture.setImageDrawable(bean.image);
                switch (bean.signatureCheckResult) {
                    case PackageManager.SIGNATURE_MATCH:
                        holder.signature.setText(strSigned);
                        holder.signature.setTextColor(Color.GREEN);
                        holder.signature.setVisibility(View.VISIBLE);
                        break;
                    case PackageManager.SIGNATURE_FIRST_NOT_SIGNED:
                    case PackageManager.SIGNATURE_NEITHER_SIGNED:
                        holder.signature.setText(strNotSigned);
                        holder.signature.setTextColor(Color.RED);
                        holder.signature.setVisibility(View.VISIBLE);
                        break;
                    case PackageManager.SIGNATURE_NO_MATCH:
                        holder.signature.setText(strWrongSignature);
                        holder.signature.setTextColor(Color.RED);
                        holder.signature.setVisibility(View.VISIBLE);
                        break;
                    default:
                        holder.signature.setVisibility(View.GONE);
                        break;
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        final Object item = mItems.get(position);
        switch (item.toString()) {
            case ITEM_TEAM:
                return ITEM_TYPE_TEAM;
            case ITEM_VERSIONS:
                return ITEM_TYPE_VERSIONS;
            default:
                return ITEM_TYPE_BEAN;
        }
    }
}
