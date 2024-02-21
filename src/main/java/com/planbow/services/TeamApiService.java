package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.teams.Teams;
import com.planbow.repository.TeamApiRepository;
import com.planbow.security.services.UserDetailsImpl;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@Log4j2
public class TeamApiService {

    private TeamApiRepository teamApiRepository;
    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setTeamApiRepository(TeamApiRepository teamApiRepository) {
        this.teamApiRepository = teamApiRepository;
    }

    public ResponseEntity<ResponseJsonHandler> createTeam(String name, String description) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        if(teamApiRepository.isTeamExists(name,userId))
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided team name already exists");

        Teams teams  = new Teams();
        teams.setName(name);
        teams.setDescription(description);
        teams.setCreatedBy(userId);
        teams.setCreatedOn(Instant.now());
        teams.setModifiedOn(Instant.now());
        teams.setActive(true);
        teams  = teamApiRepository.saveOrUpdateTeam(teams);

        ObjectNode node=objectMapper.createObjectNode();
        node.put("id",teams.getId());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,node);

    }

    public ResponseEntity<ResponseJsonHandler> getTeams(int index, int itemsPerIndex, RequestJsonHandler requestJsonHandler){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        ObjectNode dataNode  = objectMapper.createObjectNode();
        long count  = teamApiRepository.teamCount(userId);
        String search = requestJsonHandler.getStringValue("search");
        dataNode.put("draw",(index+1));
        dataNode.put("recordsTotal",count);
        List<Teams> teams = teamApiRepository.getTeams(index,itemsPerIndex,search,userId);
        dataNode.set("teams",objectMapper.valueToTree(teams));
        if(search!=null && !StringUtils.isEmpty(search))
            dataNode.put("recordsFiltered",teams.size());
        else
            dataNode.put("recordsFiltered",count);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,dataNode);
    }

    public ResponseEntity<ResponseJsonHandler> getTeam(String teamId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        Teams teams  = teamApiRepository.getTeam(teamId);
        if(teams==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided teamId does not exists");
        if(!teams.getCreatedBy().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"Provided teamId does not belongs to you");
        if(!teams.isActive())
            return ResponseJsonUtil.getResponse(HttpStatus.FORBIDDEN,"Provided teamId is previously inactivated");
        return ResponseJsonUtil.getResponse(HttpStatus.OK,teams);
    }

    public ResponseEntity<ResponseJsonHandler> updateTeam(String teamId, String name, String description) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        Teams teams  = teamApiRepository.getTeam(teamId);
        if(teams==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided teamId does not exists");
        if(!teams.getCreatedBy().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"Provided teamId does not belongs to you");
        if(!teams.isActive())
            return ResponseJsonUtil.getResponse(HttpStatus.FORBIDDEN,"Provided teamId is previously inactivated");
        if(!StringUtils.isEmpty(name)){
            if(!teams.getName().equalsIgnoreCase(name)){
                if(teamApiRepository.isTeamExists(name,userId))
                    return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided team name already exists");
            }
            teams.setName(name);
        }

        if(!StringUtils.isEmpty(description))
            teams.setDescription(description);

        teams.setModifiedOn(Instant.now());
        teams = teamApiRepository.saveOrUpdateTeam(teams);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,teams);
    }

    public ResponseEntity<ResponseJsonHandler> deactivateTeam(String teamId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        Teams teams  = teamApiRepository.getTeam(teamId);
        if(teams==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided teamId does not exists");
        if(!teams.getCreatedBy().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"Provided teamId does not belongs to you");
        teams.setActive(false);
        teamApiRepository.saveOrUpdateTeam(teams);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,"Team successfully deactivated");
    }
}
