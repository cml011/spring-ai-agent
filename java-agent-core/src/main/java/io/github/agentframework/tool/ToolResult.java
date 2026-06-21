package io.github.agentframework.tool;

public class ToolResult {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_ERROR = -1;

    private final int code;
    private final String message;
    private final Object data;

    private ToolResult(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ToolResult success(String message) {
        return new ToolResult(CODE_SUCCESS, message, null);
    }

    public static ToolResult success(String message, Object data) {
        return new ToolResult(CODE_SUCCESS, message, data);
    }

    public static ToolResult error(String message) {
        return new ToolResult(CODE_ERROR, message, null);
    }

    public boolean isSuccess() {
        return code == CODE_SUCCESS;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return isSuccess() ? message : "[错误] " + message;
    }
}
