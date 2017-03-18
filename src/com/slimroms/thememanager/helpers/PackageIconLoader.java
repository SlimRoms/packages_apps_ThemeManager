/*
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
package com.slimroms.thememanager.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.widget.ImageView;

public class PackageIconLoader {
    private static final int CACHE_SIZE = 100;
    private static LruCache<String, Drawable> mCache = new LruCache<>(CACHE_SIZE);

    public static void load(Context context, ImageView target, String packageName) {
        try {
            Drawable d = mCache.get(packageName);
            if (d != null) {
                target.setImageDrawable(d);
            } else {
                try {
                    d = context.getPackageManager().getApplicationIcon(packageName);
                } catch (PackageManager.NameNotFoundException ex) {
                    d = context.getPackageManager().getApplicationIcon("android");
                }
                target.setImageDrawable(d);
                mCache.put(packageName, d);
            }
        }
        catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
            target.setImageBitmap(null);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        mCache.evictAll();
        super.finalize();
    }
}
