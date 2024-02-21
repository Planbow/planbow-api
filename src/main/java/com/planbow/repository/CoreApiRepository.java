package com.planbow.repository;


import com.planbow.documents.core.UserEnquiry;
import com.planbow.util.data.support.repository.MongoDbRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
@Transactional
@Log4j2
@SuppressWarnings({"ALL"})
public class CoreApiRepository extends MongoDbRepository {


    public UserEnquiry saveorUpdateUserEnquiry(UserEnquiry userEnquiry){
        return (UserEnquiry) saveOrUpdateDocument(userEnquiry);
    }

}
