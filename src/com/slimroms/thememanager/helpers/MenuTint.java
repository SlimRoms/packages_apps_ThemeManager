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

import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Menu;
import android.view.MenuItem;

public class MenuTint {
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
