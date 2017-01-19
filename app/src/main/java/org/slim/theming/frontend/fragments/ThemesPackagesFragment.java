package org.slim.theming.frontend.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import org.slim.theming.frontend.R;
import org.slim.theming.frontend.adapters.ThemesPackagesAdapter;
import org.slim.theming.frontend.views.LineDividerItemDecoration;

public class ThemesPackagesFragment extends Fragment {
    public static final String TAG = ThemesPackagesFragment.class.getSimpleName();

    public static ThemesPackagesFragment newInstance() {
        return new ThemesPackagesFragment();
    }

    @BindView(R.id.empty_view)
    TextView mEmptyView;
    @BindView(R.id.list)
    RecyclerView mRecycler;

    private Unbinder mUnbinder;
    private ThemesPackagesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        getActivity().setTitle(R.string.nav_themes);

        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycler.addItemDecoration(new LineDividerItemDecoration(getContext()));
        mRecycler.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ThemesPackagesAdapter(getContext());
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }
}
