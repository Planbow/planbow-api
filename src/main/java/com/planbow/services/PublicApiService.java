package com.planbow.services;


import com.planbow.documents.token.RefreshToken;
import com.planbow.documents.users.Password;
import com.planbow.documents.users.User;
import com.planbow.repository.PublicApiRepository;
import com.planbow.repository.UserApiRepository;
import com.planbow.security.jwt.JwtUtils;
import com.planbow.security.response.TokenInfo;
import com.planbow.security.services.UserDetailsImpl;
import com.planbow.utility.RandomzUtility;
import com.planbow.utility.ResponseConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;
import com.planbow.util.json.handler.request.RequestJsonHandler;
import com.planbow.util.json.handler.response.ResponseJsonHandler;
import com.planbow.util.json.handler.response.util.ResponseConstants;
import com.planbow.util.json.handler.response.util.ResponseJsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import static com.planbow.utility.RandomzUtility.getToken;

@Service
@Transactional
@Log4j2
public class PublicApiService {

    private PublicApiRepository publicApiRepository;
    private ObjectMapper objectMapper;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;
    private PasswordEncoder passwordEncoder;

    private Configuration configuration;
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String host;

    @Value("${security.config.jwt.expiration.accessToken}")
    private long accessTokenExpiration;

    @Value("${security.config.jwt.expiration.refreshToken}")
    private long refreshTokenExpiration;


    private UserApiRepository userApiRepository;


    @Autowired
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Autowired
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    @Autowired
    public void setUserApiRepository(UserApiRepository userApiRepository) {
        this.userApiRepository = userApiRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setPublicApiRepository(PublicApiRepository publicApiRepository) {
        this.publicApiRepository = publicApiRepository;
    }

    public ResponseEntity<ResponseJsonHandler> authenticateUser(String email, String password,RequestJsonHandler requestJsonHandler) {
        log.info("Executing authenticateUser() method for email: {}",email);
        User user = publicApiRepository.getUser(email);
        if (user == null) {
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided email does not exists");
        }
        if(!passwordEncoder.matches(password,user.getPassword())){
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,ResponseConstant.INVALID_PASSWORD.getStatus());
        }
        user.setDeviceId(requestJsonHandler.getStringValue("deviceId"));
        publicApiRepository.saveOrUpdateUser(user);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(),password, authorities));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        TokenInfo tokenInfo=getToken(userDetails, createRefreshToken(userDetails.getId()), accessTokenExpiration, jwtUtils.generateJwtToken(authentication));
        ObjectNode data  = objectMapper.createObjectNode();
        data.set("token",objectMapper.valueToTree(tokenInfo));
        //data.set("user",objectMapper.valueToTree(user));
        log.info("Returning response from authenticateUser() method  with response: {}",data);
        return ResponseJsonUtil.getResponse(HttpStatus.OK,data);
    }


    public RefreshToken createRefreshToken(String userId) {
        log.info("Executing createRefreshToken() method for userId: {}",userId);
        RefreshToken refreshToken = publicApiRepository.getRefreshToken(userId);
        if (refreshToken == null)
            refreshToken = new RefreshToken();

        refreshToken.setId(userId);
        refreshToken.setCreatedOn(Instant.now());
        refreshToken.setExpiredOn(Instant.now().plusMillis(refreshTokenExpiration));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setActive(true);
        refreshToken = publicApiRepository.saveOrUpdateRefreshToken(refreshToken);
        log.info("Returning response from createRefreshToken() method ");
        return refreshToken;
    }

    public ResponseEntity<ResponseJsonHandler> refreshToken(String token) {
        log.info("Executing refreshToken() method for token: {}",token);
        RefreshToken refreshToken = publicApiRepository.getRefreshTokenByToken(token);
        if (refreshToken == null) {
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Refresh Token does not exists");
        }
        if (refreshToken.getExpiredOn().compareTo(Instant.now()) < 0) {
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"Refresh Token is expired");
        }
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setRefreshToken(token);
        tokenInfo.setExpireIn(accessTokenExpiration);
        User user = publicApiRepository.getUserById(refreshToken.getId());
        tokenInfo.setAccessToken(jwtUtils.generateJwtToken(user.getEmail()));
        tokenInfo.setUsername(user.getEmail());
        log.info("Returning response from refreshToken() method ");
        return ResponseJsonUtil.getResponse(HttpStatus.OK,tokenInfo);

    }

    public ResponseJsonHandler authenticateWithSocialAccount(String email,String deviceId, RequestJsonHandler requestJsonHandler){
        User users  = userApiRepository.getUser(email);
        String password="google";
        if(users==null){

            users = new User();
            users.setEmail(email);
            users.setName(requestJsonHandler.getStringValue("name"));
            users.setProfilePic(requestJsonHandler.getStringValue("profilePic"));
            users.setRole(User.ROLE_USER);
            users.setDeviceId(deviceId);
            users.setActive(true);
            users.setEmailVerified(true);
            users.setCreatedOn(Instant.now());
            users.setModifiedOn(Instant.now());
            users.setPasswordCreatedOn(Instant.now());
            users.setProvider(User.PROVIDER_GOOGLE);
            users.setPassword(passwordEncoder.encode(password));
            users.setModifiedOn(Instant.now());
            users = userApiRepository.saveOrUpdateUser(users);

            Password password1  = new Password();
            password1.setId(users.getId());
            password1.setPassword(password);
            password1.setActive(true);
            password1.setCreatedOn(Instant.now());
            password1.setModifiedOn(Instant.now());
            publicApiRepository.saveOrUpdatePassword(password1);

        }else{
            users.setDeviceId(deviceId);
            users.setName(requestJsonHandler.getStringValue("name"));
            users.setProfilePic(requestJsonHandler.getStringValue("profilePic"));
            users.setModifiedOn(Instant.now());
            users = userApiRepository.saveOrUpdateUser(users);

            Password password1  = publicApiRepository.getPassword(users.getId());
            password = password1.getPassword();
        }


        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(users.getEmail(),password, authorities));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        TokenInfo tokenInfo=getToken(userDetails, createRefreshToken(userDetails.getId()), accessTokenExpiration, jwtUtils.generateJwtToken(authentication));
        ObjectNode data  = objectMapper.createObjectNode();
        data.set("token",objectMapper.valueToTree(tokenInfo));
        data.set("user",objectMapper.valueToTree(users));

        return ResponseJsonUtil.getResponse(
                objectMapper.valueToTree(data),200, ResponseConstant.SUCCESS.getStatus(),false
        );

    }


    public ResponseEntity<ResponseJsonHandler> createAccount(String email, String name, String contactNo, String gender, String password, RequestJsonHandler requestJsonHandler) {

        if(publicApiRepository.isEmailExists(email)){
            return ResponseJsonUtil.getResponse(HttpStatus.CONFLICT,"Provided email address already exists");
        }

        User users  = new User();
        users.setEmail(email);
        users.setName(name);
        users.setContactNo(contactNo);
        users.setGender(gender);
        users.setRole(User.ROLE_USER);
        users.setCountryCode(requestJsonHandler.getStringValue("countryCode"));
        users.setDeviceId(requestJsonHandler.getStringValue("deviceId"));
        users.setContactNoVerified(requestJsonHandler.getBooleanValue("contactVerified"));
        users.setEmailVerified(requestJsonHandler.getBooleanValue("emailVerified"));
        users.setActive(true);
        users.setCreatedOn(Instant.now());
        users.setModifiedOn(Instant.now());
        users.setPasswordCreatedOn(Instant.now());
        users.setProvider(User.PROVIDER_MANUAL);
        users.setPassword(passwordEncoder.encode(password));
        users = userApiRepository.saveOrUpdateUser(users);

        Password password1  = new Password();
        password1.setId(users.getId());
        password1.setPassword(password);
        password1.setActive(true);
        password1.setCreatedOn(Instant.now());
        password1.setModifiedOn(Instant.now());
        publicApiRepository.saveOrUpdatePassword(password1);

        return ResponseJsonUtil.getResponse(HttpStatus.OK, objectMapper.valueToTree(users));
    }



    public ResponseEntity<ResponseJsonHandler> forgotPassword(String email) {
        User users = publicApiRepository.getUser(email);
        if(users==null)
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided email does not exists");

        configuration .setClassForTemplateLoading(this.getClass(),"/templates/");

        int m = (int) Math.pow(10, 6 - 1);
        int otp= m + new Random().nextInt(9 * m);

        users.setOtp(otp);
        userApiRepository.saveOrUpdateUser(users);
        Map<String, Object> model = new HashMap<>();
        model.put("name", users.getName());
        model.put("date", RandomzUtility.formatDate(new Date()));

        String content = """
                Use the following OTP to reset your password.OTP is valid for
                <span style="font-weight: 600; color: #1f1f1f;">15 minutes</span>.
                Do not share this code with others
                """;

        model.put("content",content);
        model.put("otp", String.valueOf(users.getOtp()));
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        Template t = null;
        try {
            t = configuration.getTemplate("otp.ftl");
            String text = FreeMarkerTemplateUtils.processTemplateIntoString(t,model);

            helper.setTo(users.getEmail());
            helper.setFrom(host);
            helper.setSubject("Reset Password - Planbow");
            helper.setText(text,true);
            mailSender.send(message);
        }
        catch (TemplateException | MessagingException | IOException e) {
            e.printStackTrace();
        }

        return ResponseJsonUtil.getResponse(HttpStatus.OK,
                "A reset password otp has been sent to your email"
        );
    }

    public ResponseEntity<ResponseJsonHandler> verifyOtp(String email,int otp){
        User users  = publicApiRepository.getUser(email);
        if(users==null){
            return ResponseJsonUtil.getResponse(HttpStatus.BAD_REQUEST,"Email not found");
        }
        if(users.getOtp()!= otp){
            return ResponseJsonUtil.getResponse(HttpStatus.UNAUTHORIZED,"Invalid otp provided");
        }
        ObjectNode node  = objectMapper.createObjectNode();
        node.put("id",users.getId());
        node.put("email",users.getEmail());
        node.put("name",users.getName());
        return ResponseJsonUtil.getResponse(
                HttpStatus.OK,node);
    }



    public ResponseEntity<ResponseJsonHandler> setPassword(String userId,String password){
        User users  = userApiRepository.getUserById(userId);
        if(users==null){
            return ResponseJsonUtil.getResponse(HttpStatus.NOT_FOUND,"Provided userId does not exists");
        }
        users.setPasswordCreatedOn(Instant.now());
        users.setPassword(passwordEncoder.encode(password));
        userApiRepository.saveOrUpdateUser(users);

        Password password1  = new Password();
        password1.setId(users.getId());
        password1.setPassword(password);
        password1.setActive(true);
        password1.setCreatedOn(Instant.now());
        password1.setModifiedOn(Instant.now());
        publicApiRepository.saveOrUpdatePassword(password1);


        return ResponseJsonUtil.getResponse(
                HttpStatus.OK,"Password changed successfully");
    }




}
