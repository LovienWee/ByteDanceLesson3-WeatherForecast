package com.example.weatherforecast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast.model.Cast;

import java.util.ArrayList;
import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<Cast> casts = new ArrayList<>();
    private boolean isNightNow;

    public ForecastAdapter(boolean isNightNow) {
        this.isNightNow = isNightNow;
    }

    public void setData(List<Cast> list) {
        casts = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast_day, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        Cast cast = casts.get(position);

        // 1. 日期（MM/dd）
        String dateStr = cast.date.substring(5).replace("-", "/");
        holder.tvDayDate.setText(dateStr);

        // 2. 今天 / 明天 / 星期X
        if (position == 0) {
            holder.tvDayLabel.setText("今天");
        } else if (position == 1) {
            holder.tvDayLabel.setText("明天");
        } else {
            holder.tvDayLabel.setText("星期" + mapWeekToChinese(cast.week));
        }

        // 3. 天气（首行按昼夜，其余白天）
        String weatherText;
        if (position == 0 && isNightNow) {
            weatherText = cast.nightweather;
        } else {
            weatherText = cast.dayweather;
        }
        holder.tvWeatherIcon.setText(mapWeatherToEmoji(weatherText));
        holder.tvWeatherText.setText(weatherText);

        // 4. 温度区间
        holder.tvTempRange.setText(cast.daytemp + "° / " + cast.nighttemp + "°");
    }


    @Override
    public int getItemCount() {
        return casts.size();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayLabel, tvDayDate, tvWeatherIcon, tvWeatherText, tvTempRange;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayLabel    = itemView.findViewById(R.id.tv_day_label);
            tvDayDate     = itemView.findViewById(R.id.tv_day_date);
            tvWeatherIcon = itemView.findViewById(R.id.tv_weather_icon);
            tvWeatherText = itemView.findViewById(R.id.tv_weather_text);
            tvTempRange   = itemView.findViewById(R.id.tv_temp_range);
        }
    }

    private String mapWeatherToEmoji(String weather) {
        if (weather == null) return "🌈";
        if (weather.contains("晴")) return "☀️";
        if (weather.contains("云")) return "⛅";
        if (weather.contains("阴")) return "☁️";
        if (weather.contains("雨")) return "🌧️";
        if (weather.contains("雪")) return "❄️";
        if (weather.contains("雾") || weather.contains("霾")) return "🌫️";
        return "🌈";
    }

    private String mapWeekToChinese(String week) {
        if (week == null) return "";
        switch (week) {
            case "1": return "一";
            case "2": return "二";
            case "3": return "三";
            case "4": return "四";
            case "5": return "五";
            case "6": return "六";
            case "7": return "日";
        }
        return "";
    }

}
