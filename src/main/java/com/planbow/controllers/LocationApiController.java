package com.planbow.controllers;

import com.planbow.services.LocationApiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseConstants;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping("/location")
@SuppressWarnings({"All"})
public class LocationApiController {

    private LocationApiService locationApiService;

    @Autowired
    public void setLocationApiService(LocationApiService locationApiService) {
        this.locationApiService = locationApiService;
    }

    @PostMapping("/get-countries")
    public ResponseJsonHandler getCountries(@RequestBody RequestJsonHandler requestJsonHandler) {
        log.info("Executing /get-countries endpoint with payload: {}",requestJsonHandler);
        String name = requestJsonHandler.getStringValue("name");
        return locationApiService.getCountries(name);
    }

    @PostMapping("/get-states")
    public ResponseJsonHandler getStates(@RequestBody RequestJsonHandler requestJsonHandler) {
        log.info("Executing /get-states endpoint with payload: {}",requestJsonHandler);
        String countryId = requestJsonHandler.getStringValue("countryId");
        if (StringUtils.isEmpty(countryId))
            return ResponseJsonUtil.getResponse("Please provide countryId", 400, ResponseConstants.BAD_REQUEST.getStatus(), true);
        String name = requestJsonHandler.getStringValue("name");
        return locationApiService.getStates(countryId, name);
    }

    @PostMapping("/get-cities")
    public ResponseJsonHandler getCities(@RequestBody RequestJsonHandler requestJsonHandler) {
        log.info("Executing /get-cities endpoint with payload: {}",requestJsonHandler);
        String countryId = requestJsonHandler.getStringValue("countryId");
        if (StringUtils.isEmpty(countryId))
            return ResponseJsonUtil.getResponse("Please provide countryId", 400, ResponseConstants.BAD_REQUEST.getStatus(), true);

        String stateId = requestJsonHandler.getStringValue("stateId");
        if (StringUtils.isEmpty(stateId))
            return ResponseJsonUtil.getResponse("Please provide stateId", 400, ResponseConstants.BAD_REQUEST.getStatus(), true);

        String name = requestJsonHandler.getStringValue("name");
        return locationApiService.getCities(countryId, stateId, name);
    }

}
