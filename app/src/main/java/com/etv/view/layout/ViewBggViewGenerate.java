package com.etv.view.layout;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.ys.bannerlib.util.GlideImageUtil;
import com.ys.etv.R;

import java.util.List;

/**
 * 显示背景
 */
public class ViewBggViewGenerate extends Generator {

    View view;
    private ImageView iv_bgg_view;
    ImageView iv_bgg_color;
    private String imagePath;
    Context context;

    @Override
    public void timeChangeToUpdateView() {

    }

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @Override
    public void updateTextInfo(Object object) {

    }

    public ViewBggViewGenerate(Context context, int startX, int StartY, int width, int height, String imagePath) {
        super(context, startX, StartY, width, height);
        this.imagePath = imagePath;
        this.context = context;
        view = View.inflate(context, R.layout.view_bgg, null);
        initView(view);
    }

    private void initView(View view) {
        iv_bgg_view = (ImageView) view.findViewById(R.id.iv_bgg_view);
        iv_bgg_color = (ImageView) view.findViewById(R.id.iv_bgg_color);
        GlideImageUtil.loadImageNoCache(context, imagePath, iv_bgg_view);
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
    }

    @Override
    public void playComplet() {

    }

    @Override
    public void pauseDisplayView() {

    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    @Override
    public void resumePlayView() {

    }

    @Override
    public void moveViewForward(boolean b) {

    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {

    }

    @Override
    public void removeCacheView(String tag) {
        clearMemory();
    }
}
