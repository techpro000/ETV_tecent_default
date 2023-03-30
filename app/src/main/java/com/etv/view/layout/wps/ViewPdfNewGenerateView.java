package com.etv.view.layout.wps;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.db.DbStatiscs;
import com.etv.entity.StatisticsEntity;
import com.etv.listener.TaskPlayStateListener;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.TimerDealUtil;
import com.etv.view.layout.Generator;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.ys.bannerlib.BannerConfig;
import com.ys.etv.R;

import java.io.File;
import java.util.List;

/***
 * 详细讲解地址
 * https://github.com/barteksc/AndroidPdfViewer
 * 原作者    implementation 'com.github.barteksc:android-pdf-viewer:3.2.0-beta.1'
 * 自己封装  implementation 'com.gitee.shotter:android-pdf-view:1.0.2'
 * github 库文件地址
 */
public class ViewPdfNewGenerateView extends Generator {
    Context context;
    CpListEntity cpListEntity;
    List<MediAddEntity> mixtureList = null;
    View view;
    /**
     * 间隔时间
     */
    public int POLLING_INTERVAL = 5000;
    int screenWidth;
    int screenHeight;

    public ViewPdfNewGenerateView(Context context, CpListEntity cpListEntity, int x, int y, int width, int height, List<MediAddEntity> mixtureListCache) {
        super(context, x, y, width, height);
        this.context = context;
        this.screenWidth = width;
        this.screenHeight = height;
        this.cpListEntity = cpListEntity;
        this.mixtureList = mixtureListCache;
        view = LayoutInflater.from(context).inflate(R.layout.view_pdf_show_new, null);
        pdfInit(view);
        initListener();
        TimerDealUtil.getInstance().addGeneratorToList(ViewPdfNewGenerateView.this);
    }


    PDFView pdfview;
    LinearLayout iv_no_data;
    View view_click_pdf;
    TextView tv_no_date;

    private void pdfInit(View view) {
        view_click_pdf = (View) view.findViewById(R.id.view_click_pdf);
        iv_no_data = (LinearLayout) view.findViewById(R.id.iv_no_data);
        tv_no_date = (TextView) view.findViewById(R.id.tv_no_date);
        if (!SharedPerManager.getWpsShowEnable()) {
            iv_no_data.setVisibility(View.VISIBLE);
            tv_no_date.setText(context.getString(R.string.open_not_open));
            return;
        }

        if (mixtureList == null || mixtureList.size() < 1) {
            iv_no_data.setVisibility(View.VISIBLE);
            return;
        }
        MediAddEntity mediAddEntity = mixtureList.get(CURRENT_PLAY_POSITION);
        loadPdfFileToView(mediAddEntity);
    }

    //当前文件得个数
    private int CURRENT_PLAY_POSITION = 0;

    /***
     * 开始加载PDF 文件
     * @param mediAddEntity
     */
    private void loadPdfFileToView(MediAddEntity mediAddEntity) {
        String filePath = mediAddEntity.getUrl();
        File file = new File(filePath);
        if (!file.exists()) {
            iv_no_data.setVisibility(View.VISIBLE);
            TimerDealUtil.getInstance().removeGeneratorToList(ViewPdfNewGenerateView.this);
            return;
        }
        if (pdfview == null) {
            pdfview = (PDFView) view.findViewById(R.id.pdfview);
        }
        pdfview.fromFile(file)
                .defaultPage(currentShowPosition)
                .swipeHorizontal(true)  //横向移动
                .enableDoubletap(false)
                .enableSwipe(false)
                .spacing(0) // in dp
                .autoSpacing(false)
                .pageFitPolicy(FitPolicy.FULL_SCREEN)  //居中显示
                .enableAnnotationRendering(true)
                .setShowScreenSize(screenWidth, screenHeight)
                .onLoad(onLoadCompleteListener)
                .onPageChange(onPageChangeListener)
                .onPageError(onPageErrorListener)
                .load();
    }

    private void initListener() {
        view_click_pdf.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.longClickView(cpListEntity, null);
                }
                return true;
            }
        });
    }

    int addNum = 1;

    @Override
    public void timeChangeToUpdateView() {
        addNum++;
        if (addNum % POLLING_INTERVAL == 0) {
            goToShowNextPagePosition();
        }
        if (addNum > 9999) {
            addNum = 0;
        }
    }

    private void setPageTime(int duration, String printTag) {
        MyLog.playTask("设置文档切换时间=" + duration + " /printTag =  " + printTag);
        if (duration < 5000) {
            POLLING_INTERVAL = 10;
            return;
        }
        POLLING_INTERVAL = duration / 1000;
    }

    @Override
    public void updateTextInfo(Object object) {

    }

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    private int currentShowPosition = 0;
    private int totalPageSize = 0;

    private void goToShowNextPagePosition() {
        if (totalPageSize < 1) {
            return;
        }
        currentShowPosition++;
        if (currentShowPosition > totalPageSize) {
            //当前文件播放完毕，播放下一个文件
            playNextFile();
            return;
        }
        if (pdfview != null) {
//            pdfview.setPositionOffset(currentShowPosition);
            pdfview.jumpTo(currentShowPosition, true);
        }
    }

    /***
     * 播放下一个pdf 文件
     */
    private void playNextFile() {
        CURRENT_PLAY_POSITION++;
        if (CURRENT_PLAY_POSITION > mixtureList.size() - 1) {
            playComplet();
            CURRENT_PLAY_POSITION = 0;
        }
        currentShowPosition = 0;
        MediAddEntity mediAddEntity = mixtureList.get(CURRENT_PLAY_POSITION);
        loadPdfFileToView(mediAddEntity);
    }

//    private void goToShowPrePagePosition() {
//        currentShowPosition--;
//        if (totalPageSize < 1) {
//            return;
//        }
//        if (currentShowPosition < 0) {
//            currentShowPosition = totalPageSize;
//        }
//        if (pdfview != null) {
////            pdfview.setPositionOffset(currentShowPosition);
//            pdfview.jumpTo(currentShowPosition, true);
//        }
//    }

    /***
     * 加载完成
     */
    OnLoadCompleteListener onLoadCompleteListener = new OnLoadCompleteListener() {

        @Override
        public void loadComplete(int nbPages) {
            totalPageSize = nbPages;
        }
    };

    OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageChanged(int page, int pageCount) {
//            currentShowPosition = page;
        }
    };

    OnPageErrorListener onPageErrorListener = (page, throwable) -> {
    };

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {
        TimerDealUtil.getInstance().removeGeneratorToList(ViewPdfNewGenerateView.this);
        if (pdfview != null) {
            pdfview.recycle();
            pdfview = null;
        }
    }

    @Override
    public void removeCacheView(String tag) {
        clearMemory();
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        try {
            int duration = 5 * 1000;
            MediAddEntity mediAddEntity = (MediAddEntity) object;
            if (mediAddEntity == null) {
                duration = 10 * 1000;
                setPageTime(duration, "updateView，mediAddEntity == null");
                return;
            }
            String playParam = mediAddEntity.getPlayParam();
            if (playParam == null || playParam.length() < 1) {
                duration = 10 * 1000;
                setPageTime(duration, "playParam ==null");
                return;
            }
            duration = Integer.parseInt(playParam);
            setPageTime(duration * 1000, "正常设置时间");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playComplet() {
        if (listener != null) {
            listener.playComplete(TaskPlayStateListener.TAG_PLAY_WPS);
        }
    }

    @Override
    public void pauseDisplayView() {
    }

    @Override
    public void resumePlayView() {
    }

    @Override
    public void moveViewForward(boolean b) {
//        if (b) {  //下一页
//            goToShowNextPagePosition();
//        } else { //上一页
//            goToShowPrePagePosition();
//        }
    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

}
