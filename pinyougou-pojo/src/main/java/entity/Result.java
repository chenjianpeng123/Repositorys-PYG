package entity;

import java.io.Serializable;

/**
 * 返回结果
 */
public class Result implements Serializable {

    private boolean success;//是否成功
    private String massage;//返回的信息

    public Result(boolean success, String massage) {
        super();
        this.success = success;
        this.massage = massage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMassage() {
        return massage;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }
}
