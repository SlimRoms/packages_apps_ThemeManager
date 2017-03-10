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
