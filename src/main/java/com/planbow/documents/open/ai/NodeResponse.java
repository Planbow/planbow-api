package com.planbow.documents.open.ai;


import lombok.*;

import java.io.Serializable;

@Data
public class NodeResponse implements Serializable {
    private String title;
    private String description;
}
