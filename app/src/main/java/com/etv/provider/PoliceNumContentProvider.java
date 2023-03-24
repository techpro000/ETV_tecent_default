package com.etv.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.etv.config.AppInfo;
import com.etv.util.SharedPerManager;

import org.litepal.LitePal;

public class PoliceNumContentProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int PoliceID = 1;
    private static final int Police = 2;

    static {
        URI_MATCHER.addURI("com.etv.provider.PoliceNumContentProvider", "police", Police);
        URI_MATCHER.addURI("com.etv.provider.PoliceNumContentProvider", "police/#", PoliceID);
    }


    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        try {
            int flag = URI_MATCHER.match(uri);
            int workModel = SharedPerManager.getWorkModel();
            if (workModel == AppInfo.WORK_MODEL_NET) {
                switch (flag) {
                    case PoliceID:
                        cursor = LitePal.findBySQL("select * from PoliceNumEntity where titleTag=?", selection);
                        break;
                    case Police:
                        cursor = LitePal.findBySQL("select * from PoliceNumEntity");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
