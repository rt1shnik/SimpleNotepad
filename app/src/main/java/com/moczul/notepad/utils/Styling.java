package com.moczul.notepad.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

public abstract class Styling {
    private Styling() {
    }

    public static class Fonts {
        public static final String LATO_BOLD = "Lato-Bold.ttf";
    }

    public static TextView updateWithFont(Context context, TextView tv, String fontName) {
        tv.setTypeface(Typeface.createFromAsset(
                context.getAssets(),
                "fonts/" + fontName));
        return tv;
    }
}