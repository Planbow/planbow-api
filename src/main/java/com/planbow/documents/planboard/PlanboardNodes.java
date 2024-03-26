package com.planbow.documents.planboard;

import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;


@Data
@Document(collection = "planboardNodes")
public class PlanboardNodes implements BaseDocument {
    private String id;

    private String title;
    private String description;

    private String planboardId;
    private String parentId;

    private String userId; // createdBy
    private String assignedTo;
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;
    private NodeMetaData metaData;
}

