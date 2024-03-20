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
    private String userId; //createdBy
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;
}

