package com.planbow.documents.open.ai;



import lombok.*;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class NodeData {
   private List<NodeResponse> nodeResponses;
}

