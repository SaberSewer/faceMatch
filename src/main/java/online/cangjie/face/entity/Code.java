package online.cangjie.face.entity;

public enum Code {
    SUCCESS(0), FAIL(1);
    private Integer code;

    Code(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
