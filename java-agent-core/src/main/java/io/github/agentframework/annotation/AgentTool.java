package io.github.agentframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Service 方法上，自动注册为 Agent 可调用的 Tool。
 * 方法参数可以是简单类型或 DTO，DTO 字段可使用 @ToolParam 补充描述。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentTool {

    /** 工具名称，默认取方法名 */
    String name() default "";

    /** 工具描述：向 LLM 说明这个工具的功能、参数含义 */
    String description();
}
