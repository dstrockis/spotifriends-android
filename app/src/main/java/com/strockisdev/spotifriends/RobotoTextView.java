package com.strockisdev.spotifriends;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by dastrock on 10/12/2014.
 */
public class RobotoTextView extends TextView{

    public RobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(isInEditMode()) {
            return;
        }

        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.RobotoTextView);
        String fontName = styledAttrs.getString(R.styleable.RobotoTextView_typeface);
        styledAttrs.recycle();

        if (fontName != null) {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontName);
            setTypeface(typeface);
        }
    }
}
