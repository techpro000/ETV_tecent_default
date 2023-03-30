package com.etv.view.layout.image;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

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
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.ys.bannerlib.BannerHelper;
import com.ys.bannerlib.adapter.ImageAdapter;
import com.ys.bannerlib.imageloader.GlideImageLoader;
import com.ys.bannerlib.util.GlideCacheUtil;
import com.ys.etv.R;

import java.util.ArrayList;
import java.util.List;

/***
 * 加载图片资源
 */
public class ViewImageGenertrator extends Generator {

    Context context;
    View view;
    List<MediAddEntity> imageList;
    int width;
    int height;
    CpListEntity cpListEntity;
    boolean typeZero;   //区域

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    public ViewImageGenertrator(Context context, CpListEntity cpListEntity, int startX, int StartY, int width, int height, List<MediAddEntity> imageList, boolean typeZero) {
        super(context, startX, StartY, width, height);
        this.context = context;
        this.width = width;
        this.height = height;
        this.typeZero = typeZero;
        this.imageList = imageList;
        this.cpListEntity = cpListEntity;
        view = LayoutInflater.from(context).inflate(R.layout.view_image, null);
    }

    @Override
    public void timeChangeToUpdateView() {

    }

    @Override
    public void updateTextInfo(Object object) {

    }

    Banner banner;
    BannerHelper helper;

    private void initView(View view) {
        int PageTransformer = 1;
        String carton = imageList.get(0).getCartoon();
        try {
            PageTransformer = Integer.parseInt(carton);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        MyLog.e("banner", "==mPageTransformerValue=carton=" + carton + " / " + PageTransformer);
        banner = (Banner) view.findViewById(R.id.banner_image);
        helper = new BannerHelper<MediAddEntity>(banner);
        helper.setDatas(new ArrayList<>(imageList));
        helper.setPageTransformer(PageTransformer);
        helper.setImageLoader(new GlideImageLoader(), SharedPerManager.getPicSingleShowTYpe());
        initListener();
        helper.startPlay();
    }

    private void initListener() {
        helper.setOnClickListener(new OnBannerListener<MediAddEntity>() {
            @Override
            public void OnBannerClick(MediAddEntity data, int position) {
                if (typeZero) {
                    MyLog.task("区域空间，点击直接拦截");
                    return;
                }
                List<String> stringList = new ArrayList<String>();
                for (MediAddEntity mediAddEntity : imageList) {
                    stringList.add(mediAddEntity.getImageUrl());
                }
                if (listener != null) {
                    listener.clickTaskView(cpListEntity, stringList, position);
                }
            }
        });

        helper.setOnLongClickListener(new ImageAdapter.OnPageLongClickListener() {
            @Override
            public boolean onLongClick(View v, int position) {
                if (typeZero) {
                    MyLog.task("区域空间，点击直接拦截");
                    return true;
                }
                if (listener != null) {
                    listener.longClickView(cpListEntity, null);
                }
                return true;
            }
        });

        helper.addOnPageChangeCallback(new ImageAdapter.OnPageChangeCallback() {
            @Override
            public void onPageChange(int position, boolean isLast) {
                MyLog.banner("=======onPageChange====" + position + " / " + isLast + " / size--> " + (imageList.size()));
                //播放完毕
                if (isLast) {
                    //playComplet();
                    banner.postDelayed(() -> {
                        playComplet();
                    }, imageList.get(position).getLoopTime());
                }

                if (position < imageList.size()) {
                    String midId = imageList.get(position).getMidId();
                    MediAddEntity entity = imageList.get(0);
                    int playTime = (int) entity.getLoopTime() / 1000;
                    addImageUpdateToWeb(midId, playTime);
                }
            }
        });

        /*helper.addOnPageChangePlayCallback((position, isLast) -> {
            MyLog.banner("=======onPageChange====" + position + " / " + isLast + " / size--> " + (imageList.size()));
            //播放完毕
            if (isLast) {
                MyLog.banner("=======onPageChange===ready=>");
                playComplet();
            }
            if (imageList == null || imageList.size() < 1) {
                return;
            }
            if (position < imageList.size()) {
                String midId = imageList.get(position).getMidId();
                MediAddEntity entity = imageList.get(0);
                int playTime = (int) entity.getLoopTime() / 1000;
                addImageUpdateToWeb(midId, playTime);
            }
        });*/
    }

    @Override
    public void pauseDisplayView() {
        MyLog.cdl("====按键事件======pauseDisplayView=====");
        if (helper == null) {
            return;
        }
        helper.stopPlay();
    }

    @Override
    public void moveViewForward(boolean b) {
        MyLog.cdl("====按键事件======moveViewForward=====" + b);
        if (helper == null) {
            return;
        }
        if (b) {
            helper.nextPage();
        } else {
            helper.prevPage();
        }
    }

    @Override
    public void resumePlayView() {
        MyLog.cdl("====按键事件=====resumePlayView======");
        if (helper == null) {
            return;
        }
        helper.startPlay();
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        initView(view);
    }

    @Override
    public void playComplet() {
        if (listener != null) {
            listener.playComplete(TaskPlayStateListener.TAG_PLAY_PICTURE);
        }
    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    private void addImageUpdateToWeb(String midId, int timeDisplay) {
        String resourType = AppInfo.VIEW_IMAGE;
        long saveTime = System.currentTimeMillis();
        StatisticsEntity statisticsEntity = new StatisticsEntity(midId, resourType, timeDisplay, 1, saveTime);
        DbStatiscs.saveStatiseToLocal(statisticsEntity, "图片banner添加统计");
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {
        if (helper != null) {
            helper.stopPlay();
            helper = null;
        }
        if (imageList != null) {
            imageList.clear();
        }
        GlideCacheUtil.getInstance().clearImageAllCache(context);
    }

    @Override
    public void removeCacheView(String tag) {
    }

}
