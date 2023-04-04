package com.etv.util.down;

import com.etv.task.db.DBTaskUtil;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MpListEntity;
import com.etv.task.entity.PmListEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TaskWorkEntity;
import com.etv.task.entity.TextInfo;
import com.etv.util.MyLog;

import java.util.List;

/**
 * 保存服务器数据到数据库线程
 */
public class SaveDataRunnable implements Runnable{


    private SaveDateToDbListener listener;
    private List<TaskWorkEntity> listTask;
    private boolean isDownFileComplete;

    public SaveDataRunnable(boolean isDownFileComplete, List<TaskWorkEntity> listTask, SaveDateToDbListener saveDateToDbListener) {
        this.listener = saveDateToDbListener;
        this.listTask = listTask;
        this.isDownFileComplete = isDownFileComplete;
    }

    @Override
    public void run() {

        if(!isDownFileComplete) {
            MyLog.d("liujk", "下载文件过程中有问题文件，此次下载失败");
            return;
        }

        // 清理数据库
        //MyLog.playTask("clearAllDbInfo 10");
        //耗时操作。需要等清理结束之后再进行接下来操作
        MyLog.playTask("==== SaveDataRunnable step1 ");
        DBTaskUtil.clearAllDbInfo("下载完数据到本地， 清理数据库数据");

        MyLog.playTask("==== SaveDataRunnable step2");

        for (int i = 0; i < listTask.size(); i++) {
            TaskWorkEntity taskWorkEntity = listTask.get(i);

            //解析保存任务到数据库
            boolean isSaveTask = DBTaskUtil.saveTaskEntity(taskWorkEntity);
            String taskId = taskWorkEntity.getTaskId();
            if(isSaveTask) {
                List<PmListEntity> pmListEntities = taskWorkEntity.getPmListEntities();
                for (int j = 0; j < pmListEntities.size(); j++) {
                    PmListEntity pmListEntity = pmListEntities.get(j);

                    boolean saveProjectorEntity = DBTaskUtil.saveProjectorEntity(pmListEntity);

                    if(saveProjectorEntity) {

                        List<SceneEntity> sceneEntityList = pmListEntity.getSceneEntityList();
                        for (int k = 0; k < sceneEntityList.size(); k++) {
                            SceneEntity sceneEntity = sceneEntityList.get(k);
                            String senceId = sceneEntity.getSenceId();
                            boolean isSaveSencen = DBTaskUtil.saveSenceEntity(sceneEntity);

                            if(isSaveSencen) {
                                List<CpListEntity> listCp = sceneEntity.getListCp();
                                for (int l = 0; l < listCp.size(); l++) {
                                    CpListEntity cpListEntity = listCp.get(l);
                                    boolean isSave = DBTaskUtil.saveCpEntity(cpListEntity);
                                    if(!isSave) {
                                        DBTaskUtil.delSenceByProId(senceId);
                                        listener.saveDataToDbOk(false);
                                        return;
                                    }

                                    List<MpListEntity> mpList = cpListEntity.getMpList();
                                    List<TextInfo> txList = cpListEntity.getTxList();

                                    for (int m = 0; m < mpList.size(); m++) {
                                        MpListEntity mpListEntity = mpList.get(m);
                                        boolean saveMpInfo = DBTaskUtil.saveMpInfo(mpListEntity);

                                        if(!saveMpInfo) {
                                            listener.saveDataToDbOk(false);
                                            return;
                                        }
                                    }

                                    for (int m = 0; m < txList.size(); m++) {
                                        TextInfo textInfo = txList.get(m);
                                        boolean saveTxtInfo = DBTaskUtil.saveTxtInfo(textInfo);
                                        MyLog.db("保存文本信息： " + textInfo.toString() + " 保存文本信息是否成功： " + (saveTxtInfo ? "成功" : " 失败"));
                                        if(!saveTxtInfo) {
                                            listener.saveDataToDbOk(false);
                                            return;
                                        }

                                    }
                                }
                            }else {
                                DBTaskUtil.delSenceByProId(senceId);
                                listener.saveDataToDbOk(false);
                                return;
                            }


                        }
                    }else {
                        DBTaskUtil.delTaskById(taskId, "解析节目失败，直接删除当前任务信息");
                        listener.saveDataToDbOk(false);
                        return;
                    }


                }
            }

        }
        listener.saveDataToDbOk(true);
    }
}
