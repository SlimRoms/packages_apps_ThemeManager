/*
 * Copyright (C) 2017 SlimRoms Project
 * Copyright (C) 2017 Victor Lapin
 * Copyright (C) 2017 Griffin Millender
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.slimroms.thememanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
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

    private static final Boolean ENABLE_BLACKLISTED_APPLICATIONS = false;
    private static final String[] BLACKLISTED_APPLICATIONS = new String[]{
            "com.android.vending.billing.InAppBillingService.LOCK",
            "com.android.vending.billing.InAppBillingService.LACK",
            "uret.jasi2169.patcher",
            "com.dimonvideo.luckypatcher",
            "com.chelpus.lackypatch",
            "com.forpda.lp",
            "com.android.vending.billing.InAppBillingService.LUCK",
            "com.android.protips",
    };

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
            
        } else if (checkBlacklistedApps()) {
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

    private ApplicationInfo getPackageInfo(String packageName) {
        try {
            return getPackageManager().getApplicationInfo(packageName, 0);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean checkBlacklistedApps() {
        ApplicationInfo info = null;
        for (String app : BLACKLISTED_APPLICATIONS) {
            info = getPackageInfo(app);
            if (info != null) {
                break;
            }
        }
        if (info != null) {
             final AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage(getString(R.string.blacklisted_app_message) + info.loadLabel(getPackageManager()))
                     .setTitle(R.string.blacklisted_app_title)
                     .setCancelable(false)
                     .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int i) {
                            MainActivity.this.finishAffinity();
                        }
                     });
            builder.show();
        }
        return info == null;
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
        }
        else if (intent != null) {
            ActivityCompat.startActivity(this, intent, null);
        }
        else
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
