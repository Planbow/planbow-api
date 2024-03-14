package com.planbow.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planbow.documents.core.Domain;
import com.planbow.documents.core.SubDomain;
import com.planbow.documents.global.MeetingType;
import com.planbow.documents.open.ai.NodeData;
import com.planbow.documents.open.ai.NodeResponse;
import com.planbow.documents.open.ai.PromptValidation;
import com.planbow.documents.planboard.*;
import com.planbow.documents.prompts.PromptResults;
import com.planbow.entities.user.UserEntity;
import com.planbow.repository.AdminApiRepository;
import com.planbow.repository.GlobalApiRepository;
import com.planbow.repository.PlanboardApiRepository;
import com.planbow.repository.PlanbowHibernateRepository;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import com.planbow.utility.FileProcessor;
import com.planbow.utility.PlanbowUtility;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
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
    private PlanbowHibernateRepository planbowHibernateRepository;
    private GlobalApiRepository globalApiRepository;
    private EmailService emailService;


    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Autowired
    public void setGlobalApiRepository(GlobalApiRepository globalApiRepository) {
        this.globalApiRepository = globalApiRepository;
    }

    @Autowired
    public void setPlanbowHibernateRepository(PlanbowHibernateRepository planbowHibernateRepository) {
        this.planbowHibernateRepository = planbowHibernateRepository;
    }

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
                        Provide Business strategy steps for {domain} business focusing on {subdomain} focusing in {geography} market. Key departments to focus on {scope}
                        Provide the results in array of object that contains title and description as string
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
            log.info("Executing of openAiStrategicNodes() method completed for promptId: {} ",promptResults.getId());
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
                Provide the results in object that contains status and reason as string
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
        String content=generation.getOutput().getContent();
        PromptValidation promptValidation;
        try{
            promptValidation = outputParser.parse(content);
            log.info("Executing of openAiPromptValidation() method completed ");
        }catch (Exception e){
            log.error("Exception occurred in validatePrompt() method : {}",e.getMessage());
            promptValidation = new PromptValidation();
            promptValidation.setStatus("negative");
            promptValidation.setReason("Unable to process prompt "+e.getMessage());
        }
        return promptValidation;
    }


    public ResponseEntity<ResponseJsonHandler> createPlanboard(String userId, String planboardId, String workspaceId, String domainId, String subdomainId,boolean markAsDefault,String name, String description, String scope, String geography, String endDate, List<Members> members,String remark,ObjectNode schedule, MultipartFile[] multipartFiles){
        Planboard  planboard  =  planboardApiRepository.getPlanboardById(planboardId);
        if(planboard!=null)
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided planboardId already exists");

        if(planboardApiRepository.isPlanboardExists(name, userId, workspaceId)){
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided planboard name already exists");
        }
        if(schedule!=null){
            MeetingType meetingType  = globalApiRepository.getMeetingTypesById(schedule.get("meetingTypeId").asText());
            if(meetingType==null)
                return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided meetingTypeId does not exists");
        }
        planboard=new Planboard();
        planboard.setId(planboardId);
        planboard.setMarkAsDefaultDomain(markAsDefault);
        planboard.setDomainId(domainId);
        planboard.setSubdomainId(subdomainId);
        planboard.setScope(scope);
        planboard.setGeography(geography);

        planboard.setEndDate(formatStringToInstant(endDate,null));
        if(!CollectionUtils.isEmpty(members)){
            members.forEach(e-> e.setStatus(Members.STATUS_PENDING));
            planboard.setMembers(members);
        }

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
        // Initialize Strategic Nodes For Planboard
        initializeStrategicNodes(planboard);
        // Initialize Event
        initializeEvents(planboard,schedule);
        // Invite Members
        inviteMembers(planboard,schedule);
        // Initialize Attachment For Planboard
        if(multipartFiles!=null){
            initializeAttachments(finalPlanboard,multipartFiles);
        }
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
    @Async
    public void initializeEvents(Planboard planboard,ObjectNode schedule){
        new Thread(()->{
            if(schedule!=null){
                Instant start  = convertStringToInstantUTC(schedule.get("date").asText()+" "+schedule.get("start").asText());
                Instant end  = convertStringToInstantUTC(schedule.get("date").asText()+" "+schedule.get("end").asText());
                List<Events> events = new ArrayList<>();
                Set<String> ids=new HashSet<>();
                ids.add(planboard.getUserId());
                if(!CollectionUtils.isEmpty(planboard.getMembers())){
                    ids.addAll(planboard.getMembers().stream().map(Members::getUserId).filter(userId -> !StringUtils.isEmpty(userId)).collect(Collectors.toSet()));
                }
                ids.forEach(e->{
                    Events event  = new Events();
                    event.setPlanboardId(planboard.getId());
                    event.setTitle("Planboard - "+planboard.getName());
                    event.setDescription(null);
                    event.setUserId(e);
                    event.setStart(start);
                    event.setEnd(end);
                    event.setCreatedBy(planboard.getUserId());
                    event.setCreatedOn(Instant.now());
                    event.setModifiedOn(Instant.now());
                    event.setActive(true);
                    events.add(event);
                });
                planboardApiRepository.saveEvents(events);
            }

        }).start();
    }
    @Async
    public void inviteMembers(Planboard planboard,ObjectNode schedule){
        new Thread(()->{
            UserEntity owner  = planbowHibernateRepository.getUserEntity(Long.valueOf(planboard.getUserId()));
            Instant start;
            if(schedule!=null){
                start=convertStringToInstantUTC(schedule.get("date").asText()+" "+schedule.get("start").asText());
            } else {
                start = null;
            }
            Set<String> ids=new HashSet<>();
            if(!CollectionUtils.isEmpty(planboard.getMembers())){
                ids.addAll(planboard.getMembers().stream().map(Members::getUserId).filter(userId -> !StringUtils.isEmpty(userId)).collect(Collectors.toSet()));
            }
            List<UserEntity> userEntities  = planbowHibernateRepository.getUserEntities(null,new ArrayList<>(ids));

            if(!CollectionUtils.isEmpty(planboard.getMembers())){
                planboard.getMembers().forEach(e->{
                    if(!StringUtils.isEmpty(e.getUserId())){
                        UserEntity userEntity  = PlanbowUtility.getUserEntity(userEntities,Long.valueOf(e.getUserId()));
                        emailService.planboardInvite(planboard,owner,userEntity,null,e.getRole(),start);
                    }else{
                        emailService.planboardInvite(planboard,owner,null,e.getEmailId(),e.getRole(),start);
                    }
                });
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
        data.put("endDate", PlanbowUtility.formatInstantToString(planboard.getEndDate(),null));
        data.put("createdOn", PlanbowUtility.formatInstantToString(planboard.getCreatedOn(),null));

        ObjectNode businessArea  = objectMapper.createObjectNode();
        Domain domain = adminApiRepository.getDomainById(planboard.getDomainId());
        SubDomain subDomain = adminApiRepository.getSubdomainById(planboard.getSubdomainId());
        businessArea.put("domain",domain.getName());
        businessArea.put("subdomain",subDomain.getName());
        businessArea.put("scope",planboard.getScope());
        businessArea.put("geography",planboard.getGeography());
        data.set("businessArea",businessArea);

        ArrayNode members  =objectMapper.createArrayNode();
        if(!CollectionUtils.isEmpty(planboard.getMembers())){
            Set<String> ids  = planboard.getMembers().stream().map(Members::getUserId).collect(Collectors.toSet());
            List<UserEntity> userEntities = planbowHibernateRepository.getUserEntities(null,new ArrayList<>(ids));
            planboard.getMembers().forEach(e->{
                ObjectNode member  = objectMapper.createObjectNode();
                member.put("userId",e.getUserId());
                member.put("email",e.getEmailId());
                member.put("status",e.getStatus());
                member.put("role",e.getRole());
                UserEntity userEntity  = PlanbowUtility.getUserEntity(userEntities,Long.valueOf(e.getUserId()));
                if(userEntity!=null){
                    member.put("name",userEntity.getName());
                    member.put("profilePic",userEntity.getProfilePic());
                    member.put("gender",userEntity.getGender());
                }else{
                    member.set("name",objectMapper.valueToTree(null));
                    member.set("profilePic",objectMapper.valueToTree(null));
                    member.set("gender",objectMapper.valueToTree(null));
                }
                members.add(member);
            });
            data.set("members",members);
        }else{
            data.set("members",objectMapper.valueToTree(null));
        }


        ArrayNode attachmentNode = objectMapper.createArrayNode();
        List<Attachments> attachments = planboardApiRepository.getAttachments(planboardId,Attachments.TYPE_ROOT);
        attachments.forEach(e->{
            ObjectNode attachment  = objectMapper.createObjectNode();
            attachment.put("id",e.getId());
            attachment.put("name",e.getMetaData().getFileName());
            attachment.put("extension",e.getMetaData().getExtension());
            attachment.put("size",formatFileSize(e.getMetaData().getSize()));
            attachment.put("mediaUrl",e.getMediaUrl());
            attachment.set("uploadedOn",objectMapper.valueToTree(e.getUploadedOn()));
            attachmentNode.add(attachment);
        });
        data.set("attachments",attachmentNode);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

    public ResponseEntity<ResponseJsonHandler> getStrategicNodes(String planboardId) {
        TemporaryPlanboard temporaryPlanboard  = planboardApiRepository.getTemporaryPlanboardById(planboardId);
        if(temporaryPlanboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");
        PromptResults promptResults  = planboardApiRepository.getPromptResultsById(temporaryPlanboard.getPromptId());
        ArrayNode data  = objectMapper.createArrayNode();
        if(promptResults!=null && !CollectionUtils.isEmpty( promptResults.getStrategicNodes())){
            promptResults.getStrategicNodes().forEach(e->{
                ObjectNode node  = objectMapper.createObjectNode();
                node.put("title",e.getTitle());
                node.put("description",e.getDescription());
                data.add(node);
            });
        }
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }
    public ResponseEntity<ResponseJsonHandler> getPlanboardNodes(String planboardId,String userId) {
        ArrayNode data  = objectMapper.createArrayNode();
        List<PlanboardNodesAggregation> documents = planboardApiRepository.getPlanboardNodes(planboardId);

        documents.parallelStream().forEach(e->{
                    Set<String> ids = e.getChildren().parallelStream().map(PlanboardNodes::getId).collect(Collectors.toSet());
                    ObjectNode node  = objectMapper.createObjectNode();
                    node.put("id",e.getId());
                    node.put("title",e.getTitle());
                    node.put("description",e.getDescription());
                    node.put("color",e.getColor());
                    node.put("parentId",e.getParentId());
                    node.put("parentId",e.getParentId());
                    node.put("createdOn",PlanbowUtility.formatInstantToString(e.getCreatedOn(),null));
                    node.set("childIds",objectMapper.valueToTree(ids));
                    data.add(node);
                }
        );
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }
    public ResponseEntity<ResponseJsonHandler> updatePlanboard(String planboardId, String userId, RequestJsonHandler requestJsonHandler) {
        Planboard planboard  = planboardApiRepository.getPlanboardById(planboardId);
        if(planboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");
        if(!planboard.getUserId().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"You are not authorized to access this planboard");


        String name  = requestJsonHandler.getStringValue("name");
        if(!StringUtils.isEmpty(name)){
            if(!name.equals(planboard.getName())){
                if(planboardApiRepository.isPlanboardExists(name,userId,planboard.getWorkspaceId())){
                    return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided planboard name already exists");
                }
                else{
                    planboard.setName(name.trim());
                }
            }
        }
        String description  = requestJsonHandler.getStringValue("description");
        if(!StringUtils.isEmpty(description)){
            if(!description.equals(planboard.getDescription())){
                planboard.setDescription(description.trim());
            }
        }

        String endDate  = requestJsonHandler.getStringValue("endDate");
        if(StringUtils.isEmpty(endDate))
            planboard.setEndDate(formatStringToInstant(endDate,null));

        planboardApiRepository.saveOrUpdatePlanboard(planboard);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,"Planboard successfully updated");
    }
    public ResponseEntity<ResponseJsonHandler> removeMember(String planboardId, String userId, String memberId) {
        Planboard planboard  = planboardApiRepository.getPlanboardById(planboardId);
        if(planboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");
        if(!planboard.getUserId().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"You are not authorized to access this planboard");

        if(CollectionUtils.isEmpty(planboard.getMembers())){

        }

        planboardApiRepository.saveOrUpdatePlanboard(planboard);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,"Planboard successfully updated");
    }


    public ResponseEntity<ResponseJsonHandler> removeAttachment(String planboardId, String attachmentId, String userId) {
        Planboard planboard  = planboardApiRepository.getPlanboardById(planboardId);
        if(planboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");
        if(!planboard.getUserId().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"You are not authorized to access this planboard");
        Attachments attachments  = planboardApiRepository.getAttachment(attachmentId,planboardId);
        if(attachments==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided attachmentId does not exists");

        new Thread(()-> fileStorageServices.deleteFiles(List.of(attachments.getMediaUrl()))).start();
        planboardApiRepository.deleteAttachment(attachments);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,"Attachment successfully deleted");
    }

    public ResponseEntity<ResponseJsonHandler> addAttachment(String userId, String planboardId, MultipartFile[] multipartFiles){
        Planboard  planboard  =  planboardApiRepository.getPlanboardById(planboardId);
        if(planboard==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided planboardId does not exists");
        if(!planboard.getUserId().equals(userId))
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"You are not authorized to access this planboard");

        if(multipartFiles!=null){
            initializeAttachments(planboard,multipartFiles);
        }
        ObjectNode data  = objectMapper.createObjectNode();
        data.put("planboardId",planboard.getId());
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }

}
