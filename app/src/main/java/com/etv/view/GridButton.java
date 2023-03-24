package com.etv.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ys.etv.R;
import com.ys.model.entity.FileEntity;
import com.ys.model.util.FileMatch;
import com.etv.util.NetWorkUtils;


/***
 * 手机端topBar统一类型
 *
 * @author Administrator
 */
public class GridButton extends RelativeLayout {

    private static final int CORLOR_BLUE = 0XFF5cabfa;
    private static final int CORLOR_WHITE = 0XFFFFFFFF;

    public GridButton(Context context) {
        this(context, null);
    }

    public GridButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private int bgg_corlor = CORLOR_BLUE;
    String tvContent = "";
    int imageContent;
    int imgSize;
    View view;
    ImageView iv_content;
    TextView tv_content;
    TextView tv_file_length;
    TextView tv_receive_progress, tv_has_receive;
    ProgressBar progress_receiver;

    public GridButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Grid_Button);
        bgg_corlor = a.getColor(R.styleable.Grid_Button_gridBackColor, CORLOR_BLUE);
        tvContent = a.getString(R.styleable.Grid_Button_gridtitle);
        imageContent = a.getResourceId(R.styleable.Grid_Button_gridimgContent, R.mipmap.file_icon_default);
        imgSize = (int) a.getDimension(R.styleable.Grid_Button_gridimgSize, 80);
        view = LayoutInflater.from(context).inflate(R.layout.view_grid, null);
        iv_content = (ImageView) view.findViewById(R.id.iv_content);
        tv_content = (TextView) view.findViewById(R.id.tv_content);
        tv_file_length = (TextView) view.findViewById(R.id.tv_file_length);
        tv_has_receive = (TextView) view.findViewById(R.id.tv_has_receive);

        tv_receive_progress = (TextView) view.findViewById(R.id.tv_receive_progress);
        progress_receiver = (ProgressBar) view.findViewById(R.id.progress_receiver);
        tv_receive_progress.setText("");
        tv_has_receive.setText("");
        tv_content.setText("Prepare to receive documents");
        tv_file_length.setText("0 KB");
        iv_content.setBackgroundResource(imageContent);
        addView(view);
        a.recycle();
    }

    /***
     *
     * @param fileName
     * @param fileByte
     * 文件总大小
     * @param fileReceive
     * 已经接收的大小
     * 接收的
     */
    public void updateProgress(String fileName, long fileReceive, long fileByte) {
        setBackGround(fileName);
        tv_content.setText("Name : " + fileName);
        String fileLength = NetWorkUtils.bytes2kb(fileByte);
        tv_file_length.setText("Size : " + fileLength);

        int progressNum = (int) (fileReceive * 100 / fileByte);
        progress_receiver.setProgress(progressNum);
        tv_has_receive.setText(NetWorkUtils.bytes2kb(fileReceive));
        tv_receive_progress.setText(progressNum + "%");
    }

    public void resetState() {
        uopdateImage(R.mipmap.file_icon_default);
        tv_content.setText("Prepare to receive documents");
        tv_file_length.setText("0 KB");
        progress_receiver.setProgress(0);
        tv_has_receive.setText("");
        tv_receive_progress.setText("");
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            layout(l, t, r, b);
        }
    }

    public void setBackGround(String fileName) {
        int fileStyle = FileMatch.fileMatch(fileName);
        if (fileStyle == FileEntity.STYLE_FILE_IMAGE) {
            uopdateImage(R.mipmap.file_icon_image);
        } else if (fileStyle == FileEntity.STYLE_FILE_MUSIC) {
            uopdateImage(R.mipmap.file_icon_music);
        } else if (fileStyle == FileEntity.STYLE_FILE_VIDEO) {
            //这里改成默认的图标，不获取缩略图，因为rmvb格式的获取不到图片，会显示为空
            uopdateImage(R.mipmap.file_icon_video);
        } else if (fileStyle == FileEntity.STYLE_FILE_ZIP) {  //zip格式
            uopdateImage(R.mipmap.file_icon_zip);
        } else if (fileStyle == FileEntity.STYLE_FILE_APK) {  //apk格式
            uopdateImage(R.mipmap.icon_file_apk);
        } else if (fileStyle == FileEntity.STYLE_FILE_PPT) {  //ppt格式
            uopdateImage(R.mipmap.file_icon_ppt);
        } else if (fileStyle == FileEntity.STYLE_FILE_DOC) {  //word格式
            uopdateImage(R.mipmap.file_icon_word);
        } else if (fileStyle == FileEntity.STYLE_FILE_EXCEL) {  //excel格式
            uopdateImage(R.mipmap.file_icon_excel);
        } else if (fileStyle == FileEntity.STYLE_FILE_PDF) {  //pdf格式
            uopdateImage(R.mipmap.file_icon_pdf);
        } else if (fileStyle == FileEntity.STYLE_FILE_TXT) {  //txt格式
            uopdateImage(R.mipmap.file_icon_txt);
        } else if (fileStyle == FileEntity.STYLE_FILE_OTHER) {  //其他类型格式
            uopdateImage(R.mipmap.file_icon_default);
        } else {
            uopdateImage(R.mipmap.file_icon_default);
        }
    }

    public void uopdateImage(int imageId) {
        iv_content.setBackgroundResource(imageId);
    }

}
