package com.planbow.entities.user;

import com.planbow.util.data.support.entities.hibernate.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Set;


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity implements BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="userId")
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name="email")
    private String email;

    @Column(name="contactNo")
    private String contactNo;

    @Column(name="provider")
    private String provider;

    @Column(name="password")
    private String password;

    @Column(name="gender")
    private String gender;

    @Column(name="dateOfBirth")
    private String dateOfBirth;

    @Column(name="address")
    private String address;

    @Column(name="profilePic")
    private String profilePic;

    @Column(name="countryCode")
    private String countryCode;

    @Column(name="latitude")
    private double latitude;

    @Column(name="longitude")
    private double longitude;

    @Column(name="createdOn")
    private Timestamp createdOn;

    @Column(name="modifiedOn")
    private Timestamp modifiedOn;

    @Column(name="isActive")
    private Boolean isActive;

    @Column(name = "isAccountVerified")
    private Boolean  isAccountVerified;

    @Column(name = "attempts")
    private int attempts;
}
