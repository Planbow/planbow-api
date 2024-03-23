package com.planbow.documents.planboard;

import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@Document(collection = "actionItems")
public class ActionItems implements BaseDocument {
    private String id;

    private String title;
    private String description;

    private String nodeId;
    private String planboardId;
    private String parentId;

    private String status;
    private String priority;
    private Instant endDate;

    private String userId; //createdBy
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;

    public static final String STATUS_IN_TODO="todo";
    public static final String STATUS_IN_PROGRESS="in progress";
    public static final String STATUS_COMPLETED="completed";
    public static final String STATUS_DELAYED="delayed";

    public static final String PRIORITY_LOW="low";
    public static final String PRIORITY_MEDIUM="medium";
    public static final String PRIORITY_HIGH="high";
    public static final String PRIORITY_CRITICAL="critical";
}

