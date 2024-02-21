package com.planbow.controllers;


import com.planbow.services.TeamApiService;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/teams")
@Log4j2
@RestController
public class TeamApiController {

    private TeamApiService teamApiService;

    @Autowired
    public void setTeamApiService(TeamApiService teamApiService) {
        this.teamApiService = teamApiService;
    }


    @PostMapping("/create-team")
    public ResponseEntity<ResponseJsonHandler> createTeam(@RequestBody RequestJsonHandler requestJsonHandler) {
        log.info("Executing endpoint /create-team for payload : {}",requestJsonHandler);
        String name = requestJsonHandler.getStringValue("name");
        if (StringUtils.isEmpty(name))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide name");

        String description = requestJsonHandler.getStringValue("description");
        return teamApiService.createTeam(name.trim(),description);
    }

    @PostMapping("/get-teams")
    public ResponseEntity<ResponseJsonHandler> getTeams(@RequestBody RequestJsonHandler requestJsonHandler){
        Integer index  = requestJsonHandler.getIntegerValue("index");
        if(index==null)
            return ResponseJsonUtil.getResponse(
                    HttpStatus.BAD_REQUEST,"Please provide index");

        Integer itemsPerIndex  = requestJsonHandler.getIntegerValue("itemsPerIndex");
        if(itemsPerIndex==null)
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,
                    "Please provide itemsPerIndex");
        return teamApiService.getTeams(index,itemsPerIndex,requestJsonHandler);
    }

    @PostMapping("/get-team")
    public ResponseEntity<ResponseJsonHandler> getTeam(@RequestBody RequestJsonHandler requestJsonHandler){
        String teamId = requestJsonHandler.getStringValue("teamId");
        if (StringUtils.isEmpty(teamId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide teamId");
        return teamApiService.getTeam(teamId);
    }

    @PostMapping("/update-team")
    public ResponseEntity<ResponseJsonHandler> updateTeam(@RequestBody RequestJsonHandler requestJsonHandler){
        String teamId = requestJsonHandler.getStringValue("teamId");
        if (StringUtils.isEmpty(teamId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide teamId");
        String name = requestJsonHandler.getStringValue("name");
        String description = requestJsonHandler.getStringValue("description");
        return teamApiService.updateTeam(teamId.trim(),name,description);
    }

    @PostMapping("/deactivate-team")
    public ResponseEntity<ResponseJsonHandler> deactivateTeam(@RequestBody RequestJsonHandler requestJsonHandler){
        String teamId = requestJsonHandler.getStringValue("teamId");
        if (StringUtils.isEmpty(teamId))
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Please provide teamId");
        return teamApiService.deactivateTeam(teamId.trim());
    }

}
