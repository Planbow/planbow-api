package com.planbow.documents.users;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Locations implements Serializable {
    private double maxLat;
    private double minLat;

    private double maxLng;
    private double minLng;
}
