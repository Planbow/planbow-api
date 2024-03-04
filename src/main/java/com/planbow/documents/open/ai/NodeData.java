package com.planbow.documents.open.ai;



import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
public class NodeData implements Serializable {
   private List<NodeResponse> nodeResponses;
}

