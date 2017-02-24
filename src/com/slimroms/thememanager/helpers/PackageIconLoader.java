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
