package com.example.admin.wobeassignment.utilities;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Admin on 22-09-2017.
 */

public class FontManager {

    public static final String ROOT = "fonts/",
            FONTAWESOME = ROOT + "fontawesome-webfont.ttf";

    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }
}
