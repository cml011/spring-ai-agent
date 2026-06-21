package io.github.agentframework.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.agentframework.annotation.AgentTool;
import io.github.agentframework.annotation.ToolParam;
import io.github.agentframework.tool.Tool;
import io.github.agentframework.tool.ToolResult;
import io.github.agentframework.tool.ToolSpec;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 通过反射将标注了 @AgentTool 的 Spring Bean 方法包装为 Tool。
 * 自动解析方法参数和 @ToolParam 注解，生成 JSON Schema。
 */
public class ReflectiveTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String name;
    private final String description;
    private final Object bean;
    private final Method method;
    private final ToolSpec spec;

    public ReflectiveTool(Object bean, Method method) {
        AgentTool annotation = method.getAnnotation(AgentTool.class);
        this.name = annotation.name().isEmpty() ? method.getName() : annotation.name();
        this.description = annotation.description();
        this.bean = bean;
        this.method = method;
        this.spec = buildSpec();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ToolSpec getSpec() {
        return spec;
    }

    @Override
    public ToolResult execute(String argsJson) {
        try {
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] args;

            if (paramTypes.length == 0) {
                args = new Object[0];
            } else if (paramTypes.length == 1) {
                // 单个参数：如果是简单类型（String/数字），直接传值
                // 如果是 DTO，解析 JSON 反序列化
                Class<?> paramType = paramTypes[0];
                if (isSimpleType(paramType)) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parsed = MAPPER.readValue(argsJson, Map.class);
                    String value = parsed.containsKey("value")
                            ? (String) parsed.get("value")
                            : parsed.values().iterator().next().toString();
                    args = new Object[]{convertSimpleValue(value, paramType)};
                } else {
                    args = new Object[]{MAPPER.readValue(argsJson, paramType)};
                }
            } else {
                // 多参数场景：把 argsJson 解析为 Map 后按参数名顺序映射
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = MAPPER.readValue(argsJson, Map.class);
                args = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    String paramName = "arg" + i;
                    Object val = parsed.get(paramName);
                    args[i] = val != null ? MAPPER.convertValue(val, paramTypes[i]) : null;
                }
            }

            Object result = method.invoke(bean, args);
            String resultStr = result != null ? MAPPER.writeValueAsString(result) : "操作成功";
            return ToolResult.success(resultStr, result);

        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return ToolResult.error("调用失败：" + cause.getMessage());
        }
    }

    private ToolSpec buildSpec() {
        Class<?>[] paramTypes = method.getParameterTypes();

        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        List<String> required = new ArrayList<String>();

        if (paramTypes.length == 0) {
            // 无参数
        } else if (paramTypes.length == 1 && isSimpleType(paramTypes[0])) {
            Map<String, Object> prop = new LinkedHashMap<String, Object>();
            prop.put("type", "string");
            prop.put("description", "参数值");
            properties.put("value", prop);
            required.add("value");
        } else if (paramTypes.length == 1) {
            // DTO 参数：从字段上的 @ToolParam 注解生成 schema
            Class<?> dtoClass = paramTypes[0];
            for (Field field : dtoClass.getDeclaredFields()) {
                ToolParam tp = field.getAnnotation(ToolParam.class);
                Map<String, Object> prop = new LinkedHashMap<String, Object>();
                prop.put("type", jsonTypeFor(field.getType()));
                if (tp != null && !tp.description().isEmpty()) {
                    prop.put("description", tp.description());
                }
                properties.put(field.getName(), prop);
                if (tp == null || tp.required()) {
                    required.add(field.getName());
                }
            }
        } else {
            // 多个参数：按参数名描述
            for (int i = 0; i < paramTypes.length; i++) {
                Map<String, Object> prop = new LinkedHashMap<String, Object>();
                prop.put("type", "string");
                prop.put("description", "参数 " + i);
                properties.put("arg" + i, prop);
                required.add("arg" + i);
            }
        }

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("type", "object");
        params.put("properties", properties);
        params.put("required", required);

        return new ToolSpec(name, description, params);
    }

    private boolean isSimpleType(Class<?> type) {
        return type == String.class || type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Double.class || type == double.class
                || type == Boolean.class || type == boolean.class;
    }

    private Object convertSimpleValue(String value, Class<?> targetType) {
        if (targetType == String.class) return value;
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(value);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(value);
        return value;
    }

    private String jsonTypeFor(Class<?> type) {
        if (type == String.class) return "string";
        if (type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Double.class || type == double.class) return "number";
        if (type == Boolean.class || type == boolean.class) return "boolean";
        return "string";
    }
}
