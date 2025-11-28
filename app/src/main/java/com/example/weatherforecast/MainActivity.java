package com.example.weatherforecast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    // region 常量配置 ----------------------------------------------------------------

    // 选中 / 未选中 文本颜色（白色 & 半透明白）
    private static final int COLOR_TEXT_SELECTED   = Color.parseColor("#FFFFFFFF");
    private static final int COLOR_TEXT_UNSELECTED = Color.parseColor("#99FFFFFF");

    // endregion

    // region Tab 枚举 ----------------------------------------------------------------

    private enum Tab {
        TODAY,
        FORECAST
    }

    // endregion

    // region 成员变量：Fragment & 底部导航视图 -----------------------------------------

    // 两个主页面
    private TodayFragment todayFragment;
    private ForecastFragment forecastFragment;

    // 底部导航：容器视图
    private View tabToday;
    private View tabForecast;

    // 底部导航：文案 / 图标（现在图标是 emoji 的 TextView）
    private TextView tabTodayLabel;
    private TextView tabForecastLabel;
    private TextView tabTodayIcon;
    private TextView tabForecastIcon;

    // 当前选中的 Tab
    private Tab currentTab = null;

    // endregion

    // region 生命周期 ----------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragments();
        initBottomTabs();

        // 默认显示「今天」页面
        switchToTab(Tab.TODAY);
    }

    // endregion

    // region 初始化相关 --------------------------------------------------------------

    /** 初始化两个主 Fragment */
    private void initFragments() {
        todayFragment = new TodayFragment();
        forecastFragment = new ForecastFragment();
    }

    /** 初始化底部导航栏：查找 View + 绑定点击事件 */
    private void initBottomTabs() {
        tabToday        = findViewById(R.id.tab_today);
        tabForecast     = findViewById(R.id.tab_forecast);
        tabTodayLabel   = findViewById(R.id.tab_today_label);
        tabForecastLabel= findViewById(R.id.tab_forecast_label);
        tabTodayIcon    = findViewById(R.id.tab_today_icon);
        tabForecastIcon = findViewById(R.id.tab_forecast_icon);

        tabToday.setOnClickListener(v -> switchToTab(Tab.TODAY));
        tabForecast.setOnClickListener(v -> switchToTab(Tab.FORECAST));
    }

    // endregion

    // region 页面切换 & 底部 Tab 状态 ------------------------------------------------

    /**
     * 切换到指定 Tab：
     *  - 替换 Fragment
     *  - 更新底部按钮样式
     */
    private void switchToTab(Tab tab) {
        if (tab == currentTab) {
            // 已经在当前 Tab，无需重复切换
            return;
        }

        currentTab = tab;

        // 1. 切 Fragment
        Fragment target = (tab == Tab.TODAY) ? todayFragment : forecastFragment;
        switchFragment(target);

        // 2. 更新底部导航 UI
        updateBottomTabState(tab);
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void updateBottomTabState(Tab tab) {
        if (tab == Tab.TODAY) {
            // 今天选中
            tabToday.setBackgroundResource(R.drawable.bg_bottom_nav_item);
            tabForecast.setBackgroundColor(Color.TRANSPARENT);

            tabTodayLabel.setTextColor(COLOR_TEXT_SELECTED);
            tabForecastLabel.setTextColor(COLOR_TEXT_UNSELECTED);
            // 如果以后想让图标颜色也区分，可以在这里一起改

        } else {
            // 预报选中
            tabForecast.setBackgroundResource(R.drawable.bg_bottom_nav_item);
            tabToday.setBackgroundColor(Color.TRANSPARENT);

            tabForecastLabel.setTextColor(COLOR_TEXT_SELECTED);
            tabTodayLabel.setTextColor(COLOR_TEXT_UNSELECTED);
        }
    }

    // endregion
}
