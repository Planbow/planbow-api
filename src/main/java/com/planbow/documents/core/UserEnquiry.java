package com.planbow.documents.core;

import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@Document(collection = "userEnquiry")
public class UserEnquiry implements BaseDocument {

    @Id
    private String id;
    private String userId;
    private String query;
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;

}
