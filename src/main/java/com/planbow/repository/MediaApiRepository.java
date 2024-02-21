package com.planbow.repository;


import com.planbow.documents.media.Media;
import com.planbow.util.data.support.repository.MongoDbRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class MediaApiRepository extends MongoDbRepository {

    public Media saveOrUpdateMedia(Media media){
        return (Media) saveOrUpdateDocument(media);
    }

    public Media getMediaById(String id){
        return (Media) getDocument(Media.class,id);
    }

    public List<Media> getMedias(String mediaType , String userId){
        Query query  = new Query();
        Criteria criteria  = new Criteria();

        if(!mediaType.equalsIgnoreCase("All"))
             criteria  = criteria.and("mediaType").is(mediaType);

        criteria  = criteria.and("userId").is(userId);
        criteria  = criteria.and("active").is(true);
        query.with(Sort.by(Sort.Direction.DESC,"createdOn"));
        query.addCriteria(criteria);
        return (List<Media>) getDocuments(Media.class,query);

    }

}
