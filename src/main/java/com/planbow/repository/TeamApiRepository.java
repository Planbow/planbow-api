package com.planbow.repository;


import com.planbow.documents.teams.Teams;
import com.planbow.util.data.support.repository.MongoDbRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@Log4j2
@Transactional
public class TeamApiRepository extends MongoDbRepository {

    public Teams saveOrUpdateTeam(Teams team){
        return (Teams) saveOrUpdateDocument(team);
    }


    public boolean isTeamExists(String name,String userId){
        Query query  = new Query();
        Criteria criteria  = new Criteria();
        criteria = criteria.andOperator(
                Criteria.where("name").regex("^"+name+"$","i"),
                Criteria.where("active").is(true),
                Criteria.where("createdBy").is(userId));
        query.addCriteria(criteria);
        return mongoTemplate.exists(query, Teams.class);

    }

    public  long teamCount(String userId){
        Query query  = new Query();
        Criteria criteria = new Criteria();

        criteria=criteria.and("active").is(true);
        criteria=criteria.and("createdBy").is(userId);
        query.addCriteria(criteria);
        return mongoTemplate.count(query,Teams.class);
    }

    public List<Teams> getTeams(int index, int itemsPerIndex, String search, String userId){
        Pageable pageable = PageRequest.of(index, itemsPerIndex);
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria=criteria.and("active").is(true);
        criteria=criteria.and("createdBy").is(userId);
        if(!StringUtils.isEmpty(search)){
            criteria=criteria.orOperator(
                    Criteria.where("name").regex(search,"i"));
        }

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "createdOn"));
        query.with(pageable);
        return (List<Teams>) getDocuments(Teams.class,query);
    }
    public Teams getTeam(String teamId){
        return (Teams) getDocument(Teams.class,teamId);
    }

}
