package com.planbow.documents.planboard;

import lombok.Data;

import java.time.Instant;
import java.util.List;


@Data
public class PlanboardNodesAggregation {
    private String id;
    private String title;
    private String description;
    private String planboardId;
    private String parentId;

    private String color="#2563EB";
    private String userId;
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;
    private List<PlanboardNodes> children;
    List<String> childIds;
}
