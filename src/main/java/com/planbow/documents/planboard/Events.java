package com.planbow.documents.planboard;

import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@Document(collection = "events")
public class Events implements BaseDocument {
    @Id
    private String id;
    private String planboardId;

    private String title;
    private String description;

    private String userId;
    private Instant start;
    private Instant end;

    private String createdBy;
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;
}
