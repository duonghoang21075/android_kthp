package com.example.android_haui.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_haui.R;
import com.example.android_haui.model.ManagerInfo;
import com.example.android_haui.repository.RepositoryCallback;
import com.example.android_haui.repository.RoomRepository;

/**
 * Fragment hiển thị thông tin tài khoản người quản lý kết nối trực tiếp CSDL.
 */
public class ManageFragment extends Fragment {

    private static final String TAG = "ManageFragment";

    private RoomRepository roomRepository;

    private TextView tvStaffCode;
    private TextView tvDepartment;
    private TextView tvEmail;
    private TextView tvRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomRepository = RoomRepository.getInstance(requireContext());

        HeaderHelper.setup(view, R.string.manage_title, R.string.manage_subtitle);
        initViews(view);
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews(View view) {
        tvStaffCode = view.findViewById(R.id.tvStaffCode);
        tvDepartment = view.findViewById(R.id.tvDepartment);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvRole = view.findViewById(R.id.tvRole);
    }

    public void loadData() {
        if (!isAdded()) return;

        // Tải thông tin người quản lý từ SQLite (bảng manager_info)
        roomRepository.getManagerInfo(new RepositoryCallback<ManagerInfo>() {
            @Override
            public void onSuccess(ManagerInfo info) {
                if (!isAdded() || info == null) return;

                if (tvStaffCode != null && info.getStaffCode() != null) {
                    tvStaffCode.setText(info.getStaffCode());
                }
                if (tvDepartment != null && info.getDepartmentName() != null) {
                    tvDepartment.setText(info.getDepartmentName());
                }
                if (tvEmail != null && info.getEmail() != null) {
                    tvEmail.setText(info.getEmail());
                }
                if (tvRole != null && info.getRole() != null) {
                    tvRole.setText(info.getRole());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Lỗi tải thông tin quản lý: " + e.getMessage(), e);
            }
        });
    }
}
