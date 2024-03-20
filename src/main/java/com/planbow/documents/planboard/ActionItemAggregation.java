package com.planbow.documents.planboard;

import lombok.Data;

import java.time.Instant;
import java.util.List;


@Data
public class ActionItemAggregation {
    private String id;

    private String title;
    private String description;
    private String nodeId;
    private String planboardId;
    private String parentId;
    private String userId; //createdBy
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;
    private List<ActionItems> children;
    List<String> childIds;
}
