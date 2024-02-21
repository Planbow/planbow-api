package com.planbow.repository;

import com.planbow.documents.location.Cities;
import com.planbow.documents.location.States;
import com.planbow.documents.token.RefreshToken;
import com.planbow.documents.users.Password;
import com.planbow.documents.users.User;
import com.planbow.utility.RandomzUtility;
import lombok.extern.log4j.Log4j2;
import com.planbow.util.data.support.repository.MongoDbRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Log4j2
@Repository
@Transactional
@SuppressWarnings({"ALL"})
public class PublicApiRepository extends MongoDbRepository {


    public boolean isEmailExists(String email){
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email));
        return mongoTemplate.exists(query, User.class);
    }

    public User saveOrUpdateUser(User user) {
        log.info("Saving user collection to database ");
        return (User) saveOrUpdateDocument(user);
    }

    public User getUserById(String id) {
        log.info("Executing query to fetch User by id :{}", id);
        return (User) getDocument(User.class, id);
    }


    public User getUser(String email) {
        log.info("Executing query to fetch User by email :{}", email);
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email));
        List<User> users = (List<User>) getDocuments(User.class, query);
        return users.isEmpty() ? null : users.get(0);
    }


    public User saveUser(User user) {
        log.info("Executing query to save User");
        return (User) saveDocument(user);
    }

    public List<User> getUsers() {
        log.info("Executing query to fetch User ");
        return (List<User>) getDocuments(User.class);
    }


    public RefreshToken getRefreshToken(String id) {
        log.info("Executing query to fetch RefreshToken by id :{}",id);
        return (RefreshToken) getDocument(RefreshToken.class, id);
    }

    public RefreshToken getRefreshTokenByToken(String refreshToken) {
        log.info("Executing query to fetch RefreshToken by refreshToken :{}",refreshToken);
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria = criteria.andOperator(Criteria.where("token").is(refreshToken), Criteria.where("active").is(true));
        query.addCriteria(criteria);
        List<RefreshToken> refreshTokens = (List<RefreshToken>) getDocuments(RefreshToken.class, query);
        if (!refreshTokens.isEmpty())
            return refreshTokens.get(0);
        else return null;
    }

    public RefreshToken saveOrUpdateRefreshToken(RefreshToken refreshToken) {
        log.info("Executing query to save RefreshToken");
        return (RefreshToken) saveOrUpdateDocument(refreshToken);
    }


    // TEMPORARY

    public List<Cities> getCities(String stateId) {
        log.info("Executing query to fetch Cities by stateId :{}",stateId);
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria = criteria.and("active").is(true);
        criteria = criteria.and("stateId").is(stateId);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, "name"));
        query.fields().exclude(RandomzUtility.EXCLUDED_COMMON_FIELDS);
        return (List<Cities>) getDocuments(Cities.class, query);
    }

    public States getState(String name) {
        log.info("Executing query to fetch States by name :{}",name);
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria = criteria.and("active").is(true);
        criteria = criteria.andOperator(Criteria.where("name").is(name));
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, "name"));
        query.fields().exclude(RandomzUtility.EXCLUDED_COMMON_FIELDS);
        List<States> states = (List<States>) getDocuments(States.class, query);
        if (!states.isEmpty())
            return states.get(0);
        else
            return null;
    }

    public Password saveOrUpdatePassword(Password password){
        return (Password) saveOrUpdateDocument(password);
    }

    public  Password getPassword(String id){
        return (Password) getDocument(Password.class,id);
    }


}
