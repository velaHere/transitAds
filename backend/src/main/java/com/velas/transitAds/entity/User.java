package com.velas.transitAds.entity;

import com.velas.transitAds.core.domain.AccountType;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    private String email;
    private String password;
    private AccountType accountType;
    private List<String> roles;
    private boolean verified;
}
