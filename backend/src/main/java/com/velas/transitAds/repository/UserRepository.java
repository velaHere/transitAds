package com.velas.transitAds.repository;

import com.velas.transitAds.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Repository
public class UserRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserRepository(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }

    public Optional<User> findById(ObjectId userId){
        try{
            User user = mongoTemplate.findById(userId, User.class);
            return Optional.ofNullable(user);
        }catch(Exception e){
            log.error("Error: ", e);
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email){
        try{
            Query query = new Query();
            query.addCriteria(Criteria.where("email").is(email));
            return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
        }catch(Exception e){
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            return Optional.empty();
        }
    }

    public void update(ObjectId userId, String key, Object value) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(userId));

        Update update = new Update().set(key, value);

        mongoTemplate.updateFirst(query, update, User.class);
    }

    public void deleteById(ObjectId id) {
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, User.class);
    }

    public void insert(User user){
        mongoTemplate.insert(user);
    }
}
