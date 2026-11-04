package com.velas.transitAds.entity;

import com.velas.transitAds.core.domain.Vehicle;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "drivers")
public class Driver {
    @Id
    private ObjectId id;
    private String name;
    @Indexed(unique = true)
    private String phone;
    private long balance;
    private Vehicle vehicle;
    @Indexed(unique = true)
    private String vehicleNo;
    private String drivingLicenseUrl;
    private String rcUrl;
}
