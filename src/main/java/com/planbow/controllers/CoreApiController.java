package com.planbow.controllers;


import com.planbow.services.CoreApiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseConstants;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/core")
public class CoreApiController {

    private CoreApiService coreApiService;

    @Autowired
    public void setCoreApiService(CoreApiService coreApiService) {
        this.coreApiService = coreApiService;
    }

    @PostMapping("/enquiry")
    public ResponseJsonHandler enquiry(@RequestBody RequestJsonHandler requestJsonHandler){
        String query  = requestJsonHandler.getStringValue("query");
        if (StringUtils.isEmpty(query))
            return ResponseJsonUtil.getResponse("Please provide query", 400, ResponseConstants.BAD_REQUEST.getStatus(), true);
        return coreApiService.enquiry(query.trim());
    }

    @GetMapping("/test")
    public String test(){
        return "Welcome to test";
    }

}
