package online.cangjie.face.entity;

public class Result<T> {
    private Integer code;
    private T data;
    private String msg;

    public Result(String msg, T data, Integer code) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static Result success(String msg) {
        return new Result(msg, null, Code.SUCCESS.getCode());
    }

    public static <T> Result successWith(T data, String msg) {
        return new Result(msg, data, Code.SUCCESS.getCode());
    }

    public static Result fail(String msg) {
        return new Result(msg, null, Code.FAIL.getCode());
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
