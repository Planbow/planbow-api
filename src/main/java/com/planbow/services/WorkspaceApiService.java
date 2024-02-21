package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.planbow.repository.WorkspaceApiRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@Transactional
public class WorkspaceApiService {
    private WorkspaceApiRepository workspaceApiRepository;

    @Autowired
    public void setWorkspaceApiRepository(WorkspaceApiRepository workspaceApiRepository) {
        this.workspaceApiRepository = workspaceApiRepository;
    }

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
