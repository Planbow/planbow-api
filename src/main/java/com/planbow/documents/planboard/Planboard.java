package com.planbow.documents.planboard;

import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;
@Data
@Document(collection = "planboard")
public class Planboard implements BaseDocument {
    private String id;
    private String domainId;
    private boolean markAsDefaultDomain;
    private String subdomainId;
    private String scope;
    private String geography;

    private Instant endDate;
    private List<Members> members;

    private String name;
    private String description;
    private String workspaceId;
    private String remark;

    private String userId; // createdBy
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;


}
