//package com.etv.provider;
//
//
//import android.content.ContentProvider;
//import android.content.ContentValues;
//import android.content.UriMatcher;
//import android.database.Cursor;
//import android.net.Uri;
//
//import com.etv.db.DbStatiscs;
//import com.etv.entity.StatisticsEntity;
//
//import org.litepal.LitePal;
//
//
//public class StatiscsContentProvider extends ContentProvider {
//
//    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
//    private static final int StatiscsID = 1;
//    private static final int Statiscs = 2;
//
//    static {
//        URI_MATCHER.addURI("com.etv.provider.StatiscsContentProvider", "statiscs", Statiscs);
//        URI_MATCHER.addURI("com.etv.provider.StatiscsContentProvider", "statiscs/#", StatiscsID);
//    }
//
//    public StatiscsContentProvider() {
//
//    }
//
//    @Override
//    public boolean onCreate() {
//        return true;
//    }
//
//
//    @Override
//    public Cursor query(Uri uri, String[] strings, String selection, String[] strings1, String s1) {
//        Cursor cursor = null;
//        try {
//            int flag = URI_MATCHER.match(uri);
//            switch (flag) {
//                case StatiscsID:
//                    cursor = LitePal.findBySQL("select * from StatisticsEntity where mtid=?", selection);
//                    break;
//                case Statiscs:
//                    cursor = LitePal.findBySQL("select * from StatisticsEntity");
//                    break;
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
//        String mtid = contentValues.getAsString("mtid");
//        String addtype = contentValues.getAsString("addtype");
//        int pmtime = contentValues.getAsInteger("pmtime");
//        int count = contentValues.getAsInteger("count");
//        long createtime = contentValues.getAsLong("createtime");
//        StatisticsEntity statisticsEntity = new StatisticsEntity(mtid, addtype, pmtime, count, createtime);
//        boolean isSave = DbStatiscs.saveStatiseToLocal(statisticsEntity, "contentProvider保存");
//        if (isSave) {
//            return Uri.parse("success");
//        }
//        return null;
//    }
//
//    @Override
//    public int delete(Uri uri, String s, String[] strings) {
//        boolean isDel = false;
//        try {
//            int flag = URI_MATCHER.match(uri);
//            switch (flag) {
//                case StatiscsID:
//                    isDel = DbStatiscs.delInfoById(s);
//                    break;
//                case Statiscs:
//                    isDel = DbStatiscs.clearDbStatiesAllData();
//                    break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (isDel) {
//            return 1;
//        }
//        return 0;
//    }
//
//    @Override
//    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
//        return 0;
//    }
//}
