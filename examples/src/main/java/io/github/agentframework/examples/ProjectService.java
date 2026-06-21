package io.github.agentframework.examples;

import io.github.agentframework.annotation.AgentTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final Map<Long, CreateProjectRequest> projects = new ConcurrentHashMap<Long, CreateProjectRequest>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @AgentTool(description = "创建维保项目，需要项目名称、大厦名称、年份、开始日期、结束日期、项目类型")
    public String createProject(CreateProjectRequest request) {
        long id = idGenerator.getAndIncrement();
        projects.put(id, request);
        log.info("Project created: id={}, name={}, building={}", id, request.getName(), request.getBuildingName());
        return "项目已创建，编号：P" + id + "，项目名称：" + request.getName()
                + "，大厦：" + request.getBuildingName()
                + "，时间：" + request.getStartDate() + " 至 " + request.getEndDate()
                + "，类型：" + ("full".equals(request.getType()) ? "全维保" : "半维保");
    }

    public int getProjectCount() {
        return projects.size();
    }
}
