package com.planbow.repository;

import com.planbow.util.data.support.repository.MongoDbRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;


@Repository
@Log4j2
@Transactional
public class WorkspaceApiRepository extends MongoDbRepository {
}
