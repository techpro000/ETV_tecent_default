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


    private int CURRENT_PLAY_POSITION = 0;

    /***
     * 开始加载PDF 文件
     * @param mediAddEntity
     */
    private void loadPdfFileToView(MediAddEntity mediAddEntity) {
        String filePath = mediAddEntity.getUrl();
        MyLog.pdf("开始加载 pdf 文件==" + filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            iv_no_data.setVisibility(View.VISIBLE);
            return;
        }
        FitPolicy fitPolicy;
        int pdfShowType = SharedPerManager.geWPSSingleShowTYpe();
        if (pdfShowType == BannerConfig.SCREEN_SHOW_TYPE_ALL_GLOBLE) { //全屏拉伸
            fitPolicy = FitPolicy.FULL_SCREEN;
        } else {  //比例缩放
            fitPolicy = FitPolicy.BOTH_SCREEN;
        }
        pdfview = (PDFView) view.findViewById(R.id.pdfview);
        int showTypeAnimal = SharedPerManager.geWPSSingleShowAnimalTYpe();
        //0 左右   1 上下
        boolean isLeft_right = true;
        if (showTypeAnimal == 0) {
            isLeft_right = true;
        } else {
            isLeft_right = false;
        }
        pdfview.fromFile(file)
                .defaultPage(currentShowPosition)
                .swipeHorizontal(isLeft_right)
                .enableDoubletap(false)
                .enableSwipe(false)
                .spacing(0) // in dp
                .autoSpacing(false)
                .pageFitPolicy(fitPolicy)  //居中显示

//                .fitEachPage(true)
//                .pageSnap(true) // snap pages to screen boundaries
//                .pageFling(true) // make a fling change only a single page like ViewPager

                .enableAnnotationRendering(true)
                .setShowScreenSize(screenWidth, screenHeight)
                .onLoad(onLoadCompleteListener)
                .onPageChange(onPageChangeListener)
                .onPageError(onPageErrorListener)
                .load();
    }

    private void initListener() {
        view_click_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener == null) {
                    return;
                }
                listener.clickTaskView(cpListEntity, null, currentShowPosition);
            }
        });
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

    @Override
    public void timeChangeToUpdateView() {

    }

    private void setPageTime(int duration, String printTag) {
        MyLog.playTask("设置文档切换时间=" + duration + " /printTag =  " + printTag);
        POLLING_INTERVAL = duration;
        startToPlayPdfFile();
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

    private static final int AUTO_CHANGE_POSITION = 5621;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler.removeMessages(msg.what);
            switch (msg.what) {
                case AUTO_CHANGE_POSITION:
                    goToShowNextPagePosition();
                    break;
            }
        }
    };

    private void goToShowNextPagePosition() {
        startToPlayPdfFile();
        currentShowPosition++;
        if (totalPageSize < 1) {
            return;
        }
        MyLog.pdf("当前页码==" + currentShowPosition + " / " + totalPageSize);
        if (currentShowPosition > totalPageSize) {
            playNextFile();
            currentShowPosition = 0;
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
        MyLog.pdf("playNextFile==播放下一个");
        CURRENT_PLAY_POSITION++;
        if (CURRENT_PLAY_POSITION > mixtureList.size() - 1) {
            playComplet();
            CURRENT_PLAY_POSITION = 0;
        }
        MediAddEntity mediAddEntity = mixtureList.get(CURRENT_PLAY_POSITION);
        loadPdfFileToView(mediAddEntity);
    }

    private void goToShowPrePagePosition() {
        startToPlayPdfFile();
        currentShowPosition--;
        if (totalPageSize < 1) {
            return;
        }
        if (currentShowPosition < 0) {
            currentShowPosition = totalPageSize;
        }
        if (pdfview != null) {
//            pdfview.setPositionOffset(currentShowPosition);
            pdfview.jumpTo(currentShowPosition, true);
        }
    }

    public void startToPlayPdfFile() {
        if (handler == null) {
            return;
        }
        handler.removeMessages(AUTO_CHANGE_POSITION);
        handler.sendEmptyMessageDelayed(AUTO_CHANGE_POSITION, POLLING_INTERVAL);
    }


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
        if (handler != null) {
            handler.removeMessages(AUTO_CHANGE_POSITION);
            handler = null;
        }
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
        handler.removeMessages(AUTO_CHANGE_POSITION);
    }

    @Override
    public void resumePlayView() {
        handler.sendEmptyMessage(AUTO_CHANGE_POSITION);
    }

    @Override
    public void moveViewForward(boolean b) {
        if (b) {  //下一页
            goToShowNextPagePosition();
        } else { //上一页
            goToShowPrePagePosition();
        }
    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

}
