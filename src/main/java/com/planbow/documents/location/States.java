package com.planbow.documents.location;


import lombok.Data;
import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "states")
public class States implements BaseDocument {
    @Id
    private String id;
    private String name;
    private String countryId;
    private String iso2;
    private String fipsCode;
    private double latitude;
    private double longitude;
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;
}
