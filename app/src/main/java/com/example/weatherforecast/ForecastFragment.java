package com.example.weatherforecast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast.model.Cast;

import java.util.Calendar;
import java.util.List;

public class ForecastFragment extends Fragment {

    private RecyclerView recyclerView;
    private ForecastAdapter adapter;
    private TextView tvForecastCity;
    private String currentCity;
    private List<Cast> forecastData;
    private View rootForecast;
    private String pendingBgWeather;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_forecast, container, false);

        rootForecast   = view.findViewById(R.id.root_forecast);
        if (pendingBgWeather != null) {
            updateBackgroundByWeather(pendingBgWeather);
        }
        tvForecastCity = view.findViewById(R.id.tv_forecast_city);
        recyclerView = view.findViewById(R.id.recycler_forecast);

        if (currentCity != null) {
            tvForecastCity.setText(currentCity);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ForecastAdapter(isNightNow());
        recyclerView.setAdapter(adapter);

        if (forecastData != null) {
            adapter.setData(forecastData);
        }

        return view;
    }

    // 更新未来 4 天天气
    public void updateForecast(String cityName, List<Cast> list) {
        currentCity  = cityName;
        forecastData = list;

        if (tvForecastCity != null) {
            tvForecastCity.setText(cityName);
        }
        if (adapter != null) {
            adapter.setData(list);
        }

        if (list != null && !list.isEmpty()) {
            Cast first = list.get(0);
            String mainWeather;
            if (isNightNow()) {
                mainWeather = first.nightweather;
            } else {
                mainWeather = first.dayweather;
            }

            // 先存起来
            pendingBgWeather = mainWeather;

            // 如果此时 view 已经创建好了，顺便更新一次背景
            if (rootForecast != null) {
                updateBackgroundByWeather(mainWeather);
            }
        }
    }

    private boolean isNightNow() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= 18 || hour < 6);
    }

    private void updateBackgroundByWeather(String weather) {
        if (rootForecast == null || weather == null) return;

        int bgResId;
        if (weather.contains("雨")) {
            bgResId = R.drawable.bg_today_gradient_rain;
        } else if (weather.contains("雪")) {
            bgResId = R.drawable.bg_today_gradient_snow;
        } else if (weather.contains("雾") || weather.contains("霾")) {
            bgResId = R.drawable.bg_today_gradient_fog;
        } else if (weather.contains("阴")) {
            bgResId = R.drawable.bg_today_gradient_overcast;
        } else if (weather.contains("云")) { // 多云、少云等
            bgResId = R.drawable.bg_today_gradient_cloudy;
        } else {
            bgResId = R.drawable.bg_today_gradient_sunny;
        }

        rootForecast.setBackgroundResource(bgResId);
    }



}
