package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.core.Domain;
import com.planbow.documents.core.SubDomain;
import com.planbow.documents.open.ai.NodeData;
import com.planbow.documents.open.ai.NodeResponse;
import com.planbow.documents.open.ai.PromptValidation;
import com.planbow.documents.planboard.*;
import com.planbow.documents.prompts.PromptResults;
import com.planbow.repository.AdminApiRepository;
import com.planbow.repository.PlanboardApiRepository;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import com.planbow.utility.FileProcessor;
import com.planbow.utility.PlanbowUtility;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.planbow.utility.PlanbowUtility.*;

@Service
@Log4j2
public class PlanboardApiService {

    private PlanboardApiRepository planboardApiRepository;
    private AdminApiRepository adminApiRepository;
    private OpenAiChatClient chatClient;
    private ObjectMapper objectMapper;
    private FileStorageServices fileStorageServices;


    @Autowired
    public void setFileStorageServices(FileStorageServices fileStorageServices) {
        this.fileStorageServices = fileStorageServices;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setAdminApiRepository(AdminApiRepository adminApiRepository) {
        this.adminApiRepository = adminApiRepository;
    }

    @Autowired
    public void setChatClient(OpenAiChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Autowired
    public void setPlanboardApiRepository(PlanboardApiRepository planboardApiRepository) {
        this.planboardApiRepository = planboardApiRepository;
    }


    public ResponseEntity<ResponseJsonHandler> validatePrompt(String domainId, String subdomainId, String scope, String geography,String userId){
        Domain domain = adminApiRepository.getDomainById(domainId);
        if(domain==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided domainId does not exists");

        SubDomain subDomain = adminApiRepository.getSubdomainById(subdomainId);
        if(subDomain==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided subdomainId does not exists");

        ObjectNode node  = objectMapper.createObjectNode();
        PromptResults promptResults  = planboardApiRepository.getPromptResult(domainId,subdomainId,scope,geography);
        if(promptResults!=null){
            if(promptResults.getPromptValidation()!=null){
                if(promptResults.getPromptValidation().getStatus().equalsIgnoreCase("negative")){
                    PromptValidation promptValidation=openAiPromptValidation(domain.getName(), subDomain.getName(),scope,geography);
                    promptResults = preparePromptResult(planboardApiRepository,domain.getId(),subDomain.getId(),scope,geography,userId,promptResults,promptValidation);
                    TemporaryPlanboard temporaryPlanboard = prepareTemporaryPlanboard(planboardApiRepository,promptResults.getId(),userId);
                    node.put("status",promptValidation.getStatus());
                    node.put("reason",promptValidation.getReason());
                    node.put("planboardId",temporaryPlanboard.getId());
                    if(promptValidation.getStatus().equalsIgnoreCase("positive")){
                        PromptResults finalPromptResults = promptResults;
                        new Thread(()->openAiStrategicNodes(domain,subDomain,scope,geography,userId, finalPromptResults) ).start();
                    }
                }
                else {
                    if(CollectionUtils.isEmpty(promptResults.getStrategicNodes())){
                        TemporaryPlanboard temporaryPlanboard = prepareTemporaryPlanboard(planboardApiRepository,promptResults.getId(),userId);
                        node.put("status",promptResults.getPromptValidation().getStatus());
                        node.put("reason",promptResults.getPromptValidation().getReason());
                        node.put("planboardId",temporaryPlanboard.getId());
                        PromptResults finalPromptResults = promptResults;
                        new Thread(()->openAiStrategicNodes(domain,subDomain,scope,geography,userId, finalPromptResults) ).start();
                    }else{
                        TemporaryPlanboard temporaryPlanboard = prepareTemporaryPlanboard(planboardApiRepository,promptResults.getId(),userId);
                        node.put("status",promptResults.getPromptValidation().getStatus());
                        node.put("reason",promptResults.getPromptValidation().getReason());
                        node.put("planboardId",temporaryPlanboard.getId());
                    }
                }
            }
            else{
                PromptValidation promptValidation=openAiPromptValidation(domain.getName(), subDomain.getName(),scope,geography);
                promptResults = preparePromptResult(planboardApiRepository,domain.getId(),subDomain.getId(),scope,geography,userId,promptResults,promptValidation);
                TemporaryPlanboard temporaryPlanboard = prepareTemporaryPlanboard(planboardApiRepository,promptResults.getId(),userId);
                node.put("status",promptValidation.getStatus());
                node.put("reason",promptValidation.getReason());
                node.put("planboardId",temporaryPlanboard.getId());

                if(promptValidation.getStatus().equalsIgnoreCase("positive")){
                    PromptResults finalPromptResults = promptResults;
                    new Thread(()->openAiStrategicNodes(domain,subDomain,scope,geography,userId, finalPromptResults) ).start();
                }
            }
        }
        else{
            PromptValidation promptValidation=openAiPromptValidation(domain.getName(), subDomain.getName(),scope,geography);
            promptResults = preparePromptResult(planboardApiRepository,domain.getId(),subDomain.getId(),scope,geography,userId,null,promptValidation);
            TemporaryPlanboard temporaryPlanboard = prepareTemporaryPlanboard(planboardApiRepository,promptResults.getId(),userId);
            node.put("status",promptValidation.getStatus());
            node.put("reason",promptValidation.getReason());
            node.put("planboardId",temporaryPlanboard.getId());
            if(promptValidation.getStatus().equalsIgnoreCase("positive")){
                PromptResults finalPromptResults = promptResults;
                new Thread(()->openAiStrategicNodes(domain,subDomain,scope,geography,userId, finalPromptResults) ).start();
            }
        }
        return ResponseJsonUtil.getResponse(HttpStatus.OK,node);
    }

    private void openAiStrategicNodes(Domain domain,SubDomain subdomain,String scope,String geography,String userId,PromptResults promptResults) {
        log.info("Executing openAiStrategicNodes() method");
        BeanOutputParser<NodeData> outputParser = new BeanOutputParser<>(NodeData.class);
        if(scope==null)
            scope=" ";

        if(geography==null)
            geography=" ";
        String query =
                """
                Provide Only 3 Business strategy steps for {domain} business focusing on {subdomain} focusing in {geography} market. Key departments to focus on {scope}.format pointers in a flat sequential structure.
                {format}
                """;

        Map<String,Object> map  = new HashMap<>();
        map.put("domain",domain.getName());
        map.put("subdomain",subdomain.getName());
        map.put("geography",geography);
        map.put("scope",scope);
        map.put("format",outputParser.getFormat());
        PromptTemplate promptTemplate = new PromptTemplate(query,map);
        Prompt prompt = promptTemplate.create();
        Generation generation = chatClient.call(prompt).getResult();
        NodeData nodeData;
        try{
            nodeData = outputParser.parse(generation.getOutput().getContent());
           promptResults.setStrategicNodes(nodeData.getNodeResponses());
        }catch (Exception e){
            log.error("Exception occurred in generateNodes() method : {}",e.getMessage());
        }
        planboardApiRepository.saveOrUpdatePromptResults(promptResults);

    }


    private  PromptValidation openAiPromptValidation(String domain, String subdomain, String scope, String geography){
        log.info("Executing openAiPromptValidation() method");
        BeanOutputParser<PromptValidation> outputParser = new BeanOutputParser<>(PromptValidation.class);
        if(scope==null)
            scope=" ";

        if(geography==null)
            geography=" ";
        String query =
                """
                Validate this prompt from the business point of view and respond with positive or negative if the prompt makes sense. If this comes out to be negative then provide one line reason as well -\s
                Provide Business strategy steps for {domain} business focusing on {subdomain} focusing in {geography} market. Key departments to focus on {scope}.
                {format}
                """;

        Map<String,Object> map  = new HashMap<>();
        map.put("domain",domain);
        map.put("subdomain",subdomain);
        map.put("geography",geography);
        map.put("scope",scope);
        map.put("format",outputParser.getFormat());
        PromptTemplate promptTemplate = new PromptTemplate(query,map);
        Prompt prompt = promptTemplate.create();
        Generation generation = chatClient.call(prompt).getResult();
        PromptValidation promptValidation;
        try{
            promptValidation = outputParser.parse(generation.getOutput().getContent());
        }catch (Exception e){
            log.error("Exception occurred in validatePrompt() method : {}",e.getMessage());
            promptValidation = new PromptValidation();
            promptValidation.setStatus("negative");
            promptValidation.setReason("Unable to process prompt "+e.getMessage());
        }
        return promptValidation;
    }


    public ResponseEntity<ResponseJsonHandler> createPlanboard(String userId, String planboardId, String workspaceId, String domainId, String subdomainId,boolean markAsDefault,String name, String description, String scope, String geography, String endDate, List<Members> members,String remark, MultipartFile[] multipartFiles){
        Planboard  planboard  =  planboardApiRepository.getPlanboardById(planboardId);
        if(planboard!=null)
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided planboardId already exists");

        planboard=new Planboard();
        planboard.setId(planboardId);
        planboard.setMarkAsDefaultDomain(markAsDefault);
        planboard.setDomainId(domainId);
        planboard.setSubdomainId(subdomainId);
        planboard.setScope(scope);
        planboard.setGeography(geography);

        planboard.setEndDate(formatStringToInstant(endDate));
        members.forEach(e-> e.setStatus(Members.STATUS_PENDING));
        planboard.setMembers(members);

        planboard.setName(name);
        planboard.setDescription(description);
        planboard.setRemark(remark);
        planboard.setWorkspaceId(workspaceId);
        planboard.setUserId(userId);
        planboard.setCreatedOn(Instant.now());
        planboard.setModifiedOn(Instant.now());
        planboard.setActive(true);

        planboard  = planboardApiRepository.saveOrUpdatePlanboard(planboard);
        Planboard finalPlanboard = planboard;

        // Initialize Attachment For Planboard
        if(multipartFiles!=null){
            initializeAttachments(finalPlanboard,multipartFiles);
        }

        // Initialize Strategic Nodes For Planboard
        initializeStrategicNodes(planboard);

        ObjectNode data  = objectMapper.createObjectNode();
        data.put("planboardId",planboard.getId());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

    @Async
    public void initializeAttachments(Planboard planboard ,final MultipartFile[] files){
        for (MultipartFile multipartFile : files) {
            try {
                new Thread(new FileProcessor(planboard,multipartFile,fileStorageServices,planboardApiRepository)).start();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Async
    public void initializeStrategicNodes(Planboard planboard){
        new Thread(()->{
            TemporaryPlanboard temporaryPlanboard  = planboardApiRepository.getTemporaryPlanboardById(planboard.getId());
            if(temporaryPlanboard!=null){
                PromptResults promptResults  = planboardApiRepository.getPromptResultsById(temporaryPlanboard.getPromptId());
                if(promptResults!=null && !CollectionUtils.isEmpty(promptResults.getStrategicNodes())){
                    String parentId=null;
                    for (NodeResponse e : promptResults.getStrategicNodes()) {
                        PlanboardNodes  planboardNodes  = new PlanboardNodes();
                        planboardNodes.setPlanboardId(planboard.getId());
                        planboardNodes.setTitle(e.getTitle());
                        planboardNodes.setDescription(e.getDescription());
                        planboardNodes.setParentId(parentId);
                        planboardNodes.setUserId(planboard.getUserId());
                        planboardNodes.setCreatedOn(Instant.now());
                        planboardNodes.setModifiedOn(Instant.now());
                        planboardNodes.setActive(true);
                        planboardNodes = planboardApiRepository.saveOrUpdatePlanboardNodes(planboardNodes);
                        parentId=planboardNodes.getId();
                    }
                }
            }
        }).start();
    }

    public ResponseEntity<ResponseJsonHandler> planboardSummary(String userId, String planboardId) {
        Planboard planboard  = planboardApiRepository.getPlanboardById(planboardId);
        if(planboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");

        ObjectNode data  = objectMapper.createObjectNode();
        data.put("planboardId",planboard.getId());
        data.put("name",planboard.getName());
        data.put("description",planboard.getDescription());
        data.put("endDate", PlanbowUtility.formatInstantToString(planboard.getEndDate()));
        data.put("createdOn", PlanbowUtility.formatInstantToString(planboard.getCreatedOn()));

        ObjectNode businessArea  = objectMapper.createObjectNode();
        Domain domain = adminApiRepository.getDomainById(planboard.getDomainId());
        SubDomain subDomain = adminApiRepository.getSubdomainById(planboard.getSubdomainId());
        businessArea.put("domain",domain.getName());
        businessArea.put("subdomain",subDomain.getName());
        businessArea.put("scope",planboard.getScope());
        businessArea.put("geography",planboard.getGeography());
        data.set("businessArea",businessArea);
        Set<String> ids  = planboard.getMembers().stream().map(Members::getUserId).collect(Collectors.toSet());

        ArrayNode members  =objectMapper.createArrayNode();
        planboard.getMembers().forEach(e->{
            ObjectNode member  = objectMapper.createObjectNode();
            member.put("userId",e.getUserId());
            member.put("email",e.getEmailId());
            member.put("status",e.getStatus());
            member.put("role",e.getRole());
            member.put("name","");
            member.put("profilePic","");
            member.put("gender","");
            members.add(member);
        });

        data.set("members",members);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }
}
