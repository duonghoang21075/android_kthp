package com.example.android_haui.repository;

/**
 * Interface callback dùng để trả kết quả xử lý bất đồng bộ từ Repository về UI Thread.
 *
 * @param <T> Kiểu dữ liệu của kết quả trả về
 */
public interface RepositoryCallback<T> {

    /**
     * Được gọi trên UI Thread khi tác vụ hoàn thành thành công.
     *
     * @param result Dữ liệu trả về
     */
    void onSuccess(T result);

    /**
     * Được gọi trên UI Thread khi xảy ra lỗi trong quá trình thực thi.
     *
     * @param e Ngoại lệ xảy ra
     */
    void onError(Exception e);
}
