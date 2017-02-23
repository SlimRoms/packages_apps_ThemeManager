package com.slimroms.thememanager.adapters;

import android.content.Context;
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

        ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.lbl_name);
            description = (TextView) itemView.findViewById(R.id.lbl_description);
            picture = (ImageView) itemView.findViewById(R.id.img_picture);
        }
    }

    public static class Bean {
        public CharSequence name;
        public CharSequence description;
        public Drawable image;
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

    public AboutAdapter(Context context, List<Object> items) {
        mInflater = LayoutInflater.from(context);
        strVersions = context.getResources().getString(R.string.versions);
        strTeam = context.getResources().getString(R.string.the_team);
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
