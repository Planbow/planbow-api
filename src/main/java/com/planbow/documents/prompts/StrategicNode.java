package com.planbow.documents.prompts;

import lombok.Data;

import java.io.Serializable;


@Data
public class StrategicNode implements Serializable {
    private String title;
    private String description;
}
