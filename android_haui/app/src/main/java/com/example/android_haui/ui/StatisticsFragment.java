package com.example.android_haui.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.android_haui.R;
import com.example.android_haui.database.RoomDao;
import com.example.android_haui.repository.RepositoryCallback;
import com.example.android_haui.repository.RoomRepository;

import java.util.List;
import java.util.Locale;

/**
 * Fragment hiển thị Thống kê tổng hợp số lượng, tỷ lệ phòng học theo trạng thái, tầng và loại phòng
 * lấy trực tiếp từ CSDL SQLite.
 */
public class StatisticsFragment extends Fragment {

    private static final String TAG = "StatisticsFragment";

    private RoomRepository roomRepository;

    private TextView tvStatsTotalRooms;
    private TextView tvStatsTotalCenter;
    private TextView tvStatsEmptyPercent;
    private TextView tvStatsInUsePercent;
    private TextView tvStatsMaintPercent;
    private LinearLayout layoutFloorContainer;
    private LinearLayout layoutTypeContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomRepository = RoomRepository.getInstance(requireContext());

        HeaderHelper.setup(view, R.string.stats_title, R.string.stats_subtitle);
        initViews(view);
        loadStatistics();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }

    private void initViews(View view) {
        tvStatsTotalRooms = view.findViewById(R.id.tvStatsTotalRooms);
        tvStatsTotalCenter = view.findViewById(R.id.tvStatsTotalCenter);
        tvStatsEmptyPercent = view.findViewById(R.id.tvStatsEmptyPercent);
        tvStatsInUsePercent = view.findViewById(R.id.tvStatsInUsePercent);
        tvStatsMaintPercent = view.findViewById(R.id.tvStatsMaintPercent);
        layoutFloorContainer = view.findViewById(R.id.layoutFloorContainer);
        layoutTypeContainer = view.findViewById(R.id.layoutTypeContainer);
    }

    public void loadStatistics() {
        if (!isAdded()) return;

        // 1. Lấy thống kê tổng quan (Tổng số phòng, Trống, Đang sử dụng, Bảo trì)
        roomRepository.getRoomStatistics(new RepositoryCallback<RoomRepository.RoomStatistics>() {
            @Override
            public void onSuccess(RoomRepository.RoomStatistics stats) {
                if (!isAdded()) return;

                int total = stats.totalRooms;
                if (tvStatsTotalRooms != null) {
                    tvStatsTotalRooms.setText(String.valueOf(total));
                }
                if (tvStatsTotalCenter != null) {
                    tvStatsTotalCenter.setText(String.valueOf(total));
                }

                if (total > 0) {
                    int emptyPct = (int) Math.round((stats.emptyRooms * 100.0) / total);
                    int inUsePct = (int) Math.round((stats.inUseRooms * 100.0) / total);
                    int maintPct = (int) Math.round((stats.maintenanceRooms * 100.0) / total);

                    if (tvStatsEmptyPercent != null) {
                        tvStatsEmptyPercent.setText(String.format(Locale.getDefault(), "%d phòng · %d%%", stats.emptyRooms, emptyPct));
                    }
                    if (tvStatsInUsePercent != null) {
                        tvStatsInUsePercent.setText(String.format(Locale.getDefault(), "%d phòng · %d%%", stats.inUseRooms, inUsePct));
                    }
                    if (tvStatsMaintPercent != null) {
                        tvStatsMaintPercent.setText(String.format(Locale.getDefault(), "%d phòng · %d%%", stats.maintenanceRooms, maintPct));
                    }
                }

                // 2. Tải thống kê phân bố theo tầng từ CSDL
                loadFloorStatistics(total);

                // 3. Tải thống kê phân bố theo loại phòng từ CSDL
                loadTypeStatistics(total);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi lấy dữ liệu thống kê tổng quát: " + e.getMessage(), e);
            }
        });
    }

    private void loadFloorStatistics(int totalRooms) {
        roomRepository.getFloorStatistics(new RepositoryCallback<List<RoomDao.FloorStat>>() {
            @Override
            public void onSuccess(List<RoomDao.FloorStat> floorStats) {
                if (!isAdded() || floorStats == null || floorStats.isEmpty() || layoutFloorContainer == null) return;
                renderFloorStats(floorStats, totalRooms);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi lấy thống kê tầng: " + e.getMessage(), e);
            }
        });
    }

    private void loadTypeStatistics(int totalRooms) {
        roomRepository.getTypeStatistics(new RepositoryCallback<List<RoomDao.TypeStat>>() {
            @Override
            public void onSuccess(List<RoomDao.TypeStat> typeStats) {
                if (!isAdded() || typeStats == null || typeStats.isEmpty() || layoutTypeContainer == null) return;
                renderTypeStats(typeStats, totalRooms);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi lấy thống kê loại phòng: " + e.getMessage(), e);
            }
        });
    }

    private void renderFloorStats(List<RoomDao.FloorStat> floorStats, int totalRooms) {
        layoutFloorContainer.removeAllViews();
        int count = floorStats.size();

        for (int i = 0; i < count; i++) {
            RoomDao.FloorStat stat = floorStats.get(i);
            View row = createStatRow("Tầng " + stat.floor, stat.roomCount, totalRooms, i == count - 1);
            layoutFloorContainer.addView(row);
        }
    }

    private void renderTypeStats(List<RoomDao.TypeStat> typeStats, int totalRooms) {
        layoutTypeContainer.removeAllViews();
        int count = typeStats.size();

        for (int i = 0; i < count; i++) {
            RoomDao.TypeStat stat = typeStats.get(i);
            String label = stat.type;
            if (label != null && !label.toLowerCase().startsWith("phòng")) {
                label = "Phòng " + label;
            }
            View row = createStatRow(label, stat.roomCount, totalRooms, i == count - 1);
            layoutTypeContainer.addView(row);
        }
    }

    private View createStatRow(String label, int count, int totalRooms, boolean isLast) {
        LinearLayout rowLayout = new LinearLayout(requireContext());
        rowLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (!isLast) {
            rowParams.bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);
        }
        rowLayout.setLayoutParams(rowParams);

        RelativeLayout topLayout = new RelativeLayout(requireContext());
        topLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tvLabel = new TextView(requireContext());
        tvLabel.setText(label);
        tvLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_filter_inactive));
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        RelativeLayout.LayoutParams labelParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        tvLabel.setLayoutParams(labelParams);
        topLayout.addView(tvLabel);

        TextView tvCount = new TextView(requireContext());
        tvCount.setText(String.format(Locale.getDefault(), "%d phòng", count));
        tvCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvCount.setTypeface(null, Typeface.BOLD);
        RelativeLayout.LayoutParams countParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        countParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        tvCount.setLayoutParams(countParams);
        topLayout.addView(tvCount);

        rowLayout.addView(topLayout);

        LinearLayout trackLayout = new LinearLayout(requireContext());
        trackLayout.setOrientation(LinearLayout.HORIZONTAL);
        trackLayout.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_bar_track));
        trackLayout.setWeightSum(100f);
        LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (8 * getResources().getDisplayMetrics().density));
        trackParams.topMargin = (int) (7 * getResources().getDisplayMetrics().density);
        trackLayout.setLayoutParams(trackParams);

        float pct = totalRooms > 0 ? ((float) count * 100f / totalRooms) : 0f;
        if (pct < 1f && count > 0) pct = 1f;
        float remain = Math.max(0f, 100f - pct);

        View progressView = new View(requireContext());
        progressView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_bar_progress));
        progressView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, pct));
        trackLayout.addView(progressView);

        if (remain > 0) {
            View remainView = new View(requireContext());
            remainView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, remain));
            trackLayout.addView(remainView);
        }

        rowLayout.addView(trackLayout);
        return rowLayout;
    }
}
