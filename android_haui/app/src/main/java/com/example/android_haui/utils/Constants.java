package com.example.android_haui.utils;

/**
 * Lớp chứa các hằng số dùng chung trong toàn bộ ứng dụng.
 */
public final class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
    }

    // 1. Cấu hình CSDL SQLite
    public static final String DATABASE_NAME = "room-management-app.db";
    public static final int DATABASE_VERSION = 2;

    // 2. Tên bảng và các cột
    public static final String TABLE_ROOM = "Room";
    public static final String TABLE_ROOMS = "rooms";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ROOM_CODE = "room_code";
    public static final String COLUMN_ROOM_NAME = "room_name";
    public static final String COLUMN_FLOOR = "floor";
    public static final String COLUMN_ROOM_TYPE = "room_type";
    public static final String COLUMN_CAPACITY = "capacity";
    public static final String COLUMN_EQUIPMENT = "equipment";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_UPDATE_AT = "update_at";

    // Bảng thông tin người quản lý
    public static final String TABLE_MANAGER_INFO = "manager_info";
    public static final String COLUMN_STAFF_CODE = "staff_code";
    public static final String COLUMN_DEPARTMENT_NAME = "department_name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_ROLE = "role";

    // 3. Trạng thái phòng học (hỗ trợ cả mã hằng số và chuỗi tiếng Việt trong CSDL)
    public static final String EMPTY = "EMPTY";
    public static final String IN_USE = "IN_USE";
    public static final String MAINTENANCE = "MAINTENANCE";
    public static final String STATUS_EMPTY_VN = "Trống";
    public static final String STATUS_IN_USE_VN = "Đang sử dụng";
    public static final String STATUS_MAINTENANCE_VN = "Bảo trì";

    // 4. Intent & Bundle Extras
    public static final String EXTRA_ROOM_CODE = "extra_room_code";
    public static final String EXTRA_ROOM_ITEM = "extra_room_item";
    public static final String EXTRA_SELECTED_TAB = "extra_selected_tab";

    // 5. Tab Index cho Bottom Navigation
    public static final int TAB_OVERVIEW = 0;
    public static final int TAB_ROOMS = 1;
    public static final int TAB_STATS = 2;
    public static final int TAB_MANAGE = 3;
}
