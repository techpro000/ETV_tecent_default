package com.etv.view.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.ys.bannerlib.util.GlideImageUtil;
import com.ys.etv.R;

import java.io.File;
import java.util.List;

/**
 * 展示控件
 * Created by 打怪机器人
 */
public class ViewButtonGenerate extends Generator {

    View view;
    CpListEntity cplistEntity;
    String content;
    Context context;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @Override
    public void timeChangeToUpdateView() {

    }

    @Override
    public void updateTextInfo(Object object) {

    }

    public ViewButtonGenerate(Context context, CpListEntity cplistEntity, int startX, int StartY, int width, int height, String content) {
        super(context, startX, StartY, width, height);
        MyLog.playTask("======button 点击事件=======onCreate=====");
        this.cplistEntity = cplistEntity;
        this.context = context;
        this.content = content;
        view = LayoutInflater.from(context).inflate(R.layout.view_button, null);
        initView(view);
    }

    TextView btn_view_click;
    ImageView iv_btn_bgg;

    private void initView(View view) {
        iv_btn_bgg = (ImageView) view.findViewById(R.id.iv_btn_bgg);
        btn_view_click = (TextView) view.findViewById(R.id.btn_view_click);
        btn_view_click.setTextSize(taFontSize);
        btn_view_click.setTextColor(textColor);
        iv_btn_bgg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyLog.playTask("======button 点击事件============");
                if (listener != null) {
                    listener.clickTaskView(cplistEntity, null, 0);
                }
            }
        });
    }

    float taFontSize = 25;  //字体大小
    int textColor = 0xffffffff;

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        TextInfo textInfo = (TextInfo) object;
        if (textInfo == null) {
            return;
        }
        addButtonBggColor(textInfo);
        try {
            String textSizeString = textInfo.getTaFontSize();
            int textSize = 25;
            if (textSizeString != null && textSizeString.length() > 0) {
                textSize = Integer.parseInt(textSizeString);
            } else {
                textSize = 25;
            }
            String textContent = textInfo.getTaContent();
            btn_view_click.setText(textContent);
            btn_view_click.setTextSize(textSize);
            String textColot = textInfo.getTaColor();
            int textColor = TaskDealUtil.getColorFromInToSystem(textColot);
            btn_view_click.setTextColor(textColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addButtonBggColor(TextInfo textInfo) {
        String taBgImage = textInfo.getTaBgImage();
        MyLog.playTask("=====buttonImage====" + taBgImage);
        if (taBgImage != null || taBgImage.length() > 5) {
            String imageLocalPath = TaskDealUtil.getSavePath(taBgImage);
            File file = new File(imageLocalPath);
            MyLog.playTask("=====buttonImage=imageName==文件存在=" + (file.exists()));
            if (file.exists()) {
                GlideImageUtil.loadImageByPath(context, imageLocalPath, iv_btn_bgg);
            }
            MyLog.playTask("=====buttonImage=imageName===" + imageLocalPath);
        }
        String bggColor = textInfo.getTaBgColor();
        btn_view_click.setBackgroundColor(TaskDealUtil.getColorFromInToSystem(bggColor));
        MyLog.playTask("=====buttonImage=显示背景色===");
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
    public void moveViewForward(boolean b) {

    }

    @Override
    public void resumePlayView() {

    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {
        if (btn_view_click != null) {
            btn_view_click = null;
        }
    }

    @Override
    public void removeCacheView(String tag) {
        clearMemory();
    }
}
