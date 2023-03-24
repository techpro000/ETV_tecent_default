package com.etv.view.layout.wps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.style.FontStyle;
import com.bin.david.form.utils.DensityUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.util.SharedPerManager;
import com.etv.util.wps.excelpoi.ExcelCallback;
import com.etv.util.wps.excelpoi.IExcel2Table;
import com.etv.util.wps.excelpoi.POIExcel2Table;
import com.etv.util.wps.excelpoi.SheetAdapter;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.util.List;

/**
 * 功能更完善的EXCEL数据读取器
 */
public class ViewExcelPoiGenertrator extends Generator implements ExcelCallback, View.OnClickListener {

    View view;
    String excelPath;
    Context context;

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    @Override
    public void timeChangeToUpdateView() {

    }

    @Override
    public void updateTextInfo(Object object) {

    }

    public ViewExcelPoiGenertrator(Context context, int x, int y, int width, int height, String excelPath) {
        super(context, x, y, width, height);
        this.context = context;
        this.excelPath = excelPath;
        view = LayoutInflater.from(context).inflate(R.layout.view_smart_table, null);
        initView(view);
    }

    private SmartTable<Cell> table;
    private RecyclerView recyclerView;
    private IExcel2Table<Cell> iExcel2Table;
    private Button btn_mless, btn_more;

    private void initView(View view) {
        LinearLayout iv_no_data = (LinearLayout) view.findViewById(R.id.iv_no_data);
        TextView tv_desc = (TextView) view.findViewById(R.id.tv_desc);
        File file = new File(excelPath);
        if (!file.exists()) {
            iv_no_data.setVisibility(View.VISIBLE);
            tv_desc.setText(context.getString(R.string.file_not_exict));
        }
        if (!SharedPerManager.getWpsShowEnable()) {
            iv_no_data.setVisibility(View.VISIBLE);
            tv_desc.setText(context.getString(R.string.open_not_open));
            return;
        }
        FontStyle.setDefaultTextSize(DensityUtils.sp2px(context, 15));
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        table = (SmartTable<Cell>) view.findViewById(R.id.table);
        iExcel2Table = new POIExcel2Table();
        iExcel2Table.setIsAssetsFile(false);
        iExcel2Table.initTableConfig(context, table);
        iExcel2Table.setCallback(this);
        iExcel2Table.loadSheetList(context, excelPath);

        btn_mless = (Button) view.findViewById(R.id.btn_mless);
        btn_more = (Button) view.findViewById(R.id.btn_more);
        btn_more.setOnClickListener(this);
        btn_mless.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mless:
                if (table == null) {
                    return;
                }
                table.getMatrixHelper().zoomMin();
                break;
            case R.id.btn_more:
                if (table == null) {
                    return;
                }
                table.getMatrixHelper().zoomMax();
                break;
        }
    }


    @Override
    public void getSheetListSuc(List<String> sheetNames) {
        recyclerView.setHasFixedSize(true);
        if (sheetNames != null && sheetNames.size() > 0) {
            final SheetAdapter sheetAdapter = new SheetAdapter(sheetNames);
            sheetAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    sheetAdapter.setSelectPosition(position);
                    iExcel2Table.loadSheetContent(context, position);
                }
            });
            recyclerView.setAdapter(sheetAdapter);
            iExcel2Table.loadSheetContent(context, 0);
        }
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {
        if (iExcel2Table != null) {
            iExcel2Table.clear();
        }
        iExcel2Table = null;
    }

    @Override
    public void removeCacheView(String tag) {
        clearMemory();
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {

    }

    @Override
    public void playComplet() {

    }

    @Override
    public void pauseDisplayView() {

    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    @Override
    public void resumePlayView() {

    }

    @Override
    public void moveViewForward(boolean b) {

    }


}
