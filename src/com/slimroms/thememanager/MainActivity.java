/*
 * Copyright (C) 2016-2017 Projekt Substratum
 *
 * Modified/reimplemented for use by SlimRoms :
 *
 * Copyright (C) 2017 SlimRoms Project
 * Copyright (C) 2017 Victor Lapin
 * Copyright (C) 2017 Griffin Millender
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.slimroms.thememanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.slimroms.thememanager.fragments.ThemesPackagesFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = MainActivity.class.getSimpleName();
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check signatures first, do stuff later
        final int signatureCheckResult = App.getInstance().checkSignature(getPackageName());
        if (signatureCheckResult != PackageManager.SIGNATURE_MATCH) {
            // found security issue, should finish work
            Log.i(TAG, App.class.getName() + " encountered a signature mismatch: " + signatureCheckResult);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.signature_mismatch)
                    .setTitle(R.string.signature_mismatch_title)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.this.finishAffinity();
                        }
                    });
            builder.create().show();
        } else {
            App.getInstance().bindBackends();
        }

        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_drawer);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        mDrawerToggle.syncState();

        if (savedInstanceState == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, ThemesPackagesFragment.newInstance(), ThemesPackagesFragment.TAG);
            ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
        }
    }

    @Override
    protected void onDestroy() {
        App.getInstance().unbindBackends();
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.isChecked()) {
            // if user has pressed the already selected item, do nothing
            mDrawerLayout.closeDrawers();
            return true;
        }

        Fragment fragment = null;
        Intent intent = null;
        String tag = "";

        switch (item.getItemId()) {
            case R.id.nav_about:
                intent = new Intent(this, AboutActivity.class);
                break;
            case R.id.nav_uninstall_overlays:
                intent = new Intent(this, UninstallActivity.class);
                break;
        }

        if (fragment != null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment, tag);
            ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(tag);
            ft.commit();
        } else if (intent != null) {
            ActivityCompat.startActivity(this, intent, null);
        } else
            return false;

        mDrawerLayout.closeDrawers();
        return true;
    }


    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }
}
