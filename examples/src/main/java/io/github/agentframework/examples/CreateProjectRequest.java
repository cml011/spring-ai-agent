package io.github.agentframework.examples;

import io.github.agentframework.annotation.ToolParam;

public class CreateProjectRequest {

    @ToolParam(description = "项目名称", required = true)
    private String name;

    @ToolParam(description = "大厦/楼宇名称", required = true)
    private String buildingName;

    @ToolParam(description = "年份，如 2026", required = true)
    private Integer year;

    @ToolParam(description = "开始日期，格式 yyyy-MM-dd", required = true)
    private String startDate;

    @ToolParam(description = "结束日期，格式 yyyy-MM-dd", required = false)
    private String endDate;

    @ToolParam(description = "项目类型：full(全维保) / half(半维保)", required = true)
    private String type;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
