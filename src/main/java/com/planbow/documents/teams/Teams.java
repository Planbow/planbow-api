package com.planbow.documents.teams;

import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@Document(collection = "teams")
public class Teams implements BaseDocument {

    @Id
    private String id;
    private String name;
    private String description;

    private String createdBy;
    private Instant createdOn;
    private Instant modifiedOn;

    private boolean active;

}
