package com.etv.task.util;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.etv.config.AppInfo;
import com.etv.service.EtvService;
import com.etv.task.db.DBTaskUtil;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.MpListEntity;
import com.etv.task.entity.PmListEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TaskWorkEntity;
import com.etv.task.entity.TextInfo;
import com.etv.task.model.TaskRequestListener;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SimpleDateUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/***
 * 任务解析
 */
public class ParsenerJsonRunnable implements Runnable {

    private String json;
    private TaskRequestListener taskRequestListener;
    private boolean isParsenerNet;  //是否是网络模式
    private Handler handlerjson = new Handler();

    public ParsenerJsonRunnable(boolean isParsenerNet, String json, TaskRequestListener taskRequestListener) {
        this.json = json;
        this.taskRequestListener = taskRequestListener;
        this.isParsenerNet = isParsenerNet;
    }

    @Override
    public void run() {
        if (isParsenerNet) {  //网络模式节目解析
            if (json == null || json.length() < 8) {
                DBTaskUtil.clearAllDbInfo("请求得任务信息，JSON IS NULL 没有任务,直接删库");
                taskRequestListener.parserJsonOver("网络任务 = 解析异常,没有相关的 JSON 数据", null);
                return;
            }
            List<TaskWorkEntity> taskWorkEntityList = parsenerTaskEntity(json);
            if (taskWorkEntityList == null || taskWorkEntityList.size() < 1) {
                //表示当前没有节目，直接删库，删除素材信息，停止播放
                MyLog.task("=请求得任务信息，没有任务,直接删库==");
                DBTaskUtil.clearAllDbInfo("请求得任务信息，没有任务,直接删库");
            }
            taskRequestListener.parserJsonOver("解析数据完成", taskWorkEntityList);
            return;
        }
        MyLog.task("开始解析离线任务");
        //离线单机任务
        if (json == null || json.length() < 8) {
            MyLog.task("离线任务=没有数据");
            taskRequestListener.parserJsonOver("单机软件 = 解析异常,没有相关的 JSON 数据", null);
            return;
        }
        List<TaskWorkEntity> taskWorkEntityList = parsenerSingleTaskEntity(json);
        MyLog.task("离线任务=解析完毕");
        taskRequestListener.parserJsonOver("解析数据完成", taskWorkEntityList);
    }

    /***
     * 解析网络任务资源
     * @param data
     */
    public List<TaskWorkEntity> parsenerTaskEntity(String data) {
        List<TaskWorkEntity> taskWorkEntityList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            int lengthArray = jsonArray.length();
            MyLog.task("=====任务的数据==" + lengthArray);
            if (lengthArray < 1) {
                finishMySelf("未获取到任务");
                return taskWorkEntityList;
            }
            //如果解析到节目数据，清理数据库，重新保存
            for (int i = 0; i < lengthArray; i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                String etLevel = jsonData.getString("etLevel"); //1替换 2追加 3插播  4同步  5触发
                Log.e("TAG", "parsenerTaskEntity: " + etLevel);
                if (etLevel.contains(AppInfo.TASK_PLAY_PLAY_SAME)) {
                    MyLog.task("=====任务的数据=当前同步关闭直接跳过任务=");
                    continue;
                }
                String taskid = jsonData.getString("id");
                String etName = jsonData.getString("etName");
                String startDate = jsonData.getString("etStartDate");
                String startTime = jsonData.getString("etStartTime");
//              修改任务的起始时间
                if (etLevel.equals(AppInfo.TASK_PLAY_REPLACE)) {
                    startDate = "1990-01-01";
                    startTime = jsonData.getString("etStartTime");
                }
                MyLog.task("parsenerTaskEntity: " + startDate + "/" + startTime);
                String etTaskType = jsonData.getString("etTaskType");  // 1普通节目任务 2双屏异显任务
                String endDate = jsonData.getString("etEndDate");
                String endTime = jsonData.getString("etEndTime");
                String sendTime = jsonData.getString("etSendTime");
                String etMon = jsonData.getString("etMon");
                String etTue = jsonData.getString("etTue");
                String etWed = jsonData.getString("etWed");
                String etThur = jsonData.getString("etThur");
                String etFri = jsonData.getString("etFri");
                String etSat = jsonData.getString("etSat");
                String etSun = jsonData.getString("etSun");
                String streamIdOne = jsonData.optString("streamIdOne");
                String streamIdTwo = jsonData.optString("streamIdTwo");
                int etIsLinkScreeen = 1;   //是否联动
                if (jsonData.toString().contains("etIsLinkScreeen")) {
                    String linkScreen = jsonData.getString("etIsLinkScreeen").trim();
                    if (linkScreen != null && linkScreen.length() > 0) {
                        etIsLinkScreeen = Integer.parseInt(linkScreen);
                    }
                }
                TaskWorkEntity workEntity = new TaskWorkEntity();
                workEntity.setStreamIdOne(streamIdOne);
                workEntity.setStreamIdTwo(streamIdTwo);
                workEntity.setEtIsLinkScreeen(etIsLinkScreeen);
                workEntity.setTaskId(taskid);
                workEntity.setEtName(etName);
                workEntity.setStartDate(startDate);
                workEntity.setEndDate(endDate);
                workEntity.setStartTime(startTime);
                workEntity.setEndTime(endTime);
                workEntity.setEtMon(etMon);
                workEntity.setEtTue(etTue);
                workEntity.setEtWed(etWed);
                workEntity.setEtThur(etThur);
                workEntity.setEtFri(etFri);
                workEntity.setEtSat(etSat);
                workEntity.setEtSun(etSun);
                workEntity.setEtLevel(etLevel);
                workEntity.setEtTaskType(etTaskType);
                workEntity.setSendTime(SimpleDateUtil.StringToLongTime(sendTime));
                //获取任务的结束日期
                long endDateTask = SimpleDateUtil.formatStringtoDate(endDate);
                long endTimeTask = SimpleDateUtil.formatStringTime(endTime);
                long currentDate = SimpleDateUtil.getCurrentDateLong();
                long currentHoMin = SimpleDateUtil.getCurrentHourMinSecond();
                MyLog.task("===解析任务属性=" + workEntity.toString());
                if (endDateTask < currentDate) { //过期的任务
                    MyLog.task("=====当前任务的结束时间在当前时候之前，删除任务");
                    delTaskByIdOrParsenError("111当前任务的结束时间在当前时候之前，删除任务", taskid);
                    continue;
                }
                if (endDateTask == currentDate) {  //当天的任务，就比任务的结束时间
                    if (endTimeTask < currentHoMin) {
                        MyLog.task("=====当前任务的结束时间在当前时候之前，删除任务");
                        delTaskByIdOrParsenError("000当前任务的结束时间在当前时候之前，删除任务", taskid);
                        continue;
                    }
                }
                //保存成功并且是普通任务
                String pmList = jsonData.getString("pmList");
                MyLog.task("=====节目列表=" + pmList);
                List<PmListEntity> pmListEntityList = parsenerPmList(taskid, etLevel, pmList, etIsLinkScreeen);
                workEntity.setPmListEntities(pmListEntityList);
                taskWorkEntityList.add(workEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return taskWorkEntityList;
    }

    /***
     * 解析单个任务
     * 如果石是U盘，直接解析单个
     * 如果是网络的，循环便利此方法即可
     * @param data
     */
    public List<TaskWorkEntity> parsenerSingleTaskEntity(String data) {
        List<TaskWorkEntity> taskWorkEntityList = new ArrayList<>();
        try {
            if (data == null || TextUtils.isEmpty(data)) {
                finishMySelf("JSON异常，解析停止");
                return null;
            }
            JSONObject jsonData = new JSONObject(data);
            String taskid = jsonData.getString("id");
            String etName = jsonData.getString("etName");
            String etLevel = jsonData.getString("etLevel"); //1替换 2追加 3插播
            String etTaskType = jsonData.getString("etTaskType");  // 1普通节目任务 2双屏异显任务
            String startDate = "2000-01-01";        //开始日期
            String startTime = "00:00:00";          //开始时间
            String endDate = "2100-01-01";          //结束日期
            String endTime = "23:59:59";            //单机时间
            String etMon = jsonData.getString("etMon");
            String etTue = jsonData.getString("etTue");
            String etWed = jsonData.getString("etWed");
            String etThur = jsonData.getString("etThur");
            String etFri = jsonData.getString("etFri");
            String etSat = jsonData.getString("etSat");
            String etSun = jsonData.getString("etSun");
            int etIsLinkScreeen = 1;   //是否联动
            if (jsonData.toString().contains("etIsLinkScreeen")) {
                String linkScreen = jsonData.getString("etIsLinkScreeen").trim();
                if (linkScreen != null && linkScreen.length() > 0) {
                    etIsLinkScreeen = Integer.parseInt(linkScreen);
                }
            }
            TaskWorkEntity workEntity = new TaskWorkEntity();
            workEntity.setEtIsLinkScreeen(etIsLinkScreeen);
            workEntity.setTaskId(taskid);
            workEntity.setEtName(etName);
            workEntity.setStartDate(startDate);
            workEntity.setEndDate(endDate);
            workEntity.setStartTime(startTime);
            workEntity.setEndTime(endTime);
            workEntity.setEtMon(etMon);
            workEntity.setEtTue(etTue);
            workEntity.setEtWed(etWed);
            workEntity.setEtThur(etThur);
            workEntity.setEtFri(etFri);
            workEntity.setEtSat(etSat);
            workEntity.setEtSun(etSun);
            workEntity.setEtLevel(etLevel);
            workEntity.setEtTaskType(etTaskType);
            workEntity.setSendTime(SimpleDateUtil.StringToLongTime("2019-07-01 11:50:03"));
            //保存成功并且是普通任务
            String pmList = jsonData.optString("pmList");
            MyLog.task("=====节目列表=" + pmList);
            List<PmListEntity> pmListEntityList = parsenerPmList(taskid, etLevel, pmList, etIsLinkScreeen);
            workEntity.setPmListEntities(pmListEntityList);
            taskWorkEntityList.add(workEntity);
        } catch (Exception e) {
            MyLog.ExceptionPrint("解析任务error: " + e.toString());
            e.printStackTrace();
        }
        return taskWorkEntityList;
    }

    private void finishMySelf(final String desc) {
        if (taskRequestListener == null) {
            return;
        }
        handlerjson.post(new Runnable() {
            @Override
            public void run() {
                taskRequestListener.finishMySelf(desc);
            }
        });
    }


    /***
     * 解析节目
     * @param taskid
     * 任务ID
     * @param pmList
     * 节目信息
     */
    private List<PmListEntity> parsenerPmList(String taskid, String etLevel, String pmList, int etIsLinkScreeen) {
        List<PmListEntity> pmListEntityList = new ArrayList<>();
        try {
            pmList = pmList.trim();
            if (TextUtils.isEmpty(pmList) || pmList.length() < 3) { //获取的数据为null
                delTaskByIdOrParsenError("解析节目获取的数据为null", taskid);
                finishMySelf("当前没有节目");
                return pmListEntityList;
            }
            JSONArray jsonArray = new JSONArray(pmList);
            int lengthArray = jsonArray.length();
            if (lengthArray < 1) {
                delTaskByIdOrParsenError("解析的节目数组==null", taskid);
                finishMySelf("当前没有节目");
                return pmListEntityList;
            }
            for (int i = 0; i < lengthArray; i++) {
                JSONObject jsonpnlist = jsonArray.getJSONObject(i);
                String proid = jsonpnlist.getString("id");                 //节目ID
                String pmName = jsonpnlist.getString("pmName");            //节目名称
                String pmType = jsonpnlist.getString("pmType");            //节目类型
                if (!SharedPerManager.getTaskTouchEnable() && pmType.contains(AppInfo.PROGRAM_TOUCH)) {
                    MyLog.task("=====任务的数据=当前互动关闭直接跳过任务=");
                    delTaskByIdOrParsenError("当前互动关闭直接跳过任务", taskid);
                    continue;
                }
                String displayPos = jsonpnlist.getString("displayPos");    //显示在那一块屏幕上
                int pmResolutionType = 1;   // 屏幕类型  1：分辨率  2：自适应  3：4K
                if (jsonpnlist.toString().contains("pmResolutionType")) {
                    pmResolutionType = jsonpnlist.optInt("pmResolutionType", 2);
                }
                int pmFixedScreen = 1;        // 1横屏 2 竖屏
                if (jsonpnlist.toString().contains("pmFixedScreen")) {
                    pmFixedScreen = jsonpnlist.optInt("pmFixedScreen", 1);
                }
//                if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
//                    pmResolutionType = 2;
//                    int screenWidth = SharedPerManager.getScreenWidth();
//                    int screenHeight = SharedPerManager.getScreenHeight();
//                    MyLog.task("======节目保存成功，CPU_MODEL_MTK_M11===" + screenWidth + " / " + screenHeight);
//                    if (screenWidth > screenHeight) {
//                        pmFixedScreen = 1;
//                    } else {
//                        pmFixedScreen = 2;
//                    }
//                }
                PmListEntity entity = new PmListEntity();
                entity.setProId(proid);
                entity.setPmResolutionType(pmResolutionType);  // 屏幕类型
                entity.setPmFixedScreen(pmFixedScreen);      // 1横屏 2 竖屏
                entity.setTaskid(taskid);
                entity.setPmName(pmName);
                entity.setPmType(pmType);
                entity.setDisplayPos(displayPos);
                String sceneList = jsonpnlist.getString("sceneList");
                MyLog.task("======节目保存成功，去解析场景===" + sceneList);
                List<SceneEntity> sceneEntityList = parsenerScenecList(proid, taskid, pmType, etLevel, displayPos, sceneList, etIsLinkScreeen, pmResolutionType, pmFixedScreen);
                entity.setSceneEntityList(sceneEntityList);

                pmListEntityList.add(entity);

            }
        } catch (Exception e) {
            MyLog.ExceptionPrint("解析任务error: " + e.toString());
            e.printStackTrace();
        }

        return pmListEntityList;
    }


    /**
     * 解析场景
     *
     * @param proid     节目ID
     * @param sceneList 场景
     */
    private List<SceneEntity> parsenerScenecList(String proid, String taskid, String pmType, String etLevel, String displayPos, String sceneList, int etIsLinkScreeen, int pmResolutionType, int pmFixedScreen) {

        List<SceneEntity> sceneEntityList = new ArrayList<>();
        MyLog.task("======准备解析场景===" + sceneList);
        try {
            if (sceneList == null || sceneList.length() < 3) {
                finishMySelf("没有场景属性");
                return sceneEntityList;
            }
            JSONArray jsonArray = new JSONArray(sceneList);
            int lengthArray = jsonArray.length();
            if (lengthArray < 1) {
                finishMySelf("当前节目没有场景属性，终端操作");
                return sceneEntityList;
            }
            for (int i = 0; i < lengthArray; i++) {
                JSONObject jsonSenceList = jsonArray.getJSONObject(i);
                String SenceId = jsonSenceList.getString("id");  //场景 ID
                String scBackImg = jsonSenceList.getString("scBackImg");  //背景图
                //这里解决超哥的个人错误。多了一个
                if (scBackImg.startsWith("/")) {
                    scBackImg = scBackImg.substring(scBackImg.indexOf("/") + 1, scBackImg.length());
                }
                String scBackimgSize = jsonSenceList.getString("scBackimgSize");  //文件大小
                String scTime = "0";
                if (jsonSenceList.toString().contains("scTime")) {
                    scTime = jsonSenceList.getString("scTime");
                    if (scTime == null || scTime.length() < 1) {
                        scTime = "0";
                    }
                }
                long currentTime = System.currentTimeMillis();
                SceneEntity sceneEntity = new SceneEntity(SenceId, proid, taskid, scBackImg, scBackimgSize, pmType, displayPos,
                    etLevel, scTime, etIsLinkScreeen, currentTime);
                MyLog.task("=====保存场景属性===" + sceneEntity.toString());
                if (sceneList.contains("componentsList")) {
                    String componentsList = jsonSenceList.getString("componentsList");
                    List<CpListEntity> cpListEntities = parsenerCpList(taskid, pmType, SenceId, componentsList, pmResolutionType, pmFixedScreen);
                    sceneEntity.setListCp(cpListEntities);
                    sceneEntityList.add(sceneEntity);
                }
            }
        } catch (Exception e) {
            MyLog.ExceptionPrint("解析任务error: " + e.toString());
            e.printStackTrace();
        }

        return sceneEntityList;
    }

    /***
     * 解析控件
     *  @param sencenId
     *  场景ID
     * @param cpList
     * 控件集合JSON
     */
    private List<CpListEntity> parsenerCpList(String taskid, String pmType, String sencenId, String cpList, int pmResolutionType, int pmFixedScreen) {
        List<CpListEntity> cpListEntityList = new ArrayList<>();
        try {
            MyLog.task("=====解析控件======" + cpList);
            if (cpList == null || cpList.length() < 5) {
                delSencenByScenId(sencenId);
                return cpListEntityList;
            }
            JSONArray jsonArray = new JSONArray(cpList);
            int lengthArray = jsonArray.length();
            if (lengthArray < 1) {
                delSencenByScenId(sencenId);
                return cpListEntityList;
            }
            for (int i = 0; i < lengthArray; i++) {
                JSONObject jsonCplist = jsonArray.getJSONObject(i);
                String cpid = jsonCplist.getString("id");                    //控件编号
                String coType = jsonCplist.getString("coType");              //控件类型
                String coLeftPosition = jsonCplist.getString("coLeftPosition");      //左边坐标
                String coRightPosition = jsonCplist.getString("coRightPosition");     //顶端坐标
                String coWidth = jsonCplist.getString("coWidth");             //控件的宽度
                String coHeight = jsonCplist.getString("coHeight");            //控件的高度
                String coActionType = jsonCplist.getString("coActionType");           //互动类型
                String coLinkAction = jsonCplist.getString("coLinkAction");             //互动行为
                String coScreenProtectTime = jsonCplist.optString("coScreenProtectTime", "0");
                String coLinkId = jsonCplist.optString("coLinkId", "");
                if (coLinkId == null || coLinkId.contains("null")) {
                    coLinkId = "";
                }
                CpListEntity cpListEntity = new CpListEntity(cpid, sencenId, coType, coLeftPosition, coRightPosition, coWidth, coHeight,
                    coActionType, coLinkAction, coScreenProtectTime, pmResolutionType, pmFixedScreen, coLinkId);
                MyLog.task("====当前保存的控件的类型==" + coType);
                if (coType.equals(AppInfo.VIEW_SUBTITLE)
                    || coType.equals(AppInfo.VIEW_DATE)
                    || coType.equals(AppInfo.VIEW_COUNT_DOWN)
                    || coType.equals(AppInfo.VIEW_WEEK)
                    || coType.equals(AppInfo.VIEW_TIME)
                    || coType.equals(AppInfo.VIEW_BUTTON)
                    || coType.equals(AppInfo.VIEW_WEB_PAGE)
                    || coType.equals(AppInfo.VIEW_STREAM_VIDEO)
                    || coType.equals(AppInfo.VIEW_WEATHER)
                    || coType.equals(AppInfo.VIEW_HDMI)
                    || coType.equals(AppInfo.VIEW_EVEV_SCREEN)) {
                    List<TextInfo> textInfoListAll = new ArrayList<>();
                    //文档，日期，星期，时间，网页，天气
                    String txList = jsonCplist.getString("txList");          //文本属性
                    MyLog.task("====获取的控件是文本属性的==" + cpListEntity.toString());
                    if (jsonCplist.toString().contains("childCompentLst")) {
                        String childCompentLst = jsonCplist.getString("childCompentLst");
                        MyLog.task("====获取的控件是关联素材=素材关联=" + childCompentLst);
                        List<TextInfo> textInfoList = parsenerChildCompentInfoForTextInfo(taskid, pmType, cpid, childCompentLst);
                        List<MpListEntity> mpListEntityList = parsenerChildCompentInfoForMpListEntity(taskid, pmType, cpid, childCompentLst);
                        if (textInfoList != null && textInfoList.size() > 0) {
                            textInfoListAll.addAll(textInfoList);
                        }
                        if (mpListEntityList != null && mpListEntityList.size() > 0) {
                            cpListEntity.setMpList(mpListEntityList);
                        }
                    }
                    List<TextInfo> textInfoList = parsenerTextList(taskid, txList, pmType);
                    if (textInfoList != null && textInfoList.size() > 0) {
                        textInfoListAll.addAll(textInfoList);
                    }
                    cpListEntity.setTxList(textInfoListAll);
                } else if (coType.contains(AppInfo.VIEW_DOC)
                    || coType.contains(AppInfo.VIEW_IMAGE)
                    || coType.contains(AppInfo.VIEW_AUDIO)
                    || coType.contains(AppInfo.VIEW_VIDEO)
                    || coType.contains(AppInfo.VIEW_IMAGE_VIDEO)
                    || coType.contains(AppInfo.VIEW_AREA)) {
                    MyLog.task("====获取的控件是资源类型的==" + cpListEntity.toString());
                    List<MpListEntity> mpListEntityListAll = new ArrayList<MpListEntity>();
                    //文档，图片，音频，视频
                    String mpList = jsonCplist.getString("mpList");          //资源文件
                    if (jsonCplist.toString().contains("childCompentLst")) {
                        String childCompentLst = jsonCplist.getString("childCompentLst");
                        MyLog.task("====获取的控件是关联素材=文本关联=" + childCompentLst);
                        List<TextInfo> textInfoList = parsenerChildCompentInfoForTextInfo(taskid, pmType, cpid, childCompentLst);
                        List<MpListEntity> mpListEntityList = parsenerChildCompentInfoForMpListEntity(taskid, pmType, cpid, childCompentLst);
                        if (textInfoList != null && textInfoList.size() > 0) {
                            cpListEntity.setTxList(textInfoList);
                        }
                        if (mpListEntityList != null && mpListEntityList.size() > 0) {
                            mpListEntityListAll.addAll(mpListEntityList);
                        }
                    }
                    List<MpListEntity> mpListEntityList = parsenerMpList(taskid, pmType, cpid, mpList);
                    if (mpListEntityList != null && mpListEntityList.size() > 0) {
                        mpListEntityListAll.addAll(mpListEntityList);
                    }
                    cpListEntity.setMpList(mpListEntityListAll);
                }
                cpListEntityList.add(cpListEntity);
            }
        } catch (Exception e) {
            MyLog.ExceptionPrint("解析任务error: " + e.toString());
            e.printStackTrace();
        }
        return cpListEntityList;
    }

    /***
     * 解析关联素材
     * @param childCompentLst
     */
    private List<TextInfo> parsenerChildCompentInfoForTextInfo(String taskId, String pmType, String cpid, String childCompentLst) {
        List<TextInfo> textInfoList = new ArrayList<>();
        if (childCompentLst == null || childCompentLst.length() < 5) {
            MyLog.task("====parsenerChildCompentInfo==null。不操作");
            return textInfoList;
        }
        try {
            JSONArray jsonArray = new JSONArray(childCompentLst);
            int lengthArray = jsonArray.length();
            if (lengthArray < 1) {
                MyLog.task("parsenerChildCompentInfo当前控件没有资源==不用解析");
                return textInfoList;
            }
            if (childCompentLst.contains("taMoveSpeed") || childCompentLst.contains("taContent")) {
                //文本属性
                for (int i = 0; i < lengthArray; i++) {
                    JSONObject jsonTxtList = jsonArray.getJSONObject(i);
                    String txtId = jsonTxtList.getString("id");        //编号
                    String taCoId = jsonTxtList.getString("taCoId");   //控件编号
                    String taContent = jsonTxtList.getString("taContent");  //文本属性
                    String taMove = "1";          //硬件加速
                    String taBgImage = jsonTxtList.optString("taBgImage", "");
                    String taBgimageSize = "";
                    if (jsonTxtList.toString().contains("taBgimageSize")) {
                        taBgimageSize = jsonTxtList.getString("taBgimageSize");   //获取按钮得背景图文件大小
                        if (taBgimageSize == null || taBgimageSize.contains("null")) {
                            taBgimageSize = "";
                        }
                    }
                    TextInfo textInfo = new TextInfo(taskId, txtId, taCoId, taContent, taMove, DBTaskUtil.MP_RELATION, taBgImage, pmType, taBgimageSize);
                    textInfoList.add(textInfo);
                    MyLog.task("==parsenerChildCompentInfo获取的文本信息==" + textInfo.toString());
                }
                return textInfoList;
            }
        } catch (Exception e) {
            MyLog.ExceptionPrint("parsenerChildCompentInfo解析任务error: " + e.toString());
            e.printStackTrace();
        }

        return textInfoList;
    }

    private List<MpListEntity> parsenerChildCompentInfoForMpListEntity(String taskId, String pmType, String cpid, String childCompentLst) {
        List<MpListEntity> mpListEntityList = new ArrayList<>();
        if (childCompentLst == null || childCompentLst.length() < 5) {
            MyLog.task("====parsenerChildCompentInfo==null。不操作");
            return mpListEntityList;
        }
        MyLog.task("====parsenerChildCompentInfo==" + childCompentLst);
        try {
            JSONArray jsonArray = new JSONArray(childCompentLst);
            int lengthArray = jsonArray.length();
            if (lengthArray < 1) {
                MyLog.task("parsenerChildCompentInfo当前控件没有资源==不用解析");
                return mpListEntityList;
            }
            //资源属性
            MyLog.task("===parsenerChildCompentInfo==获取当前的资源数组个数===" + lengthArray);
            for (int i = 0; i < lengthArray; i++) {
                JSONObject jsonMplist = jsonArray.getJSONObject(i);
                String playParam = jsonMplist.optString("playParam");   //播放时长
                String mid = jsonMplist.optString("mid");               //素材ID
                String url = jsonMplist.optString("url");               //素材地址
                String size = jsonMplist.optString("size");             //文件大小
                String cartoon = "-1";                                        //动画特效
                String volume = jsonMplist.optString("volume");          //音量
                String parentCoId = DBTaskUtil.MP_RELATION + "";
                String type = jsonMplist.optString("type");
                String md5 = jsonMplist.optString("md5");
                if (TextUtils.isEmpty(url)) {
                    continue;
                }
                MpListEntity mpListEntity = new MpListEntity(taskId, mid, cpid, url, playParam, cartoon, size, volume, pmType, parentCoId, type, md5);
                MyLog.ExceptionPrint("parsenerChildCompentInfo==添加数据到集合: " + mpListEntity);
                mpListEntityList.add(mpListEntity);
            }
        } catch (Exception e) {
            MyLog.ExceptionPrint("parsenerChildCompentInfo解析任务error: " + e.toString());
            e.printStackTrace();
        }

        return mpListEntityList;
    }

    /**
     * 当前没有控件
     * 这里清除没有控件的空场景
     *
     * @param sencenId
     */
    private void delSencenByScenId(String sencenId) {
        boolean isDelScen = DBTaskUtil.delSenceByProId(sencenId);
        MyLog.task("============空场景直接删除==" + isDelScen);
        finishMySelf("当前节目没有控件");
    }

    /***
     * 解析文本属性
     * 控件ID
     * @param txList
     */
    private List<TextInfo> parsenerTextList(String taskid, String txList, String pmType) {
        List<TextInfo> textInfoList = new ArrayList<>();
        if (txList == null || TextUtils.isEmpty(txList) || txList.length() < 5) {
            MyLog.task("==txList=null，终止解析==");
            return textInfoList;
        }
        try {
            JSONArray jsonArray = new JSONArray(txList);
            int lengthArray = jsonArray.length();
            if (lengthArray < 1) {
                MyLog.task("文本信息里面没有数据");
                return textInfoList;
            }
            for (int i = 0; i < lengthArray; i++) {
                JSONObject jsonTxtList = jsonArray.getJSONObject(i);
                String txtId = jsonTxtList.getString("id");        //编号
                String taCoId = jsonTxtList.getString("taCoId");   //控件编号
                String taContent = jsonTxtList.getString("taContent");  //文本属性
                String taColor = jsonTxtList.getString("taColor");   //文字颜色
                String taNo = jsonTxtList.getString("taNo");      //序号
                String taFontSize = jsonTxtList.getString("taFontSize");  //字体大小
                String taMove = jsonTxtList.getString("taMove");          //运动方向
                String taBgColor = jsonTxtList.getString("taBgColor");    //背景颜色
                String taMoveSpeed = jsonTxtList.getString("taMoveSpeed");  //运动速度
                String taAddress = jsonTxtList.getString("taAddress");      //天气的地址
                /*if (taAddress.contains("区")) {
                    taAddress = taAddress.substring(0, taAddress.length() - 1);
                }*/
                String taAlignment = "5";
                if (jsonTxtList.toString().contains("taAlignment")) {
                    taAlignment = jsonTxtList.getString("taAlignment");      //显示的位置
                }
                String taCountDown = "90";
                if (jsonTxtList.toString().contains("taCountDown")) {
                    taCountDown = jsonTxtList.getString("taCountDown");      //显示的位置
                }
                String taFonType = "0";
                if (jsonTxtList.toString().contains("taFonType")) {
                    taFonType = jsonTxtList.getString("taFonType");      //显示的位置
                }
                MyLog.task("==获取的文本信息==" + taFonType);

                String taBgImage = null;
                if (jsonTxtList.toString().contains("taBgImage")) {
                    taBgImage = jsonTxtList.getString("taBgImage");   //获取按钮得背景图
                }
                String taBgimageSize = "";
                if (jsonTxtList.toString().contains("taBgimageSize")) {
                    taBgimageSize = jsonTxtList.getString("taBgimageSize");   //获取按钮得背景图文件大小
                    if (taBgimageSize == null || taBgimageSize.contains("null")) {
                        taBgimageSize = "";
                    }
                }
                TextInfo textInfo = new TextInfo(taskid, txtId, taCoId, taContent, taColor, taNo, taFontSize, taMove, taAddress, taBgColor,
                    taMoveSpeed, taAlignment, taCountDown, taFonType, DBTaskUtil.MP_DEFAULT, taBgImage, pmType, taBgimageSize);

                textInfoList.add(textInfo);
            }
        } catch (Exception e) {
            MyLog.ExceptionPrint("解析任务error: " + e.toString());
            e.printStackTrace();
        }
        return textInfoList;
    }

    /***
     * 解析资源文件
     * @param cpid
     * 控件ID
     * @param mpList
     */
    private List<MpListEntity> parsenerMpList(String taskId, String pmType, String cpid, String mpList) {
        List<MpListEntity> mpListEntityList = new ArrayList<>();
        if (mpList == null || TextUtils.isEmpty(mpList) || mpList.length() < 5) {
            MyLog.task("====mpList==null。不操作");
            return mpListEntityList;
        }
        MyLog.task("====mpList==" + mpList);
        try {
            JSONArray jsonArray = new JSONArray(mpList);
            int lengthArray = jsonArray.length();
            if (lengthArray < 1) {
                MyLog.task("当前控件没有资源==不用解析");
                return mpListEntityList;
            }
            MyLog.task("=====获取当前的资源数组个数===" + lengthArray);
            for (int i = 0; i < lengthArray; i++) {
                JSONObject jsonMplist = jsonArray.getJSONObject(i);
                String playParam = jsonMplist.getString("playParam");  //播放时长
                String mid = jsonMplist.getString("mid");              //素材ID
                String url = jsonMplist.getString("url");              //素材地址
                String size = jsonMplist.getString("size");            //文件大小
                String cartoon = jsonMplist.getString("cartoon");      //动画特效
                String volume = jsonMplist.getString("volume");
                String type = jsonMplist.getString("type");
                String md5 = jsonMplist.optString("md5");
                if (playParam == null || playParam.length() < 1) {
                    playParam = "10";
                }
                MyLog.task("=====ceshi__url===" + url);
                if (!TextUtils.isEmpty(url) && url.contains("\\")) {
                    url = url.replace("\\", "/");
                }
                String parentCoId = DBTaskUtil.MP_DEFAULT + "";
                MpListEntity mpListEntity = new MpListEntity(taskId, mid, cpid, url, playParam, cartoon, size, volume, pmType, parentCoId, type, md5);
                mpListEntityList.add(mpListEntity);
            }
        } catch (Exception e) {
            MyLog.ExceptionPrint("解析任务error: " + e.toString());
            e.printStackTrace();
        }

        return mpListEntityList;
    }

    /**
     * 删除数据库，提交删除信息给服务器
     *
     * @param taskid
     */
    private void delTaskByIdOrParsenError(final String desc, final String taskid) {
        DBTaskUtil.delTaskById(taskid, desc);
        if (handlerjson == null) {
            return;
        }
        handlerjson.post(new Runnable() {
            @Override
            public void run() {
                EtvService.getInstance().deleteEquipmentTaskById(desc, taskid);
            }
        });
    }
}
