package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.planbow.documents.location.Cities;
import com.planbow.documents.location.Countries;
import com.planbow.documents.location.States;
import com.planbow.repository.LocationApiRepository;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseConstants;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Log4j2
@Transactional
public class LocationApiService {

    private LocationApiRepository locationApiRepository;
    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setLocationApiRepository(LocationApiRepository locationApiRepository) {
        this.locationApiRepository = locationApiRepository;
    }


    public ResponseJsonHandler getCountries(String name) {
        log.info("Executing getCountries() method with name: {}",name);
        List<Countries> countries = locationApiRepository.getCountries(name);
        log.info("Returning response from getCountries() method");
        return ResponseJsonUtil.getResponse(countries, 200, ResponseConstants.SUCCESS.getStatus(), false);
    }

    public ResponseJsonHandler getStates(String countryId, String name) {
        log.info("Executing getStates() method with name: {}",name);
        List<States> states = locationApiRepository.getStates(countryId, name);
        log.info("Returning response from getStates() method");
        return ResponseJsonUtil.getResponse(states, 200, ResponseConstants.SUCCESS.getStatus(), false);
    }

    public ResponseJsonHandler getCities(String countryId, String stateId, String name) {
        log.info("Executing getCities() method with name: {}",name);
        List<Cities> cities = locationApiRepository.getCities(countryId, stateId, name);
        log.info("Returning response from getCities() method");
        return ResponseJsonUtil.getResponse(cities, 200, ResponseConstants.SUCCESS.getStatus(), false);
    }

}
