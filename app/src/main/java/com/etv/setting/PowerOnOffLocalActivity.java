package com.etv.setting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.etv.config.AppInfo;
import com.etv.setting.adapter.PowerOnOffLocalAdapter;
import com.etv.util.poweronoff.PowerOnOffManager;
import com.etv.util.poweronoff.db.PowerDbManager;
import com.etv.util.MyLog;
import com.etv.util.poweronoff.entity.TimerDbEntity;
import com.ys.model.dialog.MyToastView;
import com.ys.model.dialog.WaitDialogUtil;
import com.ys.etv.R;
import com.ys.model.dialog.OridinryDialog;
import com.ys.model.listener.OridinryDialogClick;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/***
 * 单机版本的定时开关机
 */
public class PowerOnOffLocalActivity extends SettingBaseActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    ListView lv_power;
    TextView tv_show_time;
    PowerOnOffLocalAdapter adapter;
    LinearLayout iv_no_data;
    List<TimerDbEntity> timerDbEntityList = new ArrayList<TimerDbEntity>();
    private Handler handler = new Handler();
    Button btn_power_log;

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.fragment_poweronoff_local);
        initView();
        getLocalDbData();
    }

    TextView tv_mon, tv_tue, tv_wed, tv_thu, tv_fri, tv_sat, tv_sun;
    Button btn_add_time, btn_clear_time;
    OridinryDialog oridinryDialog;
    WaitDialogUtil waitDialogUtil;
    LinearLayout lin_exit;
    TextView tv_exit;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        waitDialogUtil = new WaitDialogUtil(PowerOnOffLocalActivity.this);
        lin_exit = (LinearLayout) findViewById(R.id.lin_exit);
        tv_exit = (TextView) findViewById(R.id.tv_exit);
        lin_exit.setOnClickListener(this);
        tv_exit.setOnClickListener(this);

        btn_power_log = (Button) findViewById(R.id.btn_power_log);
        btn_power_log.setOnClickListener(this);

        tv_mon = (TextView) findViewById(R.id.tv_mon);
        tv_tue = (TextView) findViewById(R.id.tv_tue);
        tv_wed = (TextView) findViewById(R.id.tv_wed);
        tv_thu = (TextView) findViewById(R.id.tv_thu);
        tv_fri = (TextView) findViewById(R.id.tv_fri);
        tv_sat = (TextView) findViewById(R.id.tv_sat);
        tv_sun = (TextView) findViewById(R.id.tv_sun);

        btn_clear_time = (Button) findViewById(R.id.btn_clear_time);
        btn_clear_time.setOnClickListener(this);
        btn_add_time = (Button) findViewById(R.id.btn_add_time);
        btn_add_time.setOnClickListener(this);
        tv_show_time = (TextView) findViewById(R.id.tv_show_time);
        iv_no_data = (LinearLayout) findViewById(R.id.iv_no_data);
        lv_power = (ListView) findViewById(R.id.lv_power);
        adapter = new PowerOnOffLocalAdapter(PowerOnOffLocalActivity.this, timerDbEntityList);
        lv_power.setAdapter(adapter);
        lv_power.setOnItemClickListener(this);
        lv_power.setOnItemLongClickListener(this);
    }

    /***
     * 获取数据库保存的定时开关时间
     */
    private void getLocalDbData() {
        try {
            timerDbEntityList.clear();
            waitDialogUtil.show(getString(R.string.refreshing));
            timerDbEntityList = PowerDbManager.queryTimerList();
            if (timerDbEntityList == null || timerDbEntityList.size() < 1) {
                waitDialogUtil.dismiss();
                iv_no_data.setVisibility(View.VISIBLE);
                PowerOnOffManager.getInstance().clearPowerOnOffTime("getLocalDbData local");
                MyToastView.getInstance().Toast(PowerOnOffLocalActivity.this, getString(R.string.no_data));
                getCurrentDate();
                return;
            }

            for(TimerDbEntity dd : timerDbEntityList) {
                System.out.println("aaaaaaaaaaaaaaaaaaaon list " + dd.getTimneId());
            }

            iv_no_data.setVisibility(View.GONE);
            adapter.setList(timerDbEntityList);
            getCurrentDate();
            PowerOnOffManager.getInstance().changePowerOnOffByWorkModel("本地定时开关机设置");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    waitDialogUtil.dismiss();
                    getCurrentDate();
                }
            }, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_power_log:
                startActivity(new Intent(PowerOnOffLocalActivity.this, PowerInOffLogActivity.class));
                break;
            case R.id.lin_exit:
            case R.id.tv_exit:
                finish();
                break;
            case R.id.btn_clear_time:
                PowerDbManager.clearTimeDb("本地手动清理数据库");
                getLocalDbData();
                break;
            case R.id.btn_add_time:
                Intent intent = new Intent(PowerOnOffLocalActivity.this, TimerChangeActivity.class);
                intent.putExtra(TimerChangeActivity.ORDER_STRING, TimerChangeActivity.ORDER_ADD);
                PowerOnOffLocalActivity.this.startActivity(intent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocalDbData();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        TimerDbEntity entity = timerDbEntityList.get(position);
        String id = entity.getTimneId();
        System.out.println("aaaaaaaaaaaaaaaaaaaonItemClick " + id);
        if (oridinryDialog == null) {
            oridinryDialog = new OridinryDialog(PowerOnOffLocalActivity.this);
        }
        oridinryDialog.show(getString(R.string.if_modify_power), getString(R.string.modify), getString(R.string.cancel));
        oridinryDialog.setOnDialogClickListener(new OridinryDialogClick() {
            @Override
            public void sure() {  //修改
                Intent intent = new Intent(PowerOnOffLocalActivity.this, TimerChangeActivity.class);
                intent.putExtra(TimerChangeActivity.ORDER_STRING, TimerChangeActivity.ORDER_MODIFY);
                intent.putExtra(TimerChangeActivity.TIMER_ID, id);
                PowerOnOffLocalActivity.this.startActivity(intent);
            }

            @Override
            public void noSure() {  //删除

            }
        });
    }

    /***
     * 更新界面，今天星期几和今天的定时开关机时间
     */
    private void getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int currentDayWeek = calendar.get(Calendar.DAY_OF_WEEK);  //今天的工作星期
        switch (currentDayWeek) {
            case 1: //7
                tv_sun.setTextColor(getResources().getColor(R.color.white));
                tv_sun.setBackgroundResource(R.drawable.terminal_setting);
                break;
            case 2: //1
                tv_mon.setBackgroundResource(R.drawable.terminal_setting);
                tv_mon.setTextColor(getResources().getColor(R.color.white));
                break;
            case 3: //2
                tv_tue.setBackgroundResource(R.drawable.terminal_setting);
                tv_tue.setTextColor(getResources().getColor(R.color.white));
                break;
            case 4: //3
                tv_wed.setBackgroundResource(R.drawable.terminal_setting);
                tv_wed.setTextColor(getResources().getColor(R.color.white));
                break;
            case 5: //4
                tv_thu.setBackgroundResource(R.drawable.terminal_setting);
                tv_thu.setTextColor(getResources().getColor(R.color.white));
                break;
            case 6: //5
                tv_fri.setBackgroundResource(R.drawable.terminal_setting);
                tv_fri.setTextColor(getResources().getColor(R.color.white));
                break;
            case 7: //6
                tv_sat.setBackgroundResource(R.drawable.terminal_setting);
                tv_sat.setTextColor(getResources().getColor(R.color.white));
                break;

        }
        //=======================================================================
        //更新一下下一次的定时开关机
        String onTime = PowerOnOffManager.getInstance().getPowerOnTime();
        String offTime = PowerOnOffManager.getInstance().getPowerOffTime();
        MyLog.d("PowerOnOff", "======onTime:" + onTime);
        MyLog.d("PowerOnOff", "======offTime:" + offTime);
        if (onTime.length() < 2 || offTime.length() < 2) {
            tv_show_time.setText(getLanguageFromResurce(R.string.close_time) + " :" + offTime + "\n" + getLanguageFromResurce(R.string.open_time) + ": " + onTime);
        } else {
            String year = onTime.substring(0, 4);
            String month = onTime.substring(4, 6);
            String day = onTime.substring(6, 8);
            String hour = onTime.substring(8, 10);
            String min = onTime.substring(10, 12);

            String offyear = offTime.substring(0, 4);
            String offmonth = offTime.substring(4, 6);
            String offday = offTime.substring(6, 8);
            String offhour = offTime.substring(8, 10);
            String offmin = offTime.substring(10, 12);
            String onTimeShow = year + "/" + month + "/" + day + "  " + hour + ":" + min + ":00";
            String offTimeShow = offyear + "/" + offmonth + "/" + offday + "  " + offhour + ":" + offmin + ":00";
            tv_show_time.setText(getString(R.string.close_time) + ": " + offTimeShow + "\n"
                    + getString(R.string.open_time) + ": " + onTimeShow);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        TimerDbEntity entity = timerDbEntityList.get(position);
        String id = entity.getTimneId();
        if (oridinryDialog == null) {
            oridinryDialog = new OridinryDialog(PowerOnOffLocalActivity.this);
        }
        oridinryDialog.show(getString(R.string.if_del_power), getString(R.string.delete), getString(R.string.cancel));
        oridinryDialog.setOnDialogClickListener(new OridinryDialogClick() {
            @Override
            public void sure() {  //删除
                boolean isDel = PowerDbManager.delTimerById(id);
                String del_power_statues = getLanguageFromResurceWithPosition(R.string.del_power_statues, isDel ?
                        getString(R.string.success) : getString(R.string.failed));
                showToastView(del_power_statues);
                getLocalDbData();
            }

            @Override
            public void noSure() {  //取消

            }
        });
        return true;
    }

//    public void onDestroy() {
//        super.onDestroy();
//        if (receiver != null) {
//            PowerOnOffLocalActivity.this.unregisterReceiver(receiver);
//        }
//    }

}
