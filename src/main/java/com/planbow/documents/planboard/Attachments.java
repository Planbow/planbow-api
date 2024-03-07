package com.planbow.documents.planboard;

import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@Document(collection = "attachments")
public class Attachments implements BaseDocument {
    private String id;

    private Instant uploadedOn;
    private boolean active;
}
