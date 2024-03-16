package com.planbow.documents.planboard;

import lombok.Data;
import java.io.Serializable;

@Data
public class NodeMetaData implements Serializable {
    private String color="#2563EB";
    private int height;
    private int width;
    private int x_position;
    private int y_position;
}
