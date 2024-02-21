package com.planbow.documents.users;


import lombok.Data;
import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "password")
public class Password implements BaseDocument {
    private String id;  //userId
    private String password;
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;
}
