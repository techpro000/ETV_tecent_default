package com.etv.view.layout.text;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etv.config.AppConfig;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.MyLog;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import java.util.List;

/**
 * 禁止状态的文本
 */
public class ViewTextSlientGenerate extends Generator {

    String textInfoShow;
    View view;
    CpListEntity cpListEntity;
    Context context;

    @Override
    public void updateTextInfo(Object object) {

    }

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    public ViewTextSlientGenerate(Context context, CpListEntity cpListEntity, int x, int y, int width, int height, String textInfo) {
        super(context, x, y, width, height);
        this.context = context;
        this.textInfoShow = textInfo;
        this.cpListEntity = cpListEntity;
        view = LayoutInflater.from(context).inflate(R.layout.view_text_slient, null);
        initView(view);
    }

    TextView tv_text_show;
    RelativeLayout rela_text_bgg;

    private void initView(View view) {
        rela_text_bgg = (RelativeLayout) view.findViewById(R.id.rela_text_bgg);
        tv_text_show = (TextView) view.findViewById(R.id.tv_text_show);
        MyLog.cdl("====静态字幕==" + textInfoShow);
        tv_text_show.setText(textInfoShow);
        tv_text_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyLog.cdl("================点击了字幕===");
                if (listener == null) {
                    return;
                }
                listener.clickTaskView(cpListEntity, null, 0);
            }
        });
        tv_text_show.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.longClickView(cpListEntity, textInfoShow);
                return true;
            }
        });
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        TextInfo textInfo = (TextInfo) object;
        int bggColor = 0x00000000;      //透明
        int textColorInt = 0xffffffff;  //默认白色
        String taAlignment = "5";

        String fontSize = "16";
        try {
            if (textInfo != null) {
                String textColor = textInfo.getTaColor();
                textColorInt = TaskDealUtil.getColorFromInToSystem(textColor);
                String backColor = textInfo.getTaBgColor();
                bggColor = TaskDealUtil.getColorFromInToSystem(backColor);
                fontSize = textInfo.getTaFontSize();
                taAlignment = textInfo.getTaAlignment();
            }
            int showPosition = TaskDealUtil.getShowPosition(taAlignment);
            tv_text_show.setGravity(showPosition);
            tv_text_show.setTextColor(textColorInt);
            float textSize = TaskDealUtil.getTextSize(fontSize);
            tv_text_show.setTextSize(textSize);
            String taFont = textInfo.getTaFonType();
            Typeface typeface = TaskDealUtil.getFontTypeFace(taFont);
            if (typeface != null) {
                tv_text_show.setTypeface(typeface);
            }
            rela_text_bgg.setBackgroundColor(bggColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playComplet() {

    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

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
        if (tv_text_show != null) {
            tv_text_show.setText("");
        }
    }

    @Override
    public void removeCacheView(String tag) {
    }

    @Override
    public void moveViewForward(boolean b) {

    }

    @Override
    public void pauseDisplayView() {

    }

    @Override
    public void resumePlayView() {

    }

}
