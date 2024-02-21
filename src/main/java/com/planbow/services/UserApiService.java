package com.planbow.services;


import com.planbow.documents.users.Locations;
import com.planbow.documents.users.Password;
import com.planbow.documents.users.User;
import com.planbow.documents.users.UserLocation;
import com.planbow.repository.CoreApiRepository;
import com.planbow.repository.PublicApiRepository;
import com.planbow.repository.UserApiRepository;
import com.planbow.security.services.UserDetailsImpl;
import com.planbow.utility.RandomzUtility;
import com.planbow.utility.ResponseConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseConstants;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.planbow.utility.RandomzUtility.DIRECTORY_USERS;

@Service
@Log4j2
@Transactional
public class UserApiService {

    private UserApiRepository userApiRepository;
    private ObjectMapper objectMapper;
    private PublicApiRepository publicApiRepository;
    private CoreApiRepository coreApiRepository;
    private FileStorageServices fileStorageServices;


    @Autowired
    public void setFileStorageServices(FileStorageServices fileStorageServices) {
        this.fileStorageServices = fileStorageServices;
    }

    @Autowired
    public void setCoreApiRepository(CoreApiRepository coreApiRepository) {
        this.coreApiRepository = coreApiRepository;
    }

    @Autowired
    public void setPublicApiRepository(PublicApiRepository publicApiRepository) {
        this.publicApiRepository = publicApiRepository;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setUserApiRepository(UserApiRepository userApiRepository) {
        this.userApiRepository = userApiRepository;
    }

    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseJsonHandler getUser(String userId) {
        User user = publicApiRepository.getUserById(userId);
        if (user == null) {
            return ResponseJsonUtil.getResponse("Provided userId does not exists", 404, ResponseConstants.NOT_FOUND.getStatus(), true);
        }
        ObjectNode node  = objectMapper.createObjectNode();
        node.put("userId",user.getId());
        node.put("name",user.getName());
        node.put("gender",user.getGender());
        node.put("email",user.getEmail());
        node.put("contactNo",user.getContactNo());
        return ResponseJsonUtil.getResponse(node,200,ResponseConstant.SUCCESS.getStatus(),false);
    }

    public ResponseJsonHandler updateUser(RequestJsonHandler requestJsonHandler){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        User user = publicApiRepository.getUserById(userId);
        if (user == null) {
            return ResponseJsonUtil.getResponse("Provided userId does not exists", 404, ResponseConstants.NOT_FOUND.getStatus(), true);
        }

        String name  = requestJsonHandler.getStringValue("name");
        if(name!=null)
            user.setName(name.trim());

        String gender = requestJsonHandler.getStringValue("gender");
        if(gender!=null)
            user.setGender(gender.trim());

        String countryCode = requestJsonHandler.getStringValue("countryCode");
        if(countryCode!=null)
            user.setCountryCode(countryCode.trim());

        String contactNo = requestJsonHandler.getStringValue("contactNo");
        if(contactNo!=null){
            if(user.getContactNo()!=null && !user.getContactNo().equals(contactNo)){
                user.setContactNoVerified(false);
            }
            user.setContactNo(contactNo.trim());
        }


        String deviceId = requestJsonHandler.getStringValue("deviceId");
        if(deviceId!=null)
            user.setDeviceId(deviceId.trim());


        boolean contactNoVerified = requestJsonHandler.getBooleanValue("contactNoVerified");
        if(contactNoVerified)
            user.setContactNoVerified(true);

        boolean emailVerified = requestJsonHandler.getBooleanValue("emailVerified");
        if(emailVerified)
            user.setEmailVerified(true);

        user.setModifiedOn(Instant.now());
        user=userApiRepository.saveOrUpdateUser(user);
        return ResponseJsonUtil.getResponse(
                objectMapper.valueToTree(user),200, ResponseConstant.SUCCESS.getStatus(),false
        );

    }

    public ResponseJsonHandler changePassword(String oldPassword,String newPassword){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        User users  = publicApiRepository.getUserById(userId);
        if(users==null){
            return ResponseJsonUtil.getResponse("Provided userId does not exists",404,"Not Found",true);
        }
        else{
            if(!passwordEncoder.matches(oldPassword,users.getPassword()))
                return ResponseJsonUtil.getResponse(ResponseConstant.INVALID_PASSWORD.getStatus(), 401, "Unauthorized",true);
        }
        users.setPasswordCreatedOn(Instant.now());
        users.setPassword(passwordEncoder.encode(newPassword));
        publicApiRepository.saveOrUpdateUser(users);

        Password password1  = new Password();
        password1.setId(users.getId());
        password1.setPassword(newPassword);
        password1.setActive(true);
        password1.setCreatedOn(Instant.now());
        password1.setModifiedOn(Instant.now());
        publicApiRepository.saveOrUpdatePassword(password1);


        return ResponseJsonUtil.getResponse(
                "Password changed successfully",200, ResponseConstant.SUCCESS.getStatus(),false
        );
    }

    public ResponseJsonHandler updateLocation(Double latitude, Double longitude) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        User users  = publicApiRepository.getUserById(userId);
        if(users==null){
            return ResponseJsonUtil.getResponse("Provided userId does not exists",404,"Not Found",true);
        }
        UserLocation userLocation =userApiRepository.getUserLocation(userId);
        if(userLocation ==null){
            userLocation = new UserLocation();
            userLocation.setLatitude(latitude);
            userLocation.setLongitude(longitude);
            userLocation.setActive(true);
            userLocation.setId(userId);
            userLocation.setCreatedOn(Instant.now());
        }
        userLocation.setModifiedOn(Instant.now());
        userApiRepository.saveOrUpdateLocation(userLocation);
        return ResponseJsonUtil.getResponse(
                "UserLocation updated successfully",200, ResponseConstant.SUCCESS.getStatus(),false
        );
    }

    public ResponseJsonHandler getUsers(double distance, Double latitude, Double longitude) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        Locations locations;
        if(latitude!=null && longitude!=null){
            locations=RandomzUtility.getLocationCoordinate(distance,latitude,longitude);
        }else{
            UserLocation userLocation  = userApiRepository.getUserLocation(userId);
            locations=RandomzUtility.getLocationCoordinate(distance,userLocation.getLatitude(),userLocation.getLongitude());
        }
        List<UserLocation> userLocations = userApiRepository.getUserLocations(userId,locations.getMinLat(), locations.getMaxLat(), locations.getMinLng(), locations.getMaxLng());
        System.out.println(userLocations.size());
        Set<String> userIds = userLocations.stream().map(UserLocation::getId).collect(Collectors.toSet());
        List<User> users  = userApiRepository.getUsers(new ArrayList<>(userIds));
        ArrayNode mapUsers  = objectMapper.createArrayNode();

        users.forEach(e->{
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id",e.getId());
            node.put("name",e.getName());
            node.put("email",e.getEmail());
            node.put("gender",e.getGender());
            node.put("profilePic",e.getProfilePic());
            node.put("contactNo",e.getContactNo());
            node.put("countryCode",e.getCountryCode());
            UserLocation location=getUserLocation(userLocations,e.getId());
            node.put("latitude",location.getLatitude());
            node.put("longitude",location.getLongitude());
            mapUsers.add(node);
        });
        return ResponseJsonUtil.getResponse(mapUsers,200,ResponseConstant.SUCCESS.getStatus(),false);
    }

    private UserLocation getUserLocation(List<UserLocation> userLocations,String id){
        return userLocations.stream().filter(f-> f.getId().equals(id)).findAny().get();
    }

    public ResponseJsonHandler uploadProfile(RequestJsonHandler requestJsonHandler, MultipartFile filePart) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        User users = userApiRepository.getUserById(userId);
        if(users==null){
            return ResponseJsonUtil.getResponse("Provided userId does not exists",404,"Not Found",true);
        }
        if(filePart!=null){
            String folder=DIRECTORY_USERS+ File.separator+userId;
            String mediaUrl=fileStorageServices.uploadFile(folder, RandomzUtility._defaultFileName(RandomzUtility.MEDIA_IMAGE),filePart);
            if(mediaUrl!=null){
                users.setProfilePic(mediaUrl);
            }
        }
        users=userApiRepository.saveOrUpdateUser(users);
        return ResponseJsonUtil.getResponse(objectMapper.valueToTree(users),200, ResponseConstant.SUCCESS.getStatus(),false);
    }
}
