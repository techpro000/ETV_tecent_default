//package com.etv.util.down;
//
//import com.etv.task.db.DBTaskUtil;
//import com.etv.task.entity.CpListEntity;
//import com.etv.task.entity.MpListEntity;
//import com.etv.task.entity.PmListEntity;
//import com.etv.task.entity.SceneEntity;
//import com.etv.task.entity.TaskWorkEntity;
//import com.etv.task.entity.TextInfo;
//import com.etv.util.MyLog;
//
//import java.util.List;
//
///**
// * 保存服务器数据到数据库线程
// */
//public class SaveDataRunnable implements Runnable {
//
//    private SaveDateToDbListener listener;
//    private List<TaskWorkEntity> listTask;
//
//    public SaveDataRunnable(List<TaskWorkEntity> listTask, SaveDateToDbListener saveDateToDbListener) {
//        this.listener = saveDateToDbListener;
//        this.listTask = listTask;
//    }
//
//    @Override
//    public void run() {
//        if (listTask == null || listTask.size() < 1) {
//            listener.saveDataToDbOk(false);
//            MyLog.db("listTask == null");
//            return;
//        }
//        //耗时操作。需要等清理结束之后再进行接下来操作
//        DBTaskUtil.clearAllDbInfo("下载完数据到本地， 清理数据库数据");
//        for (int i = 0; i < listTask.size(); i++) {
//            TaskWorkEntity taskWorkEntity = listTask.get(i);
//            //解析保存任务到数据库
//            boolean isSaveTask = DBTaskUtil.saveTaskEntity(taskWorkEntity);
//            MyLog.db("保存任务到数据库==" + isSaveTask + " / " + taskWorkEntity.toString());
//            if (isSaveTask) {
//                String taskId = taskWorkEntity.getTaskId();
//                savePmListInfo(taskWorkEntity, taskId);
//            }
//        }
//        listener.saveDataToDbOk(true);
//    }
//
//    private void savePmListInfo(TaskWorkEntity taskWorkEntity, String taskId) {
//        List<PmListEntity> pmListEntities = taskWorkEntity.getPmListEntities();
//        if (pmListEntities == null || pmListEntities.size() < 1) {
//            return;
//        }
//        for (int j = 0; j < pmListEntities.size(); j++) {
//            PmListEntity pmListEntity = pmListEntities.get(j);
//            boolean saveProjectorEntity = DBTaskUtil.saveProjectorEntity(pmListEntity);
//            MyLog.db("保存节目到数据库==" + saveProjectorEntity + " / " + pmListEntity.toString());
//            if (saveProjectorEntity) {
//                saveSceneListInfo(pmListEntity);
//            } else {
//                DBTaskUtil.delTaskById(taskId, "解析节目失败，直接删除当前任务信息");
//            }
//        }
//    }
//
//    private void saveSceneListInfo(PmListEntity pmListEntity) {
//        List<SceneEntity> sceneEntityList = pmListEntity.getSceneEntityList();
//        if (sceneEntityList == null || sceneEntityList.size() < 1) {
//            return;
//        }
//        for (int k = 0; k < sceneEntityList.size(); k++) {
//            SceneEntity sceneEntity = sceneEntityList.get(k);
//            String senceId = sceneEntity.getSenceId();
//            boolean isSaveSencen = DBTaskUtil.saveSenceEntity(sceneEntity);
//            MyLog.db("保存场景到数据库==" + isSaveSencen + " / " + sceneEntity.toString());
//            if (isSaveSencen) {
//                saveCpListInfoToDb(sceneEntity, senceId);
//            } else {
//                DBTaskUtil.delSenceByProId(senceId);
//            }
//        }
//    }
//
//    private void saveCpListInfoToDb(SceneEntity sceneEntity, String senceId) {
//        List<CpListEntity> listCp = sceneEntity.getListCp();
//        if (listCp == null || listCp.size() < 1) {
//            return;
//        }
//        for (int l = 0; l < listCp.size(); l++) {
//            CpListEntity cpListEntity = listCp.get(l);
//            boolean isSave = DBTaskUtil.saveCpEntity(cpListEntity);
//            MyLog.db("保存控件到数据库==" + isSave + " / " + cpListEntity.toString());
//            if (!isSave) {
//                DBTaskUtil.delSenceByProId(senceId);
//                return;
//            }
//            List<MpListEntity> mpList = cpListEntity.getMpList();
//            saveMediaListToDb(mpList);
//            List<TextInfo> txList = cpListEntity.getTxList();
//            saveTextInfoTodB(txList);
//        }
//    }
//
//    private void saveMediaListToDb(List<MpListEntity> mpList) {
//        if (mpList == null || mpList.size() < 1) {
//            return;
//        }
//        for (int m = 0; m < mpList.size(); m++) {
//            MpListEntity mpListEntity = mpList.get(m);
//            boolean saveMpInfo = DBTaskUtil.saveMpInfo(mpListEntity);
//            MyLog.db("保存素材信息： " + saveMpInfo + " / " + mpListEntity);
//        }
//    }
//
//    private void saveTextInfoTodB(List<TextInfo> txList) {
//        if (txList == null || txList.size() < 1) {
//            return;
//        }
//        for (int m = 0; m < txList.size(); m++) {
//            TextInfo textInfo = txList.get(m);
//            boolean saveTxtInfo = DBTaskUtil.saveTxtInfo(textInfo);
//            MyLog.db("保存文本信息： " + saveTxtInfo + " / " + textInfo);
//        }
//    }
//}
