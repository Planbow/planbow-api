package com.planbow.entities.idp;

import lombok.Data;

import java.io.Serializable;


@Data
public class Client implements Serializable {
    private String id;
    private String clientId;
    private String clientSecret;

}
