package com.codersworld.awesalibs.customFonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class SBTextViewRegular extends TextView {
    Context ctx;

    public SBTextViewRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
            setTypeface(Typeface.createFromAsset(context.getAssets(), "nunito_regular.ttf"));
    }
}
