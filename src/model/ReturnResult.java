package model;

public class ReturnResult {
    private boolean isSuccess;
    private String message;

    public ReturnResult() {}

    public ReturnResult(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public boolean isSuccess() { return isSuccess; }
    public void setSuccess(boolean isSuccess) { this.isSuccess = isSuccess; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}