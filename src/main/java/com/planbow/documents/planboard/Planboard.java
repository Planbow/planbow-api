package com.planbow.documents.planboard;

import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@Document(collection = "planboard")
public class Planboard implements BaseDocument {
    private String id;
    private String domainId;
    private boolean markAsDefaultDomain;
    private String subdomainId;
    private String scope;
    private String geography;




    private String workspaceId;
    private String userId; // createdBy
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;


}
