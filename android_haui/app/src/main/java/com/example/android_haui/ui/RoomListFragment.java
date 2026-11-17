package com.example.android_haui.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.android_haui.R;
import com.example.android_haui.model.Room;
import com.example.android_haui.repository.RepositoryCallback;
import com.example.android_haui.repository.RoomRepository;
import com.example.android_haui.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment hiển thị Danh sách phòng học từ CSDL tích hợp tìm kiếm trực tiếp và bộ lọc trạng thái.
 */
public class RoomListFragment extends Fragment {

    private static final String TAG = "RoomListFragment";

    private RoomRepository roomRepository;

    private View searchBar;
    private ImageView ivSearchIcon;
    private EditText etSearchRoom;
    private TextView btnClearSearch;
    private View layoutSuggestions;

    private LinearLayout layoutRoomsList;
    private TextView tvEmptyRoomsList;
    private TextView tvRoomsListHeading;
    private TextView tvRoomsCount;

    private TextView filterAll;
    private TextView filterEmpty;
    private TextView filterInUse;
    private TextView filterMaintenance;

    private String currentFilter = null; // null: Tất cả
    private String currentQuery = "";
    private List<Room> allRooms = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_room_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomRepository = RoomRepository.getInstance(requireContext());

        HeaderHelper.setup(
                view,
                R.string.room_list_title,
                R.string.room_list_subtitle
        );
        initViews(view);
        setupListeners(view);
        loadRoomsData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRoomsData();
    }

    private void initViews(View view) {
        searchBar = view.findViewById(R.id.searchBar);
        ivSearchIcon = view.findViewById(R.id.ivSearchIcon);
        etSearchRoom = view.findViewById(R.id.etSearchRoom);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        layoutSuggestions = view.findViewById(R.id.layoutSuggestions);

        layoutRoomsList = view.findViewById(R.id.layoutRoomsList);
        tvEmptyRoomsList = view.findViewById(R.id.tvEmptyRoomsList);
        tvRoomsListHeading = view.findViewById(R.id.tvRoomsListHeading);
        tvRoomsCount = view.findViewById(R.id.tvRoomsCount);

        filterAll = view.findViewById(R.id.filterAll);
        filterEmpty = view.findViewById(R.id.filterEmpty);
        filterInUse = view.findViewById(R.id.filterInUse);
        filterMaintenance = view.findViewById(R.id.filterMaintenance);
    }

    private void setupListeners(View view) {
        // 1. Khi ấn vào khung searchBar hoặc EditText sẽ mở bàn phím gõ trực tiếp
        if (searchBar != null && etSearchRoom != null) {
            searchBar.setOnClickListener(v -> {
                etSearchRoom.requestFocus();
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etSearchRoom, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }

        if (etSearchRoom != null) {
            etSearchRoom.setOnFocusChangeListener((v, hasFocus) -> updateSearchBarAppearance(hasFocus));

            etSearchRoom.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s != null ? s.toString().trim() : "";
                    updateSearchBarAppearance(etSearchRoom.hasFocus());
                    displayFilteredRooms();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            etSearchRoom.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    etSearchRoom.clearFocus();
                    return true;
                }
                return false;
            });
        }

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (etSearchRoom != null) {
                    etSearchRoom.setText("");
                    etSearchRoom.clearFocus();
                }
                setFilter(null);
                hideKeyboard();
            });
        }

        // 2. Các chip gợi ý tìm kiếm nhanh
        View chipLab = view.findViewById(R.id.chipLab);
        if (chipLab != null) {
            chipLab.setOnClickListener(v -> applySuggestion(getString(R.string.suggestion_lab)));
        }

        View chip101 = view.findViewById(R.id.chip101);
        if (chip101 != null) {
            chip101.setOnClickListener(v -> applySuggestion(getString(R.string.suggestion_101)));
        }

        View chipComputer = view.findViewById(R.id.chipComputer);
        if (chipComputer != null) {
            chipComputer.setOnClickListener(v -> applySuggestion(getString(R.string.suggestion_computer)));
        }

        View chipAll = view.findViewById(R.id.chipAll);
        if (chipAll != null) {
            chipAll.setOnClickListener(v -> applySuggestion(""));
        }

        // 3. Các nút lọc trạng thái
        if (filterAll != null) {
            filterAll.setOnClickListener(v -> setFilter(null));
        }
        if (filterEmpty != null) {
            filterEmpty.setOnClickListener(v -> setFilter(Constants.STATUS_EMPTY_VN));
        }
        if (filterInUse != null) {
            filterInUse.setOnClickListener(v -> setFilter(Constants.STATUS_IN_USE_VN));
        }
        if (filterMaintenance != null) {
            filterMaintenance.setOnClickListener(v -> setFilter(Constants.STATUS_MAINTENANCE_VN));
        }
    }

    private void applySuggestion(String text) {
        if (etSearchRoom != null) {
            etSearchRoom.setText(text);
            etSearchRoom.setSelection(text.length());
        }
    }

    private void updateSearchBarAppearance(boolean hasFocus) {
        if (!isAdded()) return;

        boolean isActive = hasFocus || !currentQuery.isEmpty();

        if (searchBar != null) {
            searchBar.setBackground(ContextCompat.getDrawable(
                    requireContext(),
                    isActive ? R.drawable.bg_search_bar_active : R.drawable.bg_search_bar
            ));
        }

        if (ivSearchIcon != null) {
            ivSearchIcon.setColorFilter(ContextCompat.getColor(
                    requireContext(),
                    isActive ? R.color.text_dark : R.color.text_gray
            ));
        }

        if (layoutSuggestions != null) {
            layoutSuggestions.setVisibility(isActive ? View.VISIBLE : View.GONE);
        }

        if (btnClearSearch != null) {
            boolean showReset = !currentQuery.isEmpty() || currentFilter != null;
            btnClearSearch.setVisibility(showReset ? View.VISIBLE : View.GONE);
        }

        if (tvRoomsListHeading != null) {
            tvRoomsListHeading.setText(!currentQuery.isEmpty() ? R.string.search_results_label : R.string.all_rooms);
        }
    }

    private void hideKeyboard() {
        if (getContext() == null || etSearchRoom == null) return;
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearchRoom.getWindowToken(), 0);
        }
    }

    public void setFilter(String filter) {
        this.currentFilter = filter;
        updateFilterUI();
        updateSearchBarAppearance(etSearchRoom != null && etSearchRoom.hasFocus());
        displayFilteredRooms();
    }

    private void updateFilterUI() {
        if (!isAdded()) return;

        int activeBg = R.drawable.bg_filter_active;
        int inactiveBg = R.drawable.bg_filter_inactive;
        int activeText = ContextCompat.getColor(requireContext(), R.color.white);
        int inactiveText = ContextCompat.getColor(requireContext(), R.color.text_filter_inactive);

        boolean isAll = (currentFilter == null);
        boolean isEmpty = Constants.STATUS_EMPTY_VN.equalsIgnoreCase(currentFilter) || Constants.EMPTY.equalsIgnoreCase(currentFilter);
        boolean isInUse = Constants.STATUS_IN_USE_VN.equalsIgnoreCase(currentFilter) || Constants.IN_USE.equalsIgnoreCase(currentFilter);
        boolean isMaint = Constants.STATUS_MAINTENANCE_VN.equalsIgnoreCase(currentFilter) || Constants.MAINTENANCE.equalsIgnoreCase(currentFilter);

        applyChipStyle(filterAll, isAll, activeBg, inactiveBg, activeText, inactiveText);
        applyChipStyle(filterEmpty, isEmpty, activeBg, inactiveBg, activeText, inactiveText);
        applyChipStyle(filterInUse, isInUse, activeBg, inactiveBg, activeText, inactiveText);
        applyChipStyle(filterMaintenance, isMaint, activeBg, inactiveBg, activeText, inactiveText);
    }

    private void applyChipStyle(TextView chip, boolean isActive, int activeBg, int inactiveBg, int activeText, int inactiveText) {
        if (chip == null) return;
        chip.setBackground(ContextCompat.getDrawable(requireContext(), isActive ? activeBg : inactiveBg));
        chip.setTextColor(isActive ? activeText : inactiveText);
    }

    public void loadRoomsData() {
        if (!isAdded()) return;

        roomRepository.getAllRooms(new RepositoryCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> rooms) {
                if (!isAdded()) return;
                allRooms = rooms != null ? rooms : new ArrayList<>();
                displayFilteredRooms();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi nạp danh sách phòng: " + e.getMessage(), e);
            }
        });
    }

    private boolean matchesSearchQuery(Room room) {
        if (currentQuery == null || currentQuery.isEmpty()) return true;
        String q = currentQuery.toLowerCase(Locale.getDefault());

        if (room.getRoomCode() != null && room.getRoomCode().toLowerCase(Locale.getDefault()).contains(q)) {
            return true;
        }
        if (room.getRoomName() != null && room.getRoomName().toLowerCase(Locale.getDefault()).contains(q)) {
            return true;
        }
        if (room.getRoomType() != null && room.getRoomType().toLowerCase(Locale.getDefault()).contains(q)) {
            return true;
        }
        if (("tầng " + room.getFloor()).contains(q) || String.valueOf(room.getFloor()).equals(q)) {
            return true;
        }
        return false;
    }

    private boolean matchesStatusFilter(Room r) {
        if (currentFilter == null) return true;
        return currentFilter.equalsIgnoreCase(r.getStatus()) ||
                (Constants.STATUS_EMPTY_VN.equalsIgnoreCase(currentFilter) && Constants.EMPTY.equalsIgnoreCase(r.getStatus())) ||
                (Constants.STATUS_IN_USE_VN.equalsIgnoreCase(currentFilter) && Constants.IN_USE.equalsIgnoreCase(r.getStatus())) ||
                (Constants.STATUS_MAINTENANCE_VN.equalsIgnoreCase(currentFilter) && Constants.MAINTENANCE.equalsIgnoreCase(r.getStatus()));
    }

    private void displayFilteredRooms() {
        if (layoutRoomsList == null || getContext() == null) return;

        layoutRoomsList.removeAllViews();

        List<Room> filtered = new ArrayList<>();
        for (Room r : allRooms) {
            if (matchesStatusFilter(r) && matchesSearchQuery(r)) {
                filtered.add(r);
            }
        }

        if (tvRoomsCount != null) {
            tvRoomsCount.setText(getString(R.string.rooms_count_format, filtered.size()));
        }

        if (filtered.isEmpty()) {
            if (tvEmptyRoomsList != null) {
                tvEmptyRoomsList.setVisibility(View.VISIBLE);
                layoutRoomsList.addView(tvEmptyRoomsList);
            }
            return;
        }

        if (tvEmptyRoomsList != null) {
            tvEmptyRoomsList.setVisibility(View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Room room : filtered) {
            View itemView = inflater.inflate(R.layout.item_recent_room, layoutRoomsList, false);
            RoomItemHelper.bindRoomCard(
                    requireContext(),
                    itemView,
                    room,
                    v -> {
                        Intent intent = new Intent(requireActivity(), RoomDetailActivity.class);
                        intent.putExtra(Constants.EXTRA_ROOM_CODE, room.getRoomCode());
                        intent.putExtra(Constants.EXTRA_ROOM_ITEM, room);
                        startActivity(intent);
                    }
            );
            layoutRoomsList.addView(itemView);
        }
    }
}
