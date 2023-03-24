package com.ys.bannerlib.transformer;

import android.view.View;

public class DefaultTransformer extends ABaseTransformer {

    public static DefaultTransformer create(){
        return new DefaultTransformer();
    }

    @Override
    protected void onTransform(View view, float position) {
    }

    @Override
    public boolean isPagingEnabled() {
        return true;
    }

}
