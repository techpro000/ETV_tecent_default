package com.ys.bannerlib.transformer;

import android.view.View;

public class ScaleInOutTransformer extends ABaseTransformer {

	public static ScaleInOutTransformer create(){
		return new ScaleInOutTransformer();
	}

	@Override
	protected void onTransform(View view, float position) {
		view.setPivotX(position < 0 ? 0 : view.getWidth());
		view.setPivotY(view.getHeight() / 2f);
		float scale = position < 0 ? 1f + position : 1f - position;
		view.setScaleX(scale);
		view.setScaleY(scale);
	}

}
