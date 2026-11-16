package com.example.android_haui.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_haui.R;
import com.example.android_haui.model.Room;
import com.example.android_haui.repository.RepositoryCallback;
import com.example.android_haui.repository.RoomRepository;
import com.example.android_haui.utils.Constants;

import java.util.List;

/**
 * Fragment hiển thị Tổng quan thông tin phòng học và thống kê nhanh.
 */
public class OverviewFragment extends Fragment {

    private static final String TAG = "OverviewFragment";

    private RoomRepository roomRepository;

    private TextView tvTotalRooms;
    private TextView tvTotalRoomsDesc;
    private TextView tvEmptyCount;
    private TextView tvInUseCount;
    private TextView tvMaintenanceCount;
    private LinearLayout layoutRecentRooms;
    private TextView tvEmptyRecent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomRepository = RoomRepository.getInstance(requireContext());

        HeaderHelper.setup(view, R.string.overview_title, R.string.overview_subtitle);
        initViews(view);
        setupListeners(view);
        loadDashboardData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void initViews(View view) {
        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
        tvTotalRoomsDesc = view.findViewById(R.id.tvTotalRoomsDesc);
        tvEmptyCount = view.findViewById(R.id.tvEmptyCount);
        tvInUseCount = view.findViewById(R.id.tvInUseCount);
        tvMaintenanceCount = view.findViewById(R.id.tvMaintenanceCount);
        layoutRecentRooms = view.findViewById(R.id.layoutRecentRooms);
        tvEmptyRecent = view.findViewById(R.id.tvEmptyRecent);
    }

    private void setupListeners(View view) {
        View btnQuickRoomList = view.findViewById(R.id.btnQuickRoomList);
        if (btnQuickRoomList != null) {
            btnQuickRoomList.setOnClickListener(v -> switchToTab(Constants.TAB_ROOMS));
        }

        View btnQuickStats = view.findViewById(R.id.btnQuickStats);
        if (btnQuickStats != null) {
            btnQuickStats.setOnClickListener(v -> switchToTab(Constants.TAB_STATS));
        }

        View tvSeeAllRecent = view.findViewById(R.id.tvSeeAllRecent);
        if (tvSeeAllRecent != null) {
            tvSeeAllRecent.setOnClickListener(v -> switchToTab(Constants.TAB_ROOMS));
        }

        View layoutEmptyStatus = view.findViewById(R.id.layoutEmptyStatus);
        if (layoutEmptyStatus != null) {
            layoutEmptyStatus.setOnClickListener(v -> switchToTab(Constants.TAB_ROOMS));
        }

        View layoutInUseStatus = view.findViewById(R.id.layoutInUseStatus);
        if (layoutInUseStatus != null) {
            layoutInUseStatus.setOnClickListener(v -> switchToTab(Constants.TAB_ROOMS));
        }

        View layoutMaintenanceStatus = view.findViewById(R.id.layoutMaintenanceStatus);
        if (layoutMaintenanceStatus != null) {
            layoutMaintenanceStatus.setOnClickListener(v -> switchToTab(Constants.TAB_ROOMS));
        }
    }

    private void switchToTab(int tabIndex) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).selectTab(tabIndex);
        }
    }

    public void loadDashboardData() {
        if (!isAdded()) return;

        // 1. Lấy dữ liệu thống kê tổng số phòng, số tầng, loại phòng, trạng thái trống/đang dùng/bảo trì
        roomRepository.getRoomStatistics(new RepositoryCallback<RoomRepository.RoomStatistics>() {
            @Override
            public void onSuccess(RoomRepository.RoomStatistics stats) {
                if (!isAdded()) return;

                if (tvTotalRooms != null) {
                    tvTotalRooms.setText(String.valueOf(stats.totalRooms));
                }

                if (tvTotalRoomsDesc != null) {
                    int floors = stats.floorCount > 0 ? stats.floorCount : 1;
                    int types = stats.typeCount > 0 ? stats.typeCount : 1;
                    String desc = floors + " tầng · " + types + " loại phòng";
                    tvTotalRoomsDesc.setText(desc);
                }

                if (tvEmptyCount != null) {
                    tvEmptyCount.setText(String.valueOf(stats.emptyRooms));
                }

                if (tvInUseCount != null) {
                    tvInUseCount.setText(String.valueOf(stats.inUseRooms));
                }

                if (tvMaintenanceCount != null) {
                    tvMaintenanceCount.setText(String.valueOf(stats.maintenanceRooms));
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi lấy thống kê phòng: " + e.getMessage(), e);
            }
        });

        // 2. Lấy danh sách phòng cập nhật gần đây từ CSDL
        roomRepository.getRecentRooms(5, new RepositoryCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> rooms) {
                if (!isAdded()) return;
                displayRecentRooms(rooms);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi nạp danh sách phòng gần đây: " + e.getMessage(), e);
            }
        });
    }

    private void displayRecentRooms(List<Room> rooms) {
        if (layoutRecentRooms == null || getContext() == null) return;

        layoutRecentRooms.removeAllViews();

        if (rooms == null || rooms.isEmpty()) {
            if (tvEmptyRecent != null) {
                tvEmptyRecent.setVisibility(View.VISIBLE);
                layoutRecentRooms.addView(tvEmptyRecent);
            }
            return;
        }

        if (tvEmptyRecent != null) {
            tvEmptyRecent.setVisibility(View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Room room : rooms) {
            View itemView = inflater.inflate(R.layout.item_recent_room, layoutRecentRooms, false);
            RoomItemHelper.bindRoomCard(requireContext(), itemView, room, v -> {
                Intent intent = new Intent(requireActivity(), RoomDetailActivity.class);
                intent.putExtra(Constants.EXTRA_ROOM_CODE, room.getRoomCode());
                intent.putExtra(Constants.EXTRA_ROOM_ITEM, room);
                startActivity(intent);
            });
            layoutRecentRooms.addView(itemView);
        }
    }
}
