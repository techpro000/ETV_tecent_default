package com.ys.bannerlib.transformer;

import android.view.View;

public class StackTransformer extends ABaseTransformer {

    public static StackTransformer create(){
        return new StackTransformer();
    }

    @Override
    protected void onTransform(View view, float position) {
        view.setTranslationX(position < 0 ? 0f : -view.getWidth() * position);
    }

}
