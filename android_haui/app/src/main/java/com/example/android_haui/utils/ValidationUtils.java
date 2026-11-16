package com.example.android_haui.utils;

/**
 * Tiện ích kiểm tra dữ liệu đầu vào khi thêm hoặc cập nhật phòng học.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static boolean isValidRoomCode(String roomCode) {
        if (roomCode == null || roomCode.trim().isEmpty()) {
            return false;
        }
        String trimmed = roomCode.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 20;
    }

    public static boolean isValidRoomName(String roomName) {
        return roomName != null && roomName.trim().length() >= 2;
    }

    public static boolean isValidFloor(int floor) {
        return floor > 0;
    }

    /**
     * Trích xuất số nguyên sức chứa từ chuỗi nhập (hỗ trợ cả "60" hoặc "60 chỗ ngồi").
     * Trả về -1 nếu không hợp lệ.
     */
    public static int parseCapacity(String capacityText) {
        if (capacityText == null || capacityText.trim().isEmpty()) {
            return -1;
        }
        String digitsOnly = capacityText.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            return -1;
        }
        try {
            int value = Integer.parseInt(digitsOnly);
            return (value > 0 && value <= 2000) ? value : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean isValidCapacity(String capacityText) {
        return parseCapacity(capacityText) > 0;
    }
}
