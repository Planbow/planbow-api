package com.planbow.documents.token;


import lombok.Data;
import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(value = "refreshToken")
public class RefreshToken implements BaseDocument {
    @Id
    private String id;
    private String token;
    private Instant createdOn;
    private Instant expiredOn;
    private boolean active;
}
