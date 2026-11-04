package com.velas.transitAds.repository;

import com.velas.transitAds.entity.Company;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class CompanyRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public CompanyRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Optional<Company> findById(ObjectId companyId) {
        try {
            Company company = mongoTemplate.findById(companyId, Company.class);
            return Optional.ofNullable(company);
        } catch (Exception e) {
            log.error("Error finding company by id: ", e);
            return Optional.empty();
        }
    }

    public boolean existsByPhone(String phone) {
        Query query = new Query(Criteria.where("phone").is(phone));
        return mongoTemplate.exists(query, Company.class);
    }

    public boolean existsByGstin(String gstin) {
        if (gstin == null || gstin.isBlank()) return false;
        Query query = new Query(Criteria.where("gstin").is(gstin));
        return mongoTemplate.exists(query, Company.class);
    }

    public boolean existsByBusinessRegistrationNumber(String registrationNo) {
        if(registrationNo == null || registrationNo.isBlank()) return false;
        Query query = new Query(Criteria.where("businessRegistrationNumber").is(registrationNo));
        return mongoTemplate.exists(query, Company.class);
    }

    public void insert(Company company) {
        mongoTemplate.insert(company);
    }
}