package io.github.agentframework.boot;

import io.github.agentframework.tool.Tool;
import io.github.agentframework.tool.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 自动将容器中所有 Tool 接口的 Bean 注册到 ToolRegistry。
 * 包括通过 @AgentTool 生成的 ReflectiveTool 和手动注册的 Tool Bean。
 */
public class ToolBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(ToolBeanPostProcessor.class);

    private final ToolRegistry toolRegistry;

    public ToolBeanPostProcessor(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Tool) {
            Tool tool = (Tool) bean;
            toolRegistry.register(tool);
            log.info("Registered Tool bean: {} — {}", tool.getName(), beanName);
        }
        return bean;
    }
}
