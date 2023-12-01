package com.codersworld.awesalibs.customFonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class SBTextViewBold extends TextView {
    public SBTextViewBold(Context context, AttributeSet attrs) {
        super(context, attrs);
            setTypeface(Typeface.createFromAsset(context.getAssets(), "nunito_bold.ttf"));
    }
}
