package com.planbow.documents.media;

import lombok.Data;
import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document(collection = "media")
@Data
public class Media implements BaseDocument {
    @Id
    private String id;
    private String userId;
    private String mediaType;
    private String mediaUrl;
    private String thumbnailUrl;
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;

}
