package com.planbow.repository;


import com.planbow.documents.location.Cities;
import com.planbow.documents.location.Countries;
import com.planbow.documents.location.States;
import com.planbow.util.data.support.repository.MongoDbRepository;
import com.planbow.utility.RandomzUtility;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Log4j2
@Repository
@SuppressWarnings({"All"})
public class LocationApiRepository extends MongoDbRepository {

    public List<Countries> getCountries(String name) {
        log.info("Executing query to get Countries for name :{}",name);
        Query query = new Query();
        Criteria criteria = new Criteria();

        criteria = criteria.and("active").is(true);
        if (!StringUtils.isEmpty(name)) {
            criteria = criteria.orOperator(
                    Criteria.where("name").regex(name, "i"), Criteria.where("iso2").is(name), Criteria.where("iso3").is(name));
        }
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, "name"));

        return (List<Countries>) getDocuments(Countries.class, query);
    }


    public List<States> getStates(String countryId, String name) {
        log.info("Executing query to get States for name :{}",name);
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria = criteria.and("active").is(true);
        criteria = criteria.and("countryId").is(countryId);
        if (!StringUtils.isEmpty(name)) {
            criteria = criteria.orOperator(
                    Criteria.where("name").regex(name, "i"), Criteria.where("iso2").is(name));
        }
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, "name"));
        query.fields().exclude(RandomzUtility.EXCLUDED_COMMON_FIELDS);
        return (List<States>) getDocuments(States.class, query);
    }


    public Cities getCity(String id) {
        log.info("Executing query to get Cities for id :{}",id);
        return (Cities) getDocument(Cities.class, id);
    }

    public States getState(String id) {
        log.info("Executing query to get States for id :{}",id);
        return (States) getDocument(States.class, id);
    }

    public Countries getCountry(String id) {
        log.info("Executing query to get Countries for id :{}",id);
        return (Countries) getDocument(Countries.class, id);
    }

    public List<Cities> getCities(String countryId, String stateId, String name) {
        log.info("Executing query to get Cities for countryId: {} , stateId: {} and name: {}",countryId,stateId,name);
        Query query = new Query();
        Criteria criteria = new Criteria();

        criteria = criteria.and("active").is(true);
        //criteria = criteria.and("countryId").is(countryId);
        criteria = criteria.and("stateId").is(stateId);
        if (!StringUtils.isEmpty(name)) {
            criteria = criteria.orOperator(
                    Criteria.where("name").regex(name, "i"));
        }
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, "name"));
        query.fields().exclude(RandomzUtility.EXCLUDED_COMMON_FIELDS);
        return (List<Cities>) getDocuments(Cities.class, query);
    }
}
