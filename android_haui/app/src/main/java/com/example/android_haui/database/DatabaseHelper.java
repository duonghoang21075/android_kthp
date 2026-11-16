package com.example.android_haui.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android_haui.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Lớp quản lý kết nối SQLite theo mô hình Singleton.
 * Đảm bảo chỉ 1 instance duy nhất tồn tại và sao chép chuẩn xác CSDL đóng gói sẵn từ assets.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String PREFS_NAME = "haui_db_prefs";
    private static final String KEY_ASSET_DB_VERSION = "asset_db_version";

    @android.annotation.SuppressLint("StaticFieldLeak")
    private static volatile DatabaseHelper instance;
    private final Context appContext;

    /**
     * Lấy instance duy nhất của DatabaseHelper (Thread-safe Double-Checked Locking).
     */
    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        this.appContext = context.getApplicationContext();
        copyDatabaseFromAssetsIfNeeded();
    }

    /**
     * Kiểm tra và sao chép tệp .db từ assets sang thư mục databases nội bộ của ứng dụng.
     * Đồng bộ PRAGMA user_version = DATABASE_VERSION ngay sau khi copy để ngăn SQLiteOpenHelper
     * gọi nhầm onCreate() hoặc onUpgrade() gây chèn dữ liệu thừa.
     */
    public synchronized void copyDatabaseFromAssetsIfNeeded() {
        File dbFile = appContext.getDatabasePath(Constants.DATABASE_NAME);
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int installedAssetVersion = prefs.getInt(KEY_ASSET_DB_VERSION, 0);

        if (dbFile.exists() && installedAssetVersion == Constants.DATABASE_VERSION) {
            Log.d(TAG, "Database từ assets đã được đồng bộ ở phiên bản " + installedAssetVersion);
            return;
        }

        File dbDir = dbFile.getParentFile();
        if (dbDir != null && !dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            Log.d(TAG, "Tạo thư mục databases: " + created);
        }

        // Xóa tệp DB cũ (nếu có dữ liệu thừa từ phiên bản trước) trước khi chép mới từ assets
        if (dbFile.exists()) {
            SQLiteDatabase.deleteDatabase(dbFile);
        }

        try (InputStream inputStream = appContext.getAssets().open(Constants.DATABASE_NAME);
             OutputStream outputStream = new FileOutputStream(dbFile)) {

            byte[] buffer = new byte[1024 * 4];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();

            // Đặt user_version của DB vừa copy khớp với DATABASE_VERSION để SQLiteOpenHelper không gọi onCreate()
            try (SQLiteDatabase copiedDb = SQLiteDatabase.openDatabase(
                    dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE)) {
                copiedDb.setVersion(Constants.DATABASE_VERSION);
                ensureCompatibility(copiedDb);
            }

            prefs.edit().putInt(KEY_ASSET_DB_VERSION, Constants.DATABASE_VERSION).apply();
            Log.d(TAG, "Sao chép database chuẩn từ assets thành công: " + dbFile.getAbsolutePath());
        } catch (IOException e) {
            Log.w(TAG, "Không thể sao chép DB từ assets, sẽ khởi tạo qua onCreate(): " + e.getMessage());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Fallback tạo bảng nếu không tìm thấy file trong assets
        String sql = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_ROOMS + " (" +
                Constants.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Constants.COLUMN_ROOM_CODE + " TEXT NOT NULL UNIQUE, " +
                Constants.COLUMN_ROOM_NAME + " TEXT NOT NULL, " +
                Constants.COLUMN_FLOOR + " INTEGER NOT NULL, " +
                Constants.COLUMN_ROOM_TYPE + " TEXT NOT NULL, " +
                Constants.COLUMN_CAPACITY + " INTEGER NOT NULL DEFAULT 0, " +
                Constants.COLUMN_EQUIPMENT + " TEXT, " +
                Constants.COLUMN_STATUS + " TEXT NOT NULL, " +
                Constants.COLUMN_NOTE + " TEXT, " +
                Constants.COLUMN_UPDATE_AT + " INTEGER NOT NULL)";
        db.execSQL(sql);

        ensureCompatibility(db);

        // Chỉ chèn dữ liệu mặc định nếu bảng hoàn toàn trống (tránh chèn đè dữ liệu thừa lên DB từ assets)
        if (isRoomTableEmpty(db)) {
            insertDefaultRooms(db);
        }
    }

    private boolean isRoomTableEmpty(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + Constants.TABLE_ROOMS, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) == 0;
            }
        } catch (Exception e) {
            Log.w(TAG, "Không thể kiểm tra số dòng bảng rooms: " + e.getMessage());
        }
        return true;
    }

    private void insertDefaultRooms(SQLiteDatabase db) {
        long now = System.currentTimeMillis() / 1000;
        String[] types = {"Lý thuyết", "Thực hành", "Hội trường"};
        String[] statuses = {"Trống", "Đang sử dụng", "Bảo trì"};
        int[] capacities = {60, 40, 200};

        for (int floor = 1; floor <= 3; floor++) {
            for (int i = 1; i <= 5; i++) {
                String code = floor + "0" + i;
                int typeIdx = (i - 1) % 3;
                String roomCode = (typeIdx == 1 ? "LAB" : (typeIdx == 2 ? "HT" : "P")) + code;
                String typeName = types[typeIdx];
                String status = statuses[(floor + i) % 3];
                int cap = capacities[typeIdx];

                ContentValues values = new ContentValues();
                values.put(Constants.COLUMN_ROOM_CODE, roomCode);
                values.put(Constants.COLUMN_ROOM_NAME, "Phòng " + typeName + " " + code);
                values.put(Constants.COLUMN_FLOOR, floor);
                values.put(Constants.COLUMN_ROOM_TYPE, typeName);
                values.put(Constants.COLUMN_CAPACITY, cap);
                values.put(Constants.COLUMN_EQUIPMENT, "Máy chiếu, Điều hòa, Bàn ghế");
                values.put(Constants.COLUMN_STATUS, status);
                values.put(Constants.COLUMN_NOTE, "Hoạt động bình thường");
                values.put(Constants.COLUMN_UPDATE_AT, now);
                db.insertWithOnConflict(Constants.TABLE_ROOMS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        ensureCompatibility(db);
    }

    private void ensureCompatibility(SQLiteDatabase db) {
        try {
            boolean hasRoomsTable = false;
            boolean hasRoomTable = false;

            try (Cursor c = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name IN ('rooms', 'Room')", null)) {
                while (c != null && c.moveToNext()) {
                    String name = c.getString(0);
                    if ("rooms".equalsIgnoreCase(name)) hasRoomsTable = true;
                    if ("Room".equals(name)) hasRoomTable = true;
                }
            }

            if (hasRoomsTable && !hasRoomTable) {
                db.execSQL("CREATE VIEW IF NOT EXISTS Room AS SELECT * FROM rooms");
            } else if (hasRoomTable && !hasRoomsTable) {
                db.execSQL("CREATE VIEW IF NOT EXISTS rooms AS SELECT * FROM Room");
            }

            // Đảm bảo bảng manager_info luôn tồn tại và có dữ liệu
            db.execSQL("CREATE TABLE IF NOT EXISTS " + Constants.TABLE_MANAGER_INFO + " (" +
                    Constants.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.COLUMN_STAFF_CODE + " TEXT NOT NULL, " +
                    Constants.COLUMN_DEPARTMENT_NAME + " TEXT NOT NULL, " +
                    Constants.COLUMN_EMAIL + " TEXT NOT NULL, " +
                    Constants.COLUMN_ROLE + " TEXT NOT NULL)");

            try (Cursor mgrCursor = db.rawQuery("SELECT COUNT(*) FROM " + Constants.TABLE_MANAGER_INFO, null)) {
                if (mgrCursor != null && mgrCursor.moveToFirst() && mgrCursor.getInt(0) == 0) {
                    ContentValues mgrValues = new ContentValues();
                    mgrValues.put(Constants.COLUMN_STAFF_CODE, "QL-GD001");
                    mgrValues.put(Constants.COLUMN_DEPARTMENT_NAME, "Ban Quản lý Giảng đường");
                    mgrValues.put(Constants.COLUMN_EMAIL, "quanly.giangduong@edu.vn");
                    mgrValues.put(Constants.COLUMN_ROLE, "Quản lý");
                    db.insertWithOnConflict(Constants.TABLE_MANAGER_INFO, null, mgrValues, SQLiteDatabase.CONFLICT_IGNORE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đảm bảo tính tương thích bảng: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        ensureCompatibility(db);
    }
}
