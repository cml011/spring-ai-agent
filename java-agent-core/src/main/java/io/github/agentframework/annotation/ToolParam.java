package io.github.agentframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 DTO 字段上，为 @AgentTool 方法的参数提供描述信息，
 * 这些描述会用于生成 Tool 的 JSON Schema，帮助 LLM 理解各参数含义。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolParam {

    /** 字段描述，例如 "项目名称"、"格式 yyyy-MM-dd" */
    String description() default "";

    /** 是否必填，默认 true */
    boolean required() default true;
}
