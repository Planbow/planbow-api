package com.planbow.documents.planboard;

import lombok.Data;

import java.io.Serializable;


@Data
public class MetaData implements Serializable {
    private long size;
    private String extension;
    private String path;
    private String fileName;
}
