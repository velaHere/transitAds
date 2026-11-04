package com.velas.transitAds.entity;

import com.velas.transitAds.core.domain.BusinessType;
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
@Document(collection = "companies")
public class Company {
    @Id
    private ObjectId id;

    private String companyName;

    @Indexed(unique = true)
    private String phone;

    private BusinessType businessType;

    @Indexed(unique = true, sparse = true)
    private String businessRegistrationNumber;

    @Indexed(unique = true, sparse = true)
    private String gstin;

    private String address;
    private long balance;
}
