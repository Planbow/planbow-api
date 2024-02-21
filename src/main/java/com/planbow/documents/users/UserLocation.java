package com.planbow.documents.users;

import lombok.Data;
import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@Document(collection = "locations")
public class UserLocation implements BaseDocument {

    @Id
    private String id;  // must be userId
    private double latitude;
    private double longitude;

    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;
}
