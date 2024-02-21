package com.planbow.repository;


import com.planbow.documents.users.User;
import com.planbow.documents.users.UserLocation;
import lombok.extern.log4j.Log4j2;
import com.planbow.util.data.support.repository.MongoDbRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Log4j2
@Transactional
@SuppressWarnings({"ALL"})
public class UserApiRepository extends MongoDbRepository {


    public boolean isEmailExists(String email){
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email));
        return mongoTemplate.exists(query, User.class);
    }



    public User saveOrUpdateUser(User user) {
        log.info("Executing query to save User ");
        return (User) saveOrUpdateDocument(user);
    }


    public User getUserById(String id) {
        log.info("Executing query to fetch User by id: {}",id);
        return (User) getDocument(User.class, id);
    }


    public User getUser(String email) {
        log.info("Executing query to fetch User by email: {}",email);
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email));
        List<User> users = (List<User>) getDocuments(User.class, query);
        return users.isEmpty() ? null : users.get(0);
    }

    public List<User> getUsers(List<String> ids){
        Query query  = new Query();
        Criteria criteria  = new Criteria();
        criteria.and("userId").in(ids);
        query.addCriteria(criteria);
        return (List<User>) getDocuments(User.class,query);
    }

    public UserLocation saveOrUpdateLocation(UserLocation userLocation){
        return (UserLocation) saveOrUpdateDocument(userLocation);
    }

    public UserLocation getUserLocation(String id){
        return (UserLocation) getDocument(UserLocation.class,id);
    }

    public List<UserLocation> getUserLocations(String userId,double minLat , double maxLat , double minLng , double maxLng){
        Query query  = new Query();
        Criteria criteria = new Criteria();
        criteria=criteria.and("latitude").gte(minLat).lt(maxLat);
        criteria=criteria.and("longitude").gte(minLng).lt(maxLng);
        criteria=criteria.and("id").ne(userId);
        query.addCriteria(criteria);
        return (List<UserLocation>) getDocuments(UserLocation.class,query);
    }
}
