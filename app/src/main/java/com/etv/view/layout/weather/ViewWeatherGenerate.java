package com.etv.view.layout.weather;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.util.MyLog;
import com.etv.view.layout.Generator;
import com.ys.etv.R;
import com.etv.task.util.TaskDealUtil;
import com.etv.util.weather.WeatherEntity;
import com.etv.util.weather.WeatherParse;

import java.util.List;

/**
 * 展示天气的控件
 * Created by dinglong
 */
public class ViewWeatherGenerate extends Generator {

    Context context;
    View view;
    String weatherCity;
    String taFont;
    String fontSzie;

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

    public ViewWeatherGenerate(Context context, int startX, int StartY, int width, int height, String weatherCity, String taFont, String fontSzie) {
        super(context, startX, StartY, width, height);
        this.weatherCity = weatherCity;
        this.context = context;
        this.taFont = taFont;
        this.fontSzie = fontSzie;
        view = LayoutInflater.from(context).inflate(R.layout.view_weather, null);
        float widComPairHei = (float) ((width * 1.0) / (height * 1.0));
        MyLog.cdl("====比例==" + widComPairHei + " / " + width + " / " + height);
        initView(view);
    }

    private TextView tv_temp;
    private TextView tv_weather_state;
    private TextView tv_city;
    ImageView iv_weather_icon;
    RelativeLayout lin_weather_bgg;

    private void initView(View view) {
        lin_weather_bgg = (RelativeLayout) view.findViewById(R.id.lin_weather_bgg);
        iv_weather_icon = (ImageView) view.findViewById(R.id.iv_weather_icon);
        tv_city = (TextView) view.findViewById(R.id.tv_city);
        tv_city.setText(weatherCity);
        tv_weather_state = (TextView) view.findViewById(R.id.tv_weather_state);
        tv_temp = (TextView) view.findViewById(R.id.tv_temp);
        float fontSize = TaskDealUtil.getTextSize(fontSzie);
        setTextViewSize(fontSize);
        setImageViewSize(iv_weather_icon);
        lin_weather_bgg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.longClickView(null, null);
                }
                return true;
            }
        });
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void clearMemory() {

    }

    @Override
    public void removeCacheView(String tag) {

    }

    int cityLength = 2;
    String textColor = "#FFFFFF";

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        int bggColor = 0x00000000;      //透明
        WeatherEntity weather = (WeatherEntity) object;
        if (weather == null) {
            weather = new WeatherEntity("北京", "多云", "15℃", "25℃", textColor, "");
        }
        String backColor = weather.getBggColor();
        bggColor = TaskDealUtil.getColorFromInToSystem(backColor);
        lin_weather_bgg.setBackgroundColor(bggColor);
        String city = weather.getCity();
        cityLength = city.trim().length();
        String weatherInfo = weather.getWeatherInfo();
        String lowTem = weather.getLowTem();
        if (!lowTem.contains("℃")) {
            lowTem = lowTem + "℃";
        }
        String heightTem = weather.getHeightTem();
        if (!heightTem.contains("℃")) {
            heightTem = heightTem + "℃";
        }
        WeatherParse.setWeatherImage(iv_weather_icon, weatherInfo);
        setTextColorSize(weather);
        tv_city.setText(city);
        tv_weather_state.setText(weatherInfo);
        tv_temp.setText(lowTem + "~" + heightTem);
    }

    @Override
    public void playComplet() {

    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    private void setTextColorSize(WeatherEntity weather) {
        try {
            textColor = weather.getTaColor();
            int color = TaskDealUtil.getColorFromInToSystem(textColor);
            tv_city.setTextColor(color);
            tv_weather_state.setTextColor(color);
            tv_temp.setTextColor(color);
        } catch (Exception e) {
            Log.e("cdl", "==解析天气比例尺寸error==" + e.toString());
        }
    }

    private void setTextViewSize(float fontSize) {
        Typeface typeface = TaskDealUtil.getFontTypeFace(taFont);
        if (typeface != null) {
            tv_city.setTypeface(typeface);
            tv_temp.setTypeface(typeface);
            tv_weather_state.setTypeface(typeface);
        }
        tv_city.setTextSize(fontSize);
        tv_temp.setTextSize(fontSize);
        tv_weather_state.setTextSize(fontSize);
    }

    private void setImageViewSize(View view) {
        int textHeight = tv_temp.getLineHeight();
        MyLog.cdl("====文字得高度===" + textHeight);
//        float width = (TaskDealUtil.getTextSize(fontSzie)) * 3;
        float width = textHeight * 3;
        LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        localLayoutParams.height = (int) width;
        localLayoutParams.width = (int) width;
        view.setLayoutParams(localLayoutParams);
    }


    @Override
    public void moveViewForward(boolean b) {

    }

    @Override
    public void pauseDisplayView() {

    }

    @Override
    public void resumePlayView() {

    }

}
