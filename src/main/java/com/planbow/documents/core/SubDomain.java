package com.planbow.documents.core;

import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@Document(collection = "subdomains")
public class SubDomain implements BaseDocument {
    private String id;
    private String name;
    private String description;
    private String domainId;
    private String userId; // createdBy
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;
}
