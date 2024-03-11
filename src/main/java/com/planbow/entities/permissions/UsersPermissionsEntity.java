package com.planbow.entities.permissions;

import com.planbow.util.data.support.entities.hibernate.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "users_permissions")
public class UsersPermissionsEntity implements BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="userPermissionId")
    private Long id;

    @Column(name="userId")
    private Long userId;

    @Column(name="permissionId")
    private Long permissionId;

    @Column(name="clientId")
    private String clientId;

    @Column(name="createdOn")
    private Timestamp createdOn;

    @Column(name="modifiedOn")
    private Timestamp modifiedOn;

    @Column(name="active")
    private Boolean active;
}
