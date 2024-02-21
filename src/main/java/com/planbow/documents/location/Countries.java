package com.planbow.documents.location;

import lombok.Data;
import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "countries")
public class Countries implements BaseDocument {
    @Id
    private String id;

    private String name;
    private String nativeName;

    private String iso2;
    private String iso3;
    private String numericCode;
    private String phoneCode;
    private String capital;
    private String currency;
    private String currencyName;
    private String currencySymbol;
    private String region;
    private String subRegion;
    private double latitude;
    private double longitude;
    private String emoji;
    private Instant createdOn;
    private Instant modifiedOn;
    private boolean active;
}
