package com.slimroms.thememanager.helpers;

import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Menu;
import android.view.MenuItem;

public class MenuTintHelper {
    public static void tintMenu(Menu menu, int color) {
        for (int i = 0; i < menu.size(); ++i) {
            final MenuItem item = menu.getItem(i);
            if (item.isEnabled()) {
                final Drawable drawable = item.getIcon();
                if (drawable != null) {
                    final Drawable wrapped = DrawableCompat.wrap(drawable);
                    drawable.mutate();
                    DrawableCompat.setTint(wrapped, color);
                    item.setIcon(drawable);
                }
            }
        }
    }
}
