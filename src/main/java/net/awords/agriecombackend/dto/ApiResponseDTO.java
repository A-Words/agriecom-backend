package net.awords.agriecombackend.dto;

public class ApiResponseDTO<T> {
    private int code;
    private String message;
    private T data;

    public ApiResponseDTO() {}

    public ApiResponseDTO(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponseDTO<T> success(T data) {
        return new ApiResponseDTO<>(200, "Success", data);
    }

    public static <T> ApiResponseDTO<T> error(int code, String message) {
        return new ApiResponseDTO<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
