package com.aiqaos.core.response;

public class Result<T> {
    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    public Result(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null);
    }

    public static <T> Result<T> failure(ErrorResponse error) {
        return new Result<>(false, null, error);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private boolean success;
        private T data;
        private ErrorResponse error;

        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> error(ErrorResponse error) {
            this.error = error;
            return this;
        }

        public Result<T> build() {
            return new Result<>(success, data, error);
        }
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ErrorResponse getError() { return error; }
}