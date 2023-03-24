//package com.etv.provider;
//
//import android.content.ContentProvider;
//import android.content.ContentValues;
//import android.content.UriMatcher;
//import android.database.Cursor;
//import android.net.Uri;
//
//import com.etv.config.AppInfo;
//import com.etv.util.SharedPerManager;
//import com.etv.util.poweronoff.entity.TimerDbEntity;
//
//import org.litepal.LitePal;
//
//public class TimerContentProvider extends ContentProvider {
//
//    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
//    private static final int TIMERBYID = 1;
//    private static final int TIMER = 2;
//
//    static {
//        URI_MATCHER.addURI("com.etv.provider.TimerContentProvider", "timer", TIMER);
//        URI_MATCHER.addURI("com.etv.provider.TimerContentProvider", "timer/#", TIMERBYID);
//    }
//
//    public TimerContentProvider() {
//
//    }
//
//    @Override
//    public boolean onCreate() {
//        return true;
//    }
//
//    @Override
//    public Cursor query(Uri uri, String[] strings, String selection, String[] strings1, String s1) {
//        Cursor cursor = null;
//        try {
//            int flag = URI_MATCHER.match(uri);
//            int workModel = SharedPerManager.getWorkModel();
//            if (workModel == AppInfo.WORK_MODEL_NET) {
//                switch (flag) {
//                    case TIMERBYID:
//                        cursor = LitePal.findBySQL("select * from TimerDbEntity where taskid=?", selection);
//                        break;
//                    case TIMER:
//                        cursor = LitePal.findBySQL("select * from TimerDbEntity");
//                        break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return cursor;
//    }
//
//
//    @Override
//    public String getType(Uri uri) {
//        return null;
//    }
//
//
//    @Override
//    public Uri insert(Uri uri, ContentValues contentValues) {
//        String timeId = System.currentTimeMillis() + "";
//        String ttOnTime = contentValues.getAsString("ttOnTime");
//        String ttOffTime = contentValues.getAsString("ttOffTime");
//        String ttMon = contentValues.getAsString("ttMon");
//        String ttTue = contentValues.getAsString("ttTue");
//        String ttWed = contentValues.getAsString("ttWed");
//        String ttThu = contentValues.getAsString("ttThu");
//        String ttFri = contentValues.getAsString("ttFri");
//        String ttSat = contentValues.getAsString("ttSat");
//        String ttSun = contentValues.getAsString("ttSun");
//        TimerDbEntity timerDbEntity = new TimerDbEntity(timeId, ttOnTime, ttOffTime, ttMon, ttTue, ttWed, ttThu, ttFri, ttSat, ttSun);
//        int workModel = SharedPerManager.getWorkModel();
//        if (workModel == AppInfo.WORK_MODEL_NET) {//网络模式
//            boolean isSave = DbTimerUtil.addTimerDb(timerDbEntity);
//            if (isSave) {
//                return Uri.parse("success");
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public int delete(Uri uri, String s, String[] strings) {
//        return 0;
//    }
//
//    @Override
//    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
//        return 0;
//    }
//}
