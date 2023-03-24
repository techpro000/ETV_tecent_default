package com.etv.view.layout.image;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.util.MyLog;
import com.etv.view.layout.Generator;
import com.ys.bannerlib.util.GlideImageUtil;
import com.ys.etv.R;

import java.util.List;

/***
 * 用来加载 logo 的 view
 */
public class ViewLogoImageGenertrator extends Generator {

    String fileImageUrl;
    View view;
    Context context;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    public ViewLogoImageGenertrator(Context context, int x, int y, int width, int height, String fileImageUrl) {
        super(context, x, y, width, height);
        this.context = context;
        this.fileImageUrl = fileImageUrl;
        view = View.inflate(context, R.layout.view_logo_image, null);
        MyLog.bgg("=====加载 logo==updateView=000");
        initView();
    }

    ImageView logo_image;

    private void initView() {
        logo_image = (ImageView) view.findViewById(R.id.logo_image);
        GlideImageUtil.loadImageByPath(context, fileImageUrl, logo_image);
        logo_image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.longClickView(null, null);
                }
                return true;
            }
        });
    }


    @Override
    public void updateView(Object object, boolean isShowBtn) {
        MyLog.bgg("=====加载 logo==updateView=111");
    }

    @Override
    public void timeChangeToUpdateView() {

    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {
        if (logo_image != null) {
            GlideImageUtil.clearViewCache(logo_image);
        }
    }

    @Override
    public void removeCacheView(String tag) {

    }


    @Override
    public void updateTextInfo(Object object) {
        String logoPath = (String) object;
        if (logoPath == null || logoPath.length() < 5) {
            return;
        }
        if (logo_image != null) {
            GlideImageUtil.loadImageCacheDisk(context, logoPath, logo_image);
        }
    }

    @Override
    public void playComplet() {

    }

    @Override
    public void pauseDisplayView() {

    }

    @Override
    public void resumePlayView() {

    }

    @Override
    public void moveViewForward(boolean b) {

    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

}
