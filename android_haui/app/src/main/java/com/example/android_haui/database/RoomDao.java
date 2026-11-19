package com.example.android_haui.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.android_haui.model.ManagerInfo;
import com.example.android_haui.model.Room;
import com.example.android_haui.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) thực hiện các thao tác CRUD trực tiếp trên bảng Room / rooms.
 * Đảm bảo đóng Cursor ngay sau khi đọc (try-with-resources) và dùng selectionArgs chống SQL Injection.
 */
public class RoomDao {

    private static final String TAG = "RoomDao";
    private final DatabaseHelper dbHelper;

    public RoomDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    private String getTableName(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type IN ('table', 'view') AND (name='rooms' OR name='Room')", null)) {
            while (cursor != null && cursor.moveToNext()) {
                String name = cursor.getString(0);
                if ("rooms".equalsIgnoreCase(name)) {
                    return name;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Không thể kiểm tra tên bảng, dùng mặc định: " + e.getMessage());
        }
        return Constants.TABLE_ROOM;
    }

    /**
     * Lấy danh sách toàn bộ phòng học.
     */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = getTableName(db);

        String query = "SELECT * FROM " + table +
                " ORDER BY " + Constants.COLUMN_FLOOR + ", " + Constants.COLUMN_ROOM_CODE;

        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor != null && cursor.moveToNext()) {
                rooms.add(cursorToRoom(cursor));
            }
        }
        return rooms;
    }

    /**
     * Lấy danh sách phòng theo tầng.
     */
    public List<Room> getRoomsByFloor(int floor) {
        List<Room> rooms = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = getTableName(db);

        String query = "SELECT * FROM " + table +
                " WHERE " + Constants.COLUMN_FLOOR + " = ?" +
                " ORDER BY " + Constants.COLUMN_ROOM_CODE;

        String[] selectionArgs = new String[]{String.valueOf(floor)};

        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
            while (cursor != null && cursor.moveToNext()) {
                rooms.add(cursorToRoom(cursor));
            }
        }
        return rooms;
    }

    /**
     * Lấy thông tin phòng theo mã phòng (không phân biệt hoa thường).
     */
    public Room getRoomByCode(String roomCode) {
        if (roomCode == null || roomCode.trim().isEmpty()) return null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = getTableName(db);

        String query = "SELECT * FROM " + table +
                " WHERE UPPER(" + Constants.COLUMN_ROOM_CODE + ") = UPPER(?)" +
                " LIMIT 1";

        String[] selectionArgs = new String[]{roomCode.trim()};

        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToRoom(cursor);
            }
        }
        return null;
    }

    /**
     * Chuẩn hóa giá trị trạng thái về tiếng Việt chuẩn của CSDL.
     */
    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return Constants.STATUS_EMPTY_VN;
        }
        if (Constants.EMPTY.equalsIgnoreCase(status) || Constants.STATUS_EMPTY_VN.equalsIgnoreCase(status)) {
            return Constants.STATUS_EMPTY_VN;
        }
        if (Constants.IN_USE.equalsIgnoreCase(status) || Constants.STATUS_IN_USE_VN.equalsIgnoreCase(status)) {
            return Constants.STATUS_IN_USE_VN;
        }
        if (Constants.MAINTENANCE.equalsIgnoreCase(status) || Constants.STATUS_MAINTENANCE_VN.equalsIgnoreCase(status)) {
            return Constants.STATUS_MAINTENANCE_VN;
        }
        return status.trim();
    }

    /**
     * Thêm mới một phòng học vào CSDL.
     */
    public long insertRoom(Room room) {
        if (room == null || room.getRoomCode() == null || room.getRoomCode().trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String table = getTableName(db);

        String code = room.getRoomCode().trim().toUpperCase();
        String name = (room.getRoomName() != null && !room.getRoomName().trim().isEmpty())
                ? room.getRoomName().trim()
                : ("Phòng " + code);
        String type = (room.getRoomType() != null && !room.getRoomType().trim().isEmpty())
                ? room.getRoomType().trim()
                : "Lý thuyết";
        int floor = room.getFloor() > 0 ? room.getFloor() : 1;
        int capacity = Math.max(0, room.getCapacity());
        String status = normalizeStatus(room.getStatus());

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_ROOM_CODE, code);
        values.put(Constants.COLUMN_ROOM_NAME, name);
        values.put(Constants.COLUMN_FLOOR, floor);
        values.put(Constants.COLUMN_ROOM_TYPE, type);
        values.put(Constants.COLUMN_CAPACITY, capacity);
        values.put(Constants.COLUMN_STATUS, status);

        if (room.getEquipment() != null) {
            values.put(Constants.COLUMN_EQUIPMENT, room.getEquipment().trim());
        } else {
            values.put(Constants.COLUMN_EQUIPMENT, "");
        }

        if (room.getNote() != null) {
            values.put(Constants.COLUMN_NOTE, room.getNote().trim());
        } else {
            values.put(Constants.COLUMN_NOTE, "");
        }

        long updateTime = room.getUpdateAt() > 0 ? room.getUpdateAt() : (System.currentTimeMillis() / 1000);
        values.put(Constants.COLUMN_UPDATE_AT, updateTime);

        return db.insert(table, null, values);
    }

    /**
     * Cập nhật trạng thái của phòng học.
     */
    public int updateRoomStatus(String roomCode, String status) {
        return updateRoomStatus(roomCode, status, null);
    }

    /**
     * Cập nhật trạng thái và ghi chú của phòng học.
     */
    public int updateRoomStatus(String roomCode, String status, String note) {
        if (roomCode == null || status == null) return 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String table = getTableName(db);

        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_STATUS, status);
        if (note != null) {
            values.put(Constants.COLUMN_NOTE, note);
        }
        values.put(Constants.COLUMN_UPDATE_AT, System.currentTimeMillis() / 1000);

        return db.update(
                table,
                values,
                Constants.COLUMN_ROOM_CODE + " = ?",
                new String[]{roomCode.trim()}
        );
    }

    /**
     * Xóa phòng học theo mã.
     */
    public int deleteRoom(String roomCode) {
        if (roomCode == null) return 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String table = getTableName(db);

        return db.delete(
                table,
                Constants.COLUMN_ROOM_CODE + " = ?",
                new String[]{roomCode.trim()}
        );
    }

    /**
     * Tìm kiếm phòng học theo mã phòng.
     */
    public List<Room> searchRooms(String keyword) {
        List<Room> rooms = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = getTableName(db);

        String safeKeyword = keyword != null ? "%" + keyword.trim() + "%" : "%";
        String query = "SELECT * FROM " + table +
                " WHERE " + Constants.COLUMN_ROOM_CODE + " LIKE ?" +
                " ORDER BY " + Constants.COLUMN_FLOOR + ", " + Constants.COLUMN_ROOM_CODE;

        try (Cursor cursor = db.rawQuery(query, new String[]{safeKeyword})) {
            while (cursor != null && cursor.moveToNext()) {
                rooms.add(cursorToRoom(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi tìm kiếm phòng: " + e.getMessage());
        }
        return rooms;
    }

    /**
     * Đếm số phòng học theo trạng thái (hỗ trợ cả tiếng Anh lẫn tiếng Việt).
     */
    public int countRoomsByStatus(String status) {
        if (status == null) return 0;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = getTableName(db);

        String query;
        String[] args;
        if (Constants.EMPTY.equalsIgnoreCase(status) || Constants.STATUS_EMPTY_VN.equalsIgnoreCase(status)) {
            query = "SELECT COUNT(*) FROM " + table + " WHERE " + Constants.COLUMN_STATUS + " IN (?, ?)";
            args = new String[]{Constants.EMPTY, Constants.STATUS_EMPTY_VN};
        } else if (Constants.IN_USE.equalsIgnoreCase(status) || Constants.STATUS_IN_USE_VN.equalsIgnoreCase(status)) {
            query = "SELECT COUNT(*) FROM " + table + " WHERE " + Constants.COLUMN_STATUS + " IN (?, ?)";
            args = new String[]{Constants.IN_USE, Constants.STATUS_IN_USE_VN};
        } else if (Constants.MAINTENANCE.equalsIgnoreCase(status) || Constants.STATUS_MAINTENANCE_VN.equalsIgnoreCase(status)) {
            query = "SELECT COUNT(*) FROM " + table + " WHERE " + Constants.COLUMN_STATUS + " IN (?, ?)";
            args = new String[]{Constants.MAINTENANCE, Constants.STATUS_MAINTENANCE_VN};
        } else {
            query = "SELECT COUNT(*) FROM " + table + " WHERE " + Constants.COLUMN_STATUS + " = ?";
            args = new String[]{status};
        }

        try (Cursor cursor = db.rawQuery(query, args)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đếm số phòng theo trạng thái: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Đếm tổng số phòng học trong hệ thống.
     */
    public int getTotalRoomCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = getTableName(db);
        String query = "SELECT COUNT(*) FROM " + table;

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đếm tổng số phòng: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Đếm số tầng phân biệt có trong CSDL.
     */
    public int getDistinctFloorCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = getTableName(db);
        String query = "SELECT COUNT(DISTINCT " + Constants.COLUMN_FLOOR + ") FROM " + table;

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đếm số tầng: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Đếm số loại phòng phân biệt có trong CSDL.
     */
    public int getDistinctTypeCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = getTableName(db);

        // Kiểm tra xem cột room_type có tồn tại trong bảng không
        try {
            String query = "SELECT COUNT(DISTINCT " + Constants.COLUMN_ROOM_TYPE + ") FROM " + table;
            try (Cursor cursor = db.rawQuery(query, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int count = cursor.getInt(0);
                    return count > 0 ? count : 1;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Cột room_type không có hoặc lỗi đếm loại phòng: " + e.getMessage());
        }
        return 1;
    }

    /**
     * Lấy danh sách các phòng học cập nhật gần đây nhất.
     */
    public List<Room> getRecentRooms(int limit) {
        List<Room> rooms = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = getTableName(db);

        String orderBy = Constants.COLUMN_ID + " DESC";
        try (Cursor checkCursor = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (checkCursor != null && checkCursor.moveToNext()) {
                int nameIdx = checkCursor.getColumnIndex("name");
                if (nameIdx != -1) {
                    String col = checkCursor.getString(nameIdx);
                    if (Constants.COLUMN_UPDATE_AT.equalsIgnoreCase(col)) {
                        orderBy = Constants.COLUMN_UPDATE_AT + " DESC, " + Constants.COLUMN_ID + " DESC";
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Lỗi kiểm tra PRAGMA table_info: " + e.getMessage());
        }

        String query = "SELECT * FROM " + table + " ORDER BY " + orderBy + " LIMIT ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(limit)})) {
            while (cursor != null && cursor.moveToNext()) {
                rooms.add(cursorToRoom(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi lấy danh sách phòng cập nhật gần đây: " + e.getMessage(), e);
        }
        return rooms;
    }

    private Room cursorToRoom(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COLUMN_ID));
        String roomCode = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_ROOM_CODE));
        int floor = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COLUMN_FLOOR));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_STATUS));

        Room room = new Room(id, roomCode, floor, status);

        int nameIdx = cursor.getColumnIndex(Constants.COLUMN_ROOM_NAME);
        if (nameIdx != -1 && !cursor.isNull(nameIdx)) {
            room.setRoomName(cursor.getString(nameIdx));
        }

        int typeIdx = cursor.getColumnIndex(Constants.COLUMN_ROOM_TYPE);
        if (typeIdx != -1 && !cursor.isNull(typeIdx)) {
            room.setRoomType(cursor.getString(typeIdx));
        }

        int capIdx = cursor.getColumnIndex(Constants.COLUMN_CAPACITY);
        if (capIdx != -1 && !cursor.isNull(capIdx)) {
            room.setCapacity(cursor.getInt(capIdx));
        }

        int eqIdx = cursor.getColumnIndex(Constants.COLUMN_EQUIPMENT);
        if (eqIdx != -1 && !cursor.isNull(eqIdx)) {
            room.setEquipment(cursor.getString(eqIdx));
        }

        int noteIdx = cursor.getColumnIndex(Constants.COLUMN_NOTE);
        if (noteIdx != -1 && !cursor.isNull(noteIdx)) {
            room.setNote(cursor.getString(noteIdx));
        }

        int updateIdx = cursor.getColumnIndex(Constants.COLUMN_UPDATE_AT);
        if (updateIdx != -1 && !cursor.isNull(updateIdx)) {
            room.setUpdateAt(cursor.getLong(updateIdx));
        }

        return room;
    }

    /**
     * Dữ liệu thống kê số phòng theo tầng.
     */
    public static class FloorStat {
        public final int floor;
        public final int roomCount;

        public FloorStat(int floor, int roomCount) {
            this.floor = floor;
            this.roomCount = roomCount;
        }
    }

    /**
     * Dữ liệu thống kê số phòng theo loại phòng.
     */
    public static class TypeStat {
        public final String type;
        public final int roomCount;

        public TypeStat(String type, int roomCount) {
            this.type = type;
            this.roomCount = roomCount;
        }
    }

    /**
     * Lấy thống kê số phòng theo từng tầng (GROUP BY floor).
     */
    public List<FloorStat> getFloorStatistics() {
        List<FloorStat> stats = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = getTableName(db);
        String query = "SELECT " + Constants.COLUMN_FLOOR + ", COUNT(*) as count FROM " + table +
                " GROUP BY " + Constants.COLUMN_FLOOR +
                " ORDER BY " + Constants.COLUMN_FLOOR + " ASC";
        try (Cursor cursor = db.rawQuery(query, null)) {
            while (cursor != null && cursor.moveToNext()) {
                int floor = cursor.getInt(0);
                int count = cursor.getInt(1);
                stats.add(new FloorStat(floor, count));
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi lấy thống kê theo tầng: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Lấy thống kê số phòng theo loại phòng (GROUP BY room_type).
     */
    public List<TypeStat> getTypeStatistics() {
        List<TypeStat> stats = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = getTableName(db);
        try {
            String query = "SELECT " + Constants.COLUMN_ROOM_TYPE + ", COUNT(*) as count FROM " + table +
                    " GROUP BY " + Constants.COLUMN_ROOM_TYPE +
                    " ORDER BY count DESC";
            try (Cursor cursor = db.rawQuery(query, null)) {
                while (cursor != null && cursor.moveToNext()) {
                    String type = cursor.getString(0);
                    if (type == null || type.trim().isEmpty()) {
                        type = "Khác";
                    }
                    int count = cursor.getInt(1);
                    stats.add(new TypeStat(type, count));
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Lỗi lấy thống kê loại phòng: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Lấy thông tin người quản lý từ bảng manager_info.
     */
    public ManagerInfo getManagerInfo() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_MANAGER_INFO + " LIMIT 1", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COLUMN_ID));
                String code = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_STAFF_CODE));
                String dept = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_DEPARTMENT_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_EMAIL));
                String role = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_ROLE));
                return new ManagerInfo(id, code, dept, email, role);
            }
        } catch (Exception e) {
            Log.w(TAG, "Không thể đọc manager_info, dùng dữ liệu mặc định: " + e.getMessage());
        }
        return new ManagerInfo(1, "QL-GD001", "Ban Quản lý Giảng đường", "quanly.giangduong@edu.vn", "Quản lý");
    }
}
