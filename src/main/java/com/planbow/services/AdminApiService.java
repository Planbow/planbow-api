package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.core.Domain;
import com.planbow.documents.core.SubDomain;
import com.planbow.repository.AdminApiRepository;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@Transactional
@Log4j2
public class AdminApiService {
    private ObjectMapper objectMapper;
    private AdminApiRepository adminApiRepository;


    @Autowired
    public void setAdminApiRepository(AdminApiRepository adminApiRepository) {
        this.adminApiRepository = adminApiRepository;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<ResponseJsonHandler> addDomain(String name,String description,String userId) {
        if(adminApiRepository.isDomainExists(name)){
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided domain name already exists");
        }

        Domain domain = new Domain();
        domain.setName(name);
        domain.setDescription(description);
        domain.setActive(true);
        domain.setCreatedOn(Instant.now());
        domain.setModifiedOn(Instant.now());
        domain.setUserId(userId);
        domain = adminApiRepository.saveOrUpdateDomain(domain);
        ObjectNode data  =objectMapper.createObjectNode();
        data.put("domainId",domain.getId());
        data.put("name",domain.getName());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

    public ResponseEntity<ResponseJsonHandler> addSubDomain(String domainId, String name,String description, String userId) {
        Domain domain = adminApiRepository.getDomainById(domainId);
        if(domain==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided domainId does not exists");

        if(adminApiRepository.isSubDomainExists(name,domainId)){
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided subdomain name already exists in given domainId");
        }

        SubDomain subDomain  = new SubDomain();
        subDomain.setName(name);
        subDomain.setDescription(description);
        subDomain.setActive(true);
        subDomain.setCreatedOn(Instant.now());
        subDomain.setModifiedOn(Instant.now());
        subDomain.setUserId(userId);
        subDomain.setDomainId(domainId);
        subDomain = adminApiRepository.saveOrUpdateSubDomain(subDomain);
        ObjectNode data  =objectMapper.createObjectNode();
        data.put("domainId",subDomain.getId());
        data.put("name",subDomain.getName());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }
}
