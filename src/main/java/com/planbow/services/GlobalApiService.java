package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.global.Organization;
import com.planbow.repository.GlobalApiRepository;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Log4j2
public class GlobalApiService {

    private GlobalApiRepository globalApiRepository;
    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setGlobalApiRepository(GlobalApiRepository globalApiRepository) {
        this.globalApiRepository = globalApiRepository;
    }


    public ResponseEntity<ResponseJsonHandler> getOrganizations(String userId) {
        List<Organization> organizations = globalApiRepository.getOrganizations(userId);
        if(organizations.isEmpty()){
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"No organization found for this account");
        }
        ArrayNode data  = objectMapper.createArrayNode();
        organizations.forEach(e->{
            ObjectNode node  =objectMapper.createObjectNode();
            node.put("organizationId",e.getId());
            node.put("organizationName",e.getName());
            data.add(node);
        });
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }


    public ResponseEntity<ResponseJsonHandler> createOrganization(String organizationName,String userId) {
        if(globalApiRepository.isOrganizationExists(organizationName)){
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided organization name already exists");
        }

        Organization organization  = new Organization();
        organization.setName(organizationName);
        organization.setCreatedBy(userId);
        organization.setCreatedOn(Instant.now());
        organization.setModifiedOn(Instant.now());
        organization.setActive(true);
        organization = globalApiRepository.saveOrUpdateOrganization(organization);

        ObjectNode data  =objectMapper.createObjectNode();
        data.put("organizationId",organization.getId());
        data.put("organizationName",organization.getName());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);

    }
}
