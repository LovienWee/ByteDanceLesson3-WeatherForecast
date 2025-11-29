package com.example.weatherforecast;

import static android.widget.Toast.makeText;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weatherforecast.model.Cast;
import com.example.weatherforecast.model.Forecast;
import com.example.weatherforecast.model.WeatherResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TodayFragment extends Fragment {

    private static final String TAG = "TodayFragment";

    // 高德配置
    private static final String API_KEY = "d73086a8814c6373345a50cb321346cd"; // 我的Key！！仅供开发学习和参考，请勿商用！！！
    private static final String BASE_URL = "https://restapi.amap.com/v3/weather/weatherInfo";

    // 城市编码
    private static final String CITY_BEIJING   = "110000";
    private static final String CITY_SHANGHAI  = "310000";
    private static final String CITY_GUANGZHOU = "440100";
    private static final String CITY_SHENZHEN  = "440300";

    private String currentCityCode = CITY_SHANGHAI;

    // UI 视图
    private View rootView;
    private TextView tvCity;
    private TextView tvWeather;
    private TextView tvTemp;
    private TextView tvHighLow;

    private TextView tvDayWeather;
    private TextView tvDayTemp;
    private TextView tvDayWind;

    private TextView tvNightWeather;
    private TextView tvNightTemp;
    private TextView tvNightWind;
    private View rootToday;

    private TextView tabCityBeijing;
    private TextView tabCityShanghai;
    private TextView tabCityGuangzhou;
    private TextView tabCityShenzhen;

    private String currentMainWeather;
    private Forecast lastForecast;
    private Cast lastTodayCast;



    // 网络相关
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_today, container, false);

        rootToday = view.findViewById(R.id.root_today);

        // 先设置背景（用我们刚刚的 currentMainWeather 逻辑）
        if (currentMainWeather != null) {
            updateBackgroundByWeather(currentMainWeather);
        } else {
            updateBackgroundByWeather(isNightNow() ? "阴" : "晴");
        }

        initViews(view);
        initCityTabs();

        // ⭐ 核心逻辑：如果有缓存，就先用缓存渲染一遍
        if (lastForecast != null && lastTodayCast != null) {
            renderTodayWeather(lastForecast, lastTodayCast);
        } else {
            // 第一次进入，没有数据时才去请求
            fetchWeatherForCurrentCity();
        }

        return view;

    }

    private void initViews(View view) {
        rootToday = view.findViewById(R.id.root_today);

        tvCity = view.findViewById(R.id.tv_city);
        tvWeather = view.findViewById(R.id.tv_weather);
        tvTemp = view.findViewById(R.id.tv_temp);
        tvHighLow = view.findViewById(R.id.tv_high_low);

        tvDayWeather = view.findViewById(R.id.tv_day_weather);
        tvDayTemp = view.findViewById(R.id.tv_day_temp);
        tvDayWind = view.findViewById(R.id.tv_day_wind);

        tvNightWeather = view.findViewById(R.id.tv_night_weather);
        tvNightTemp = view.findViewById(R.id.tv_night_temp);
        tvNightWind = view.findViewById(R.id.tv_night_wind);

        tabCityBeijing = view.findViewById(R.id.tab_city_beijing);
        tabCityShanghai = view.findViewById(R.id.tab_city_shanghai);
        tabCityGuangzhou = view.findViewById(R.id.tab_city_guangzhou);
        tabCityShenzhen = view.findViewById(R.id.tab_city_shenzhen);
    }

    private void initCityTabs() {
        View.OnClickListener listener = v -> {
            String newCity = currentCityCode;

            if (v.getId() == R.id.tab_city_beijing) {
                newCity = CITY_BEIJING;
            } else if (v.getId() == R.id.tab_city_shanghai) {
                newCity = CITY_SHANGHAI;
            } else if (v.getId() == R.id.tab_city_guangzhou) {
                newCity = CITY_GUANGZHOU;
            } else if (v.getId() == R.id.tab_city_shenzhen) {
                newCity = CITY_SHENZHEN;
            }

            if (!newCity.equals(currentCityCode)) {
                currentCityCode = newCity;
                updateCityTabSelected();
                fetchWeatherForCurrentCity();
            }
        };

        tabCityBeijing.setOnClickListener(listener);
        tabCityShanghai.setOnClickListener(listener);
        tabCityGuangzhou.setOnClickListener(listener);
        tabCityShenzhen.setOnClickListener(listener);

        // 初始化一次选中状态
        updateCityTabSelected();
    }

    private void updateCityTabSelected() {
        // 全部先设为未选中
        tabCityBeijing.setSelected(false);
        tabCityShanghai.setSelected(false);
        tabCityGuangzhou.setSelected(false);
        tabCityShenzhen.setSelected(false);

        int unselectedColor = Color.parseColor("#CCFFFFFF");
        tabCityBeijing.setTextColor(unselectedColor);
        tabCityShanghai.setTextColor(unselectedColor);
        tabCityGuangzhou.setTextColor(unselectedColor);
        tabCityShenzhen.setTextColor(unselectedColor);

        TextView selectedTab = null;
        if (CITY_BEIJING.equals(currentCityCode)) {
            selectedTab = tabCityBeijing;
        } else if (CITY_SHANGHAI.equals(currentCityCode)) {
            selectedTab = tabCityShanghai;
        } else if (CITY_GUANGZHOU.equals(currentCityCode)) {
            selectedTab = tabCityGuangzhou;
        } else if (CITY_SHENZHEN.equals(currentCityCode)) {
            selectedTab = tabCityShenzhen;
        }

        if (selectedTab != null) {
            selectedTab.setSelected(true);
            selectedTab.setTextColor(Color.WHITE);
        }
    }


    // region 网络请求 & 数据渲染 ---------------------------------------

    private void fetchWeatherForCurrentCity() {
        String url = BASE_URL
                + "?city=" + currentCityCode
                + "&extensions=all"
                + "&key=" + API_KEY;

        Log.d(TAG, "fetchWeather url = " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "request failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "response not successful, code = " + response.code());
                    return;
                }

                String json = response.body().string();
                Log.d(TAG, "response json = " + json);

                WeatherResponse weatherResponse = gson.fromJson(json, WeatherResponse.class);
                if (weatherResponse == null
                        || weatherResponse.forecasts == null
                        || weatherResponse.forecasts.isEmpty()) {
                    Log.e(TAG, "no forecasts");
                    return;
                }

                Forecast forecast = weatherResponse.forecasts.get(0);
                List<Cast> casts = forecast.casts;
                if (casts == null || casts.isEmpty()) {
                    Log.e(TAG, "no casts");
                    return;
                }

                Cast today = forecast.casts.get(0);

                // 回到主线程更新 UI
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> renderTodayWeather(forecast, today));
            }
        });
    }

    private void renderTodayWeather(Forecast forecast, Cast today) {
        // 顶部城市、天气
        tvCity.setText(forecast.city);

        String mainWeather;
        String mainTemp;

        if (isNightNow()) {
            mainWeather = today.nightweather;
            mainTemp = today.nighttemp;
        } else {
            mainWeather = today.dayweather;
            mainTemp = today.daytemp;
        }

        // 缓存
        lastForecast = forecast;
        lastTodayCast = today;
        currentMainWeather = mainWeather;

        // Emoji 显示主天气
        String emojiWeather = mapWeatherToEmoji(mainWeather);
        tvWeather.setText(emojiWeather);

        // 背景渐变也根据主天气决定
        updateBackgroundByWeather(mainWeather);

        // 当前温度 & 高低温
        tvTemp.setText(mainTemp + "°");
        String highLow = "最高: " + today.daytemp + "°  最低: " + today.nighttemp + "°";
        tvHighLow.setText(highLow);

        // 白天卡片
        tvDayWeather.setText(today.dayweather);
        tvDayTemp.setText(today.daytemp + "°");
        tvDayWind.setText(today.daywind + " " + today.daypower + "级");

        // 夜间卡片
        tvNightWeather.setText(today.nightweather);
        tvNightTemp.setText(today.nighttemp + "°");
        tvNightWind.setText(today.nightwind + " " + today.nightpower + "级");

        // 把未来几天的数据发给 MainActivity，让它转交给 ForecastFragment
        if (getActivity() instanceof MainActivity) {
            MainActivity act = (MainActivity) getActivity();
            act.updateForecastData(forecast.city, forecast.casts);
        }

    }

    private String mapWeatherToEmoji(String weather) {
        if (weather == null) return "天气未知";

        if (weather.contains("晴")) {
            return "☀️ " + weather;
        } else if (weather.contains("云")) {
            return "⛅ " + weather;
        } else if (weather.contains("雨")) {
            return "🌧️ " + weather;
        } else if (weather.contains("雪")) {
            return "❄️ " + weather;
        } else if (weather.contains("雾") || weather.contains("霾")) {
            return "🌫️ " + weather;
        } else if (weather.contains("阴")) {
            return "☁️ " + weather;
        } else {
            return "🌈 " + weather;
        }
    }

    private void updateBackgroundByWeather(String weather) {
        if (rootToday == null || weather == null) return;

        int bgResId;

        if (weather.contains("雨")) {
            bgResId = R.drawable.bg_today_gradient_rain;
        } else if (weather.contains("雪")) {
            bgResId = R.drawable.bg_today_gradient_snow;
        } else if (weather.contains("雾") || weather.contains("霾")) {
            bgResId = R.drawable.bg_today_gradient_fog;
        } else if (weather.contains("阴")) {
            bgResId = R.drawable.bg_today_gradient_overcast;
        } else if (weather.contains("云")) {   // 多云、少云
            bgResId = R.drawable.bg_today_gradient_cloudy;
        } else {
            // 默认当作晴天
            bgResId = R.drawable.bg_today_gradient_sunny;
        }

        rootToday.setBackgroundResource(bgResId);
    }

    private boolean isNightNow() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);  // 0~23

        // 18点到凌晨6点算夜间
        return (hour >= 18 || hour < 6);
    }

    // endregion
}
