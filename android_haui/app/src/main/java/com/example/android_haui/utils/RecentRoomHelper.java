package com.example.android_haui.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper quản lý danh sách các mã phòng đã gọi/xem gần đây lưu trong SharedPreferences.
 */
public final class RecentRoomHelper {

    private static final String PREF_NAME = "recent_rooms_prefs";
    private static final String KEY_RECENT_CODES = "recent_room_codes";
    private static final int MAX_RECENT = 5;

    private RecentRoomHelper() {
    }

    /**
     * Thêm mã phòng vào lịch sử gần đây (đưa lên đầu, loại bỏ trùng lặp, tối đa 5 mã).
     */
    public static void addRecentRoom(Context context, String roomCode) {
        if (context == null || roomCode == null || roomCode.trim().isEmpty()) return;

        String code = roomCode.trim().toUpperCase(Locale.getDefault());
        List<String> list = getRecentRooms(context);

        list.remove(code);
        list.add(0, code);

        while (list.size() > MAX_RECENT) {
            list.remove(list.size() - 1);
        }

        JSONArray array = new JSONArray();
        for (String c : list) {
            array.put(c);
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_RECENT_CODES, array.toString()).apply();
    }

    /**
     * Lấy danh sách mã phòng đã gọi/xem gần đây.
     */
    public static List<String> getRecentRooms(Context context) {
        List<String> list = new ArrayList<>();
        if (context == null) return list;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_RECENT_CODES, null);
        if (raw == null || raw.trim().isEmpty()) {
            return list;
        }

        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                String c = array.optString(i, null);
                if (c != null && !c.trim().isEmpty() && !list.contains(c)) {
                    list.add(c.trim());
                }
            }
        } catch (JSONException ignored) {
        }
        return list;
    }

    /**
     * Xóa toàn bộ lịch sử mã phòng gần đây.
     */
    public static void clearRecentRooms(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_RECENT_CODES).apply();
    }
}
