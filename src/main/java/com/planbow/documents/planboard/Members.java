package com.planbow.documents.planboard;

import lombok.Data;

import java.io.Serializable;


@Data
public class Members implements Serializable {
    private String userId;
    private String emailId;
    private String role;
    private String status;

    public static final String STATUS_PENDING="pending";
    public static final String STATUS_ACCEPTED="accepted";
    public static final String STATUS_REJECTED="rejected";
}
