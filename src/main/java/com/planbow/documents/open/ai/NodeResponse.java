package com.planbow.documents.open.ai;


import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
public class NodeResponse {
    private String title;
    private String description;
}
