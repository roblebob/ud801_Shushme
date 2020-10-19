package com.example.android.shushme;

import android.content.Context;
import android.util.AttributeSet;

import androidx.constraintlayout.utils.widget.ImageFilterView;

public class MyBar extends ImageFilterView {

    public MyBar(Context context)                                       { super(context); }
    public MyBar(Context context, AttributeSet attrs)                   { super(context, attrs); }
    public MyBar(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
