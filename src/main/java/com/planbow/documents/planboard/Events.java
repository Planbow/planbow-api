package com.planbow.documents.planboard;

import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document(collection = "events")
public class Events implements BaseDocument {
    private String id;
}