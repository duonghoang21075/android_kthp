package com.example.android_haui.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.android_haui.database.DatabaseHelper;
import com.example.android_haui.database.RoomDao;
import com.example.android_haui.model.ManagerInfo;
import com.example.android_haui.model.Room;
import com.example.android_haui.utils.Constants;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository đóng vai trò Single Source of Truth giữa CSDL và tầng UI.
 * Mọi thao tác truy xuất dữ liệu từ UI bắt buộc phải gọi qua lớp này.
 * Thực thi bất đồng bộ trên Background Thread và trả kết quả về UI Thread qua Handler.
 */
public class RoomRepository {

    private static volatile RoomRepository instance;

    private final RoomDao roomDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public static RoomRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (RoomRepository.class) {
                if (instance == null) {
                    DatabaseHelper dbHelper = DatabaseHelper.getInstance(context.getApplicationContext());
                    instance = new RoomRepository(new RoomDao(dbHelper));
                }
            }
        }
        return instance;
    }

    // Constructor dùng cho Dependency Injection hoặc testing
    public RoomRepository(RoomDao roomDao) {
        this.roomDao = roomDao;
        this.executorService = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    private <T> void postSuccess(RepositoryCallback<T> callback, T result) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(result));
        }
    }

    private <T> void postError(RepositoryCallback<T> callback, Exception e) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(e));
        }
    }

    /**
     * Lấy danh sách tất cả các phòng học.
     */
    public void getAllRooms(RepositoryCallback<List<Room>> callback) {
        executorService.execute(() -> {
            try {
                List<Room> rooms = roomDao.getAllRooms();
                postSuccess(callback, rooms);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Lấy danh sách phòng theo tầng.
     */
    public void getRoomsByFloor(int floor, RepositoryCallback<List<Room>> callback) {
        executorService.execute(() -> {
            try {
                List<Room> rooms = roomDao.getRoomsByFloor(floor);
                postSuccess(callback, rooms);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Lấy chi tiết một phòng theo mã phòng.
     */
    public void getRoomByCode(String roomCode, RepositoryCallback<Room> callback) {
        executorService.execute(() -> {
            try {
                Room room = roomDao.getRoomByCode(roomCode);
                postSuccess(callback, room);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Thêm mới một phòng học vào CSDL.
     */
    public void insertRoom(Room room, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                long rowId = roomDao.insertRoom(room);
                postSuccess(callback, rowId != -1);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Cập nhật trạng thái phòng học.
     */
    public void updateRoomStatus(String roomCode, String status, RepositoryCallback<Boolean> callback) {
        updateRoomStatus(roomCode, status, null, callback);
    }

    /**
     * Cập nhật trạng thái và ghi chú phòng học.
     */
    public void updateRoomStatus(String roomCode, String status, String note, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                int affectedRows = roomDao.updateRoomStatus(roomCode, status, note);
                postSuccess(callback, affectedRows > 0);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Xóa phòng học theo mã phòng.
     */
    public void deleteRoom(String roomCode, RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                int affectedRows = roomDao.deleteRoom(roomCode);
                postSuccess(callback, affectedRows > 0);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Tìm kiếm phòng học theo từ khóa.
     */
    public void searchRooms(String keyword, RepositoryCallback<List<Room>> callback) {
        executorService.execute(() -> {
            try {
                List<Room> rooms = roomDao.searchRooms(keyword);
                postSuccess(callback, rooms);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Đếm tổng số phòng học.
     */
    public void getTotalRoomCount(RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = roomDao.getTotalRoomCount();
                postSuccess(callback, count);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Đếm số phòng học theo trạng thái.
     */
    public void countRoomsByStatus(String status, RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = roomDao.countRoomsByStatus(status);
                postSuccess(callback, count);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Lấy danh sách phòng học cập nhật gần đây.
     */
    public void getRecentRooms(int limit, RepositoryCallback<List<Room>> callback) {
        executorService.execute(() -> {
            try {
                List<Room> rooms = roomDao.getRecentRooms(limit);
                postSuccess(callback, rooms);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Lớp POJO chứa thông tin thống kê tóm tắt.
     */
    public static class RoomStatistics {
        public final int totalRooms;
        public final int emptyRooms;
        public final int inUseRooms;
        public final int maintenanceRooms;
        public final int floorCount;
        public final int typeCount;

        public RoomStatistics(int totalRooms, int emptyRooms, int inUseRooms, int maintenanceRooms) {
            this(totalRooms, emptyRooms, inUseRooms, maintenanceRooms, 0, 0);
        }

        public RoomStatistics(int totalRooms, int emptyRooms, int inUseRooms, int maintenanceRooms, int floorCount, int typeCount) {
            this.totalRooms = totalRooms;
            this.emptyRooms = emptyRooms;
            this.inUseRooms = inUseRooms;
            this.maintenanceRooms = maintenanceRooms;
            this.floorCount = floorCount;
            this.typeCount = typeCount;
        }
    }

    /**
     * Lấy toàn bộ số liệu thống kê phòng trong một lệnh gọi bất đồng bộ duy nhất.
     */
    public void getRoomStatistics(RepositoryCallback<RoomStatistics> callback) {
        executorService.execute(() -> {
            try {
                int total = roomDao.getTotalRoomCount();
                int empty = roomDao.countRoomsByStatus(Constants.EMPTY);
                int inUse = roomDao.countRoomsByStatus(Constants.IN_USE);
                int maint = roomDao.countRoomsByStatus(Constants.MAINTENANCE);
                int floors = roomDao.getDistinctFloorCount();
                int types = roomDao.getDistinctTypeCount();
                RoomStatistics stats = new RoomStatistics(total, empty, inUse, maint, floors, types);
                postSuccess(callback, stats);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Lấy thống kê số phòng theo từng tầng.
     */
    public void getFloorStatistics(RepositoryCallback<List<RoomDao.FloorStat>> callback) {
        executorService.execute(() -> {
            try {
                List<RoomDao.FloorStat> stats = roomDao.getFloorStatistics();
                postSuccess(callback, stats);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Lấy thống kê số phòng theo từng loại phòng.
     */
    public void getTypeStatistics(RepositoryCallback<List<RoomDao.TypeStat>> callback) {
        executorService.execute(() -> {
            try {
                List<RoomDao.TypeStat> stats = roomDao.getTypeStatistics();
                postSuccess(callback, stats);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    /**
     * Lấy thông tin quản lý từ bảng manager_info.
     */
    public void getManagerInfo(RepositoryCallback<ManagerInfo> callback) {
        executorService.execute(() -> {
            try {
                ManagerInfo info = roomDao.getManagerInfo();
                postSuccess(callback, info);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }
}
