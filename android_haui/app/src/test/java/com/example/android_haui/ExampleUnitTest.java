package com.example.android_haui;

import com.example.android_haui.database.RoomDao;
import com.example.android_haui.model.ManagerInfo;
import com.example.android_haui.model.Room;
import com.example.android_haui.repository.RoomRepository;
import com.example.android_haui.utils.Constants;
import com.example.android_haui.utils.ValidationUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test kiểm tra các models, constants và thống kê dữ liệu.
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testManagerInfoModel() {
        ManagerInfo info = new ManagerInfo(1, "QL-GD001", "Ban Quản lý Giảng đường", "quanly.giangduong@edu.vn", "Quản lý");
        assertEquals(1, info.getId());
        assertEquals("QL-GD001", info.getStaffCode());
        assertEquals("Ban Quản lý Giảng đường", info.getDepartmentName());
        assertEquals("quanly.giangduong@edu.vn", info.getEmail());
        assertEquals("Quản lý", info.getRole());
    }

    @Test
    public void testRoomStatisticsModel() {
        RoomRepository.RoomStatistics stats = new RoomRepository.RoomStatistics(25, 12, 10, 3, 3, 3);
        assertEquals(25, stats.totalRooms);
        assertEquals(12, stats.emptyRooms);
        assertEquals(10, stats.inUseRooms);
        assertEquals(3, stats.maintenanceRooms);
        assertEquals(3, stats.floorCount);
        assertEquals(3, stats.typeCount);
    }

    @Test
    public void testFloorAndTypeStats() {
        RoomDao.FloorStat floorStat = new RoomDao.FloorStat(1, 10);
        assertEquals(1, floorStat.floor);
        assertEquals(10, floorStat.roomCount);

        RoomDao.TypeStat typeStat = new RoomDao.TypeStat("Lý thuyết", 15);
        assertEquals("Lý thuyết", typeStat.type);
        assertEquals(15, typeStat.roomCount);
    }

    @Test
    public void testRoomModelAndConstants() {
        Room room = new Room(10, "LAB101", 1, Constants.STATUS_EMPTY_VN);
        room.setRoomName("Phòng Thực hành Cơ bản");
        room.setRoomType("Thực hành");
        room.setCapacity(40);
        room.setEquipment("Máy tính, Máy chiếu");
        room.setNote("Hoạt động tốt");

        assertEquals("LAB101", room.getRoomCode());
        assertEquals(1, room.getFloor());
        assertEquals("Trống", room.getStatus());
        assertEquals(40, room.getCapacity());
        assertEquals("Thực hành", room.getRoomType());
    }

    @Test
    public void testCapacityValidation() {
        assertEquals(60, ValidationUtils.parseCapacity("60"));
        assertEquals(60, ValidationUtils.parseCapacity("60 chỗ ngồi"));
        assertEquals(-1, ValidationUtils.parseCapacity("0"));
        assertEquals(-1, ValidationUtils.parseCapacity("abc"));
        assertTrue(ValidationUtils.isValidCapacity("40"));
        assertFalse(ValidationUtils.isValidCapacity(""));
    }
}
