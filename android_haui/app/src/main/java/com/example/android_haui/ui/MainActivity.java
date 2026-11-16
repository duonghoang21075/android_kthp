package com.example.android_haui.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.android_haui.R;
import com.example.android_haui.utils.Constants;

/**
 * Host Activity chính chứa Bottom Navigation cố định và chuyển đổi mượt mà giữa 4 màn hình:
 * 1. Tổng quan (OverviewFragment)
 * 2. Phòng học (RoomListFragment)
 * 3. Thống kê (StatisticsFragment)
 * 4. Quản lý (ManageFragment)
 *
 * Chuyển đổi trạng thái thông qua FragmentManager (show / hide) giúp:
 * - Tối ưu tốc độ hiển thị tức thì (0ms lag, không tải lại Activity).
 * - Giữ nguyên trạng thái cuộn, tìm kiếm, bộ lọc của từng màn hình.
 * - Bottom Navigation luôn giữ vị trí ổn định, nâng cao trải nghiệm người dùng (UX).
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavHelper bottomNavHelper;

    private OverviewFragment overviewFragment;
    private RoomListFragment roomListFragment;
    private StatisticsFragment statisticsFragment;
    private ManageFragment manageFragment;
    private Fragment activeFragment;

    private int currentTab = Constants.TAB_OVERVIEW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBottomNavigation();
        setupBackNavigation();

        int startTab = getIntent() != null ? getIntent().getIntExtra(Constants.EXTRA_SELECTED_TAB, Constants.TAB_OVERVIEW) : Constants.TAB_OVERVIEW;
        selectTab(startTab);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.hasExtra(Constants.EXTRA_SELECTED_TAB)) {
            int tab = intent.getIntExtra(Constants.EXTRA_SELECTED_TAB, Constants.TAB_OVERVIEW);
            selectTab(tab);
        }
    }

    private void initBottomNavigation() {
        View bottomNavView = findViewById(R.id.layoutBottomNav);
        if (bottomNavView != null) {
            bottomNavHelper = BottomNavHelper.setup(bottomNavView, Constants.TAB_OVERVIEW, this::selectTab);
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentTab != Constants.TAB_OVERVIEW) {
                    selectTab(Constants.TAB_OVERVIEW);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * Chuyển đổi giữa các tab [Tổng quan, Phòng học, Thống kê, Quản lý].
     * Chỉ nội dung giao diện chuyển đổi, Bottom Navigation giữ nguyên vị trí.
     */
    public void selectTab(int tabIndex) {
        Fragment targetFragment;
        switch (tabIndex) {
            case Constants.TAB_ROOMS:
                if (roomListFragment == null) roomListFragment = new RoomListFragment();
                targetFragment = roomListFragment;
                break;
            case Constants.TAB_STATS:
                if (statisticsFragment == null) statisticsFragment = new StatisticsFragment();
                targetFragment = statisticsFragment;
                break;
            case Constants.TAB_MANAGE:
                if (manageFragment == null) manageFragment = new ManageFragment();
                targetFragment = manageFragment;
                break;
            case Constants.TAB_OVERVIEW:
            default:
                tabIndex = Constants.TAB_OVERVIEW;
                if (overviewFragment == null) overviewFragment = new OverviewFragment();
                targetFragment = overviewFragment;
                break;
        }

        if (targetFragment == activeFragment && targetFragment.isAdded()) {
            if (bottomNavHelper != null) {
                bottomNavHelper.setSelectedTab(tabIndex);
            }
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        if (activeFragment != null) {
            transaction.hide(activeFragment);
        }

        if (!targetFragment.isAdded()) {
            transaction.add(R.id.fragment_container, targetFragment, "tab_" + tabIndex);
        } else {
            transaction.show(targetFragment);
        }

        transaction.commitAllowingStateLoss();

        activeFragment = targetFragment;
        currentTab = tabIndex;

        if (bottomNavHelper != null) {
            bottomNavHelper.setSelectedTab(tabIndex);
        }
    }

    public int getCurrentTab() {
        return currentTab;
    }
}
