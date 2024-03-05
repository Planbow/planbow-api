package com.planbow.documents.planboard;

import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@Document(collection = "temporaryPlanboard")
public class TemporaryPlanboard implements BaseDocument {
    private String id;
    private String userId; // createdBy
    private String promptId;
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;


}
