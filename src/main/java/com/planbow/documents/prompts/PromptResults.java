package com.planbow.documents.prompts;

import com.planbow.documents.open.ai.NodeResponse;
import com.planbow.documents.open.ai.PromptValidation;
import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;


@Data
@Document(collection = "promptResults")
public class PromptResults implements BaseDocument {
    @Id
    private String id;
    private String domainId;
    private String subdomainId;
    private String scope;
    private String geography;

    private PromptValidation promptValidation;
    private List<NodeResponse> strategicNodes;

    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;

    private String userId;
}
