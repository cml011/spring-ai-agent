package io.github.agentframework.boot;

import io.github.agentframework.annotation.AgentTool;
import io.github.agentframework.tool.Tool;
import io.github.agentframework.tool.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

import java.lang.reflect.Method;

/**
 * 扫描所有 Spring Bean 中标注了 @AgentTool 的方法，
 * 自动生成 ReflectiveTool 并注册到 ToolRegistry。
 */
public class AgentToolBeanPostProcessor implements BeanPostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AgentToolBeanPostProcessor.class);

    private final ToolRegistry toolRegistry;

    public AgentToolBeanPostProcessor(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        for (Method method : clazz.getMethods()) {
            AgentTool annotation = method.getAnnotation(AgentTool.class);
            if (annotation != null) {
                Tool tool = new ReflectiveTool(bean, method);
                toolRegistry.register(tool);
                log.info("Registered @AgentTool: {} — {}", tool.getName(), annotation.description());
            }
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
