package com.velas.transitAds.repository;

import com.velas.transitAds.entity.Driver;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class DriverRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public DriverRepository(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }

    public Optional<Driver> findById(ObjectId driverId){
        try{
            Driver driver = mongoTemplate.findById(driverId, Driver.class);
            return Optional.ofNullable(driver);
        }catch(Exception e){
            log.error("Error: ", e);
            return Optional.empty();
        }
    }

    public boolean existsByPhone(String phone) {
        Query query = new Query(Criteria.where("phone").is(phone));
        return mongoTemplate.exists(query, Driver.class);
    }

    public boolean existsByVehicleNo(String vehicleNo) {
        Query query = new Query(Criteria.where("vehicleNo").is(vehicleNo));
        return mongoTemplate.exists(query, Driver.class);
    }

    public void update(String driverId, String key, Object value) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(driverId));

        Update update = new Update().set(key, value);

        mongoTemplate.updateFirst(query, update, Driver.class);
    }

    public void insert(Driver driver){
        mongoTemplate.insert(driver);
    }
}
