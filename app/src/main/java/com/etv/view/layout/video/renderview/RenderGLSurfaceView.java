package com.etv.view.layout.video.renderview;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * @ProjectName: TheSimpllestplayer
 * @Package: com.yw.thesimpllestplayer.renderview
 * @ClassName: RenderGLSurfaceView
 * @Description: 封装GLSurfaceView
 * @Author: wei.yang
 * @CreateDate: 2021/11/6 16:30
 * @UpdateUser: 更新者：wei.yang
 * @UpdateDate: 2021/11/6 16:30
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class RenderGLSurfaceView extends GLSurfaceView {
    private VideoDrawer videoDrawer;
    private VideoRender videoRender;

    public RenderGLSurfaceView(Context context) {
        this(context, null);
    }

    public RenderGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initRender();
    }

    /**
     * 初始化渲染器
     */
    public void initRender() {
        Log.e("CDL", "=====initRender====111===");
        setEGLContextClientVersion(2);
        //初始化绘制器
        videoDrawer = new VideoDrawer();
        videoDrawer.setVideoSize(1080, 1920);
        //初始化渲染器
        videoRender = new VideoRender();
        videoRender.addDrawer(videoDrawer);
        setRenderer(videoRender);
    }

    public VideoDrawer getVideoDrawer() {
        return videoDrawer;
    }

    public VideoRender getVideoRender() {
        return videoRender;
    }

    public void setListener(OnSurfaceListener listener) {
        videoRender.setListener(listener);
    }

    public interface OnSurfaceListener{
        void onSurfaceCreated();
    }
}

