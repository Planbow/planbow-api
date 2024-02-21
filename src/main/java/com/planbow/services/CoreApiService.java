package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.planbow.documents.core.UserEnquiry;
import com.planbow.repository.CoreApiRepository;
import com.planbow.security.services.UserDetailsImpl;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseConstants;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Log4j2
@Transactional
public class CoreApiService {

    private CoreApiRepository coreApiRepository;
    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setCoreApiRepository(CoreApiRepository coreApiRepository) {
        this.coreApiRepository = coreApiRepository;
    }

    public ResponseJsonHandler enquiry(String query) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        UserEnquiry userEnquiry  = new UserEnquiry();
        userEnquiry.setQuery(query);
        userEnquiry.setActive(true);
        userEnquiry.setUserId(userId);
        userEnquiry.setCreatedOn(Instant.now());
        userEnquiry.setModifiedOn(Instant.now());
        userEnquiry = coreApiRepository.saveorUpdateUserEnquiry(userEnquiry);
        return ResponseJsonUtil.getResponse(userEnquiry.getId(),200, ResponseConstants.SUCCESS.getStatus(),false);
    }
}
