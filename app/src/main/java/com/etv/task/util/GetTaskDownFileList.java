package com.etv.task.util;

import com.etv.config.ApiInfo;
import com.etv.config.AppInfo;
import com.etv.db.DbFontInfo;
import com.etv.entity.FontEntity;
import com.etv.task.db.DBTaskUtil;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MpListEntity;
import com.etv.task.entity.PmListEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TaskDownEntity;
import com.etv.task.entity.TaskWorkEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.model.TaskGetDownListListener;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class GetTaskDownFileList {

    public GetTaskDownFileList() {

    }

    public void getTaskDownFileList(List<TaskWorkEntity> taskWorkEntities, TaskGetDownListListener listListener) {
        MyLog.taskDown("====000开始检索需要下载得素材====");
        if (taskWorkEntities == null || taskWorkEntities.size() < 1) {
            MyLog.taskDown("====000没有需要下载得资源，直接返回==taskWorkEntities==00==");
            listListener.getTaskDownFileListFromDb(null);
            return;
        }
        Observable.just(taskWorkEntities).map(new Function<List<TaskWorkEntity>, List<TaskDownEntity>>() {
                    @Override
                    public List<TaskDownEntity> apply(List<TaskWorkEntity> taskWorkEntities) throws Exception {
                        return getTaskDownFileListFromList(taskWorkEntities);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<TaskDownEntity>>() {
                    @Override
                    public void accept(List<TaskDownEntity> taskDownEntities) throws Exception {
                        listListener.getTaskDownFileListFromDb(taskDownEntities);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        listListener.getTaskDownFileListFromDb(null);
                    }
                });
    }

    //用来封装下载列表的，返回主界面得
    private List<TaskDownEntity> downUrlList = new ArrayList<TaskDownEntity>();

    private List<TaskDownEntity> getTaskDownFileListFromList(List<TaskWorkEntity> taskWorkEntities) {
        downUrlList.clear();
        for (TaskWorkEntity taskWorkEntity : taskWorkEntities) {
            List<PmListEntity> pmListEntities = taskWorkEntity.getPmListEntities();
            if (pmListEntities == null || pmListEntities.size() < 1) {
                continue;
            }
            for (PmListEntity pmListEntity : pmListEntities) {
                List<SceneEntity> sceneEntityList = pmListEntity.getSceneEntityList();
                if (sceneEntityList == null || sceneEntityList.size() < 1) {
                    continue;
                }
                //添加场景背景image
                addBackImageToDownList(sceneEntityList);
                for (SceneEntity sceneEntity : sceneEntityList) {
                    List<CpListEntity> listCp = sceneEntity.getListCp();
                    if (listCp == null || listCp.size() < 1) {
                        continue;
                    }
                    for (CpListEntity cpList : listCp) {
                        List<MpListEntity> mpListEntities = cpList.getMpList();
                        if (mpListEntities == null || mpListEntities.size() < 1) {
                            continue;
                        }
                        addMpEntityToList(mpListEntities);
                        for (MpListEntity mpListEntity : mpListEntities) {
                            MyLog.taskDown("获取素材信息==" + mpListEntity.toString());
                        }
                    }
                }
            }
        }
        //添加背景图
//        List<SceneEntity> sceneEntityList = DBTaskUtil.getSencenListInfoAll();
//        if (sceneEntityList != null || sceneEntityList.size() > 0) {
//            addBackImageToDownList(sceneEntityList);
//        }
        //添加字体下载
        List<TextInfo> textInfoList = DBTaskUtil.getTxtListInfoAll();
        if (textInfoList != null || textInfoList.size() > 0) {
            addFontDownInfoToList(textInfoList);
        }
        return downUrlList;
    }

    private void addMpEntityToList(List<MpListEntity> mpListEntities) {
        if (mpListEntities == null || mpListEntities.size() < 1) {
            return;
        }
        for (int i = 0; i < mpListEntities.size(); i++) {
            MpListEntity mpListEntity = mpListEntities.get(i);
            if (mpListEntity == null) {
                continue;
            }
            String md5 = mpListEntity.getMd5();
            String downUrl = mpListEntity.getUrl();
            String fileLength = mpListEntity.getSize();
            String downName = downUrl.substring(downUrl.lastIndexOf("/") + 1, downUrl.length());
            String saveFilePath = AppInfo.BASE_TASK_URL() + "/" + downName;
            boolean isFileExice = FileUtil.ifFileHasExict(saveFilePath, fileLength);
            if (!isFileExice) {
                String taskId = mpListEntity.getTaskId();
                TaskDownEntity entitySave = new TaskDownEntity(taskId, downUrl, saveFilePath, fileLength, isFileExice, md5);
                addDownTaskFileToList(entitySave, "添加素材到集合");
            }
        }
    }

    private void addDownTaskFileToList(TaskDownEntity entity, String printTag) {
        MyLog.taskDown("=addDownTaskFileToList=" + printTag);
        if (entity == null) {
            return;
        }
        if (downUrlList == null) {
            downUrlList = new ArrayList<>();
        }
        if (downUrlList.size() < 1) {
            downUrlList.add(entity);
            return;
        }
        String addDownUrl = entity.getDownUrl().trim();
        boolean isHasSave = false;
        for (int i = 0; i < downUrlList.size(); i++) {
            String downUrl = downUrlList.get(i).getDownUrl().trim();
            if (downUrl.equals(addDownUrl)) {
                isHasSave = true;
            }
        }
        if (!isHasSave && !entity.isDownOver()) {
            MyLog.taskDown("==========需要下载的文件这里统计===" + entity.toString());
            downUrlList.add(entity);
        }
    }

    private void addBackImageToDownList(List<SceneEntity> sceneEntityList) {
        if (sceneEntityList == null || sceneEntityList.size() < 1) {
            return;
        }
        for (int i = 0; i < sceneEntityList.size(); i++) {
            SceneEntity senceEntity = sceneEntityList.get(i);
            if (senceEntity == null) {
                continue;
            }
            String backFilePath = senceEntity.getScBackImg();
            if (backFilePath == null || backFilePath.length() < 5) {
                continue;
            }
            //拼接背景得下载地址
            if (!backFilePath.startsWith("http")) {
                backFilePath = ApiInfo.getFileDownUrl() + "/" + backFilePath;
            }
            String downName = backFilePath.substring(backFilePath.lastIndexOf("/") + 1, backFilePath.length());
            String saveFilePath = AppInfo.BASE_TASK_URL() + "/" + downName;
            String fileLength = senceEntity.getScBackimgSize();
            boolean isFileExice = FileUtil.ifFileHasExict(saveFilePath, fileLength);
            MyLog.taskDown("=====图片的存储===背景是否存在==" + isFileExice + " / " + saveFilePath);
            if (!isFileExice) {
                String taskId = senceEntity.getTaskid();
                TaskDownEntity entitySave = new TaskDownEntity(taskId, backFilePath, saveFilePath, fileLength, isFileExice, "");
                addDownTaskFileToList(entitySave, "场景背景图");
            }
        }
    }

    /***
     * 增加需要下载得字体
     * @param textInfoList
     */
    private void addFontDownInfoToList(List<TextInfo> textInfoList) {
        if (textInfoList == null || textInfoList.size() < 1) {
            return;
        }
        try {
            for (int i = 0; i < textInfoList.size(); i++) {
                TextInfo textInfo = textInfoList.get(i);
                String taskId = textInfo.getTaskId();
                String taBgImagePath = textInfo.getTaBgImage().toString();
                if (taBgImagePath != null && taBgImagePath.length() > 2) {
                    String downPath = ApiInfo.getFileDownUrl() + "/" + taBgImagePath;
                    String saveFilePath = TaskDealUtil.getSavePath(taBgImagePath);
                    String fileLength = textInfo.getTaBgimageSize();
                    boolean isFileExice = FileUtil.ifFileHasExict(saveFilePath, fileLength);
                    MyLog.taskDown("======buttom==文件师傅存在===" + isFileExice);
                    if (!isFileExice) {
                        TaskDownEntity entitySave = new TaskDownEntity(taskId, downPath, saveFilePath, fileLength, isFileExice, "");
                        addDownTaskFileToList(entitySave, "添加button背景图片");
                    }
                }
                String fontType = textInfo.getTaFonType();
                if (fontType == null || fontType.length() < 1) {
                    continue;
                }
                int fontTypeNum = Integer.parseInt(fontType);
                if (fontTypeNum == 1) {
                    continue;
                }
                FontEntity fontEntity = DbFontInfo.getFontInfoById(fontType);
                if (fontEntity != null) {
                    String fileName = fontEntity.getDownName();
                    String saveFilePath = AppInfo.BASE_FONT_PATH() + "/" + fileName;
                    String fileLength = fontEntity.getFontSize() + "";
                    String downPath = fontEntity.getFontDownUrl();
                    boolean isFileExice = FileUtil.ifFileHasExict(saveFilePath, fileLength);
                    MyLog.taskDown("=====检测字体属性下载==" + isFileExice + " / " + saveFilePath);
                    if (!isFileExice) {
                        TaskDownEntity entitySave = new TaskDownEntity(taskId, downPath, saveFilePath, fileLength, isFileExice, "");
                        addDownTaskFileToList(entitySave, "添加字体到集合");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
