package com.planbow.documents.users;

import lombok.Data;
import com.planbow.util.data.support.entities.mongodb.BaseDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@Document(collection = "users")
public class User implements BaseDocument {

    @Id
    private String id;
    private String name;

    private String email;
    private boolean emailVerified;

    private String password;
    private String gender;

    private String countryCode;
    private String contactNo;
    private boolean contactNoVerified;

    private String deviceId;   // MOBILE DEVICE ID FOR PUSH NOTIFICATIONS
    private String profilePic;

    private String provider;
    private String role;
    private Instant createdOn;
    private Instant modifiedOn;
    private Instant passwordCreatedOn;

    private boolean active;
    private int otp;
    public static final String ROLE_ADMIN="admin";
    public static final String ROLE_USER="user";

    public static final String PROVIDER_GOOGLE="google";
    public static final String PROVIDER_MANUAL="manual";

}
