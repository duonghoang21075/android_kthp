package com.example.android_haui.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.android_haui.R;
import com.example.android_haui.utils.Constants;

/**
 * Lớp điều khiển Bottom Navigation tái sử dụng trên các màn hình.
 * Cập nhật trạng thái tab trực quan (indicator, icon tint, text color, typeface).
 */
public class BottomNavHelper {

    public interface OnTabSelectedListener {
        void onTabSelected(int tabIndex);
    }

    private final Context context;
    private final View rootView;
    private final OnTabSelectedListener listener;

    private LinearLayout navTabOverview;
    private FrameLayout navIndicatorOverview;
    private ImageView navIconOverview;
    private TextView navTextOverview;

    private LinearLayout navTabRooms;
    private FrameLayout navIndicatorRooms;
    private ImageView navIconRooms;
    private TextView navTextRooms;

    private LinearLayout navTabStats;
    private FrameLayout navIndicatorStats;
    private ImageView navIconStats;
    private TextView navTextStats;

    private LinearLayout navTabManage;
    private FrameLayout navIndicatorManage;
    private ImageView navIconManage;
    private TextView navTextManage;

    private int currentTab = Constants.TAB_OVERVIEW;

    public static BottomNavHelper setup(View rootView, int initialTab, OnTabSelectedListener listener) {
        BottomNavHelper helper = new BottomNavHelper(rootView, listener);
        helper.setSelectedTab(initialTab);
        return helper;
    }

    private BottomNavHelper(View rootView, OnTabSelectedListener listener) {
        this.rootView = rootView;
        this.context = rootView.getContext();
        this.listener = listener;

        initViews();
        setupListeners();
    }

    private void initViews() {
        navTabOverview = rootView.findViewById(R.id.navTabOverview);
        navIndicatorOverview = rootView.findViewById(R.id.navIndicatorOverview);
        navIconOverview = rootView.findViewById(R.id.navIconOverview);
        navTextOverview = rootView.findViewById(R.id.navTextOverview);

        navTabRooms = rootView.findViewById(R.id.navTabRooms);
        navIndicatorRooms = rootView.findViewById(R.id.navIndicatorRooms);
        navIconRooms = rootView.findViewById(R.id.navIconRooms);
        navTextRooms = rootView.findViewById(R.id.navTextRooms);

        navTabStats = rootView.findViewById(R.id.navTabStats);
        navIndicatorStats = rootView.findViewById(R.id.navIndicatorStats);
        navIconStats = rootView.findViewById(R.id.navIconStats);
        navTextStats = rootView.findViewById(R.id.navTextStats);

        navTabManage = rootView.findViewById(R.id.navTabManage);
        navIndicatorManage = rootView.findViewById(R.id.navIndicatorManage);
        navIconManage = rootView.findViewById(R.id.navIconManage);
        navTextManage = rootView.findViewById(R.id.navTextManage);
    }

    private void setupListeners() {
        if (navTabOverview != null) {
            navTabOverview.setOnClickListener(v -> handleTabClick(Constants.TAB_OVERVIEW));
        }
        if (navTabRooms != null) {
            navTabRooms.setOnClickListener(v -> handleTabClick(Constants.TAB_ROOMS));
        }
        if (navTabStats != null) {
            navTabStats.setOnClickListener(v -> handleTabClick(Constants.TAB_STATS));
        }
        if (navTabManage != null) {
            navTabManage.setOnClickListener(v -> handleTabClick(Constants.TAB_MANAGE));
        }
    }

    private void handleTabClick(int tabIndex) {
        if (currentTab != tabIndex) {
            setSelectedTab(tabIndex);
            if (listener != null) {
                listener.onTabSelected(tabIndex);
            }
        }
    }

    public void setSelectedTab(int tabIndex) {
        this.currentTab = tabIndex;

        int activeColor = ContextCompat.getColor(context, R.color.card_blue_bg);
        int inactiveColor = ContextCompat.getColor(context, R.color.text_gray);

        // Tab Overview
        updateTabState(tabIndex == Constants.TAB_OVERVIEW,
                navIndicatorOverview, navIconOverview, navTextOverview, activeColor, inactiveColor);

        // Tab Rooms
        updateTabState(tabIndex == Constants.TAB_ROOMS,
                navIndicatorRooms, navIconRooms, navTextRooms, activeColor, inactiveColor);

        // Tab Stats
        updateTabState(tabIndex == Constants.TAB_STATS,
                navIndicatorStats, navIconStats, navTextStats, activeColor, inactiveColor);

        // Tab Manage
        updateTabState(tabIndex == Constants.TAB_MANAGE,
                navIndicatorManage, navIconManage, navTextManage, activeColor, inactiveColor);
    }

    private void updateTabState(boolean isSelected,
                                FrameLayout indicator,
                                ImageView icon,
                                TextView label,
                                int activeColor,
                                int inactiveColor) {
        if (indicator != null) {
            if (isSelected) {
                indicator.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_nav_active));
            } else {
                indicator.setBackground(null);
            }
        }

        if (icon != null) {
            icon.setColorFilter(isSelected ? activeColor : inactiveColor);
        }

        if (label != null) {
            label.setTextColor(isSelected ? activeColor : inactiveColor);
            label.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    public int getCurrentTab() {
        return currentTab;
    }
}
