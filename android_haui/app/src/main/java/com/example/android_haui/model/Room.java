package com.example.android_haui.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity biểu diễn thông tin một phòng học trong CSDL.
 * Implement Serializable để truyền nhận an toàn giữa các Activity/Fragment.
 */
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String roomCode;
    private String roomName;
    private int floor;
    private String roomType;
    private int capacity;
    private String equipment;
    private String status;
    private String note;
    private long updateAt;

    public Room() {
    }

    public Room(String roomCode, int floor, String status) {
        this.roomCode = roomCode;
        this.floor = floor;
        this.status = status;
    }

    public Room(int id, String roomCode, int floor, String status) {
        this.id = id;
        this.roomCode = roomCode;
        this.floor = floor;
        this.status = status;
    }

    public Room(int id, String roomCode, String roomName, int floor, String roomType,
                int capacity, String equipment, String status, String note, long updateAt) {
        this.id = id;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.floor = floor;
        this.roomType = roomType;
        this.capacity = capacity;
        this.equipment = equipment;
        this.status = status;
        this.note = note;
        this.updateAt = updateAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(long updateAt) {
        this.updateAt = updateAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return id == room.id && floor == room.floor &&
                Objects.equals(roomCode, room.roomCode) &&
                Objects.equals(status, room.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roomCode, floor, status);
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomCode='" + roomCode + '\'' +
                ", roomName='" + roomName + '\'' +
                ", floor=" + floor +
                ", roomType='" + roomType + '\'' +
                ", capacity=" + capacity +
                ", status='" + status + '\'' +
                '}';
    }
}
