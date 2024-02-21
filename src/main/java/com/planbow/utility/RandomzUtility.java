package com.planbow.utility;


import com.planbow.documents.token.RefreshToken;
import com.planbow.documents.users.Locations;
import com.planbow.security.response.TokenInfo;
import com.planbow.security.services.UserDetailsImpl;
import com.planbow.util.utility.core.Utility;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Log4j2
@Component
public class RandomzUtility {

    public static final String MOBILE_REGEX = "^[1-9][0-9]{9}$";

    public static final String DOB_REGEX = "([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))";
    public static final String PAN_CARD_REGEX = "^[A-Za-z]{5}\\d{4}[A-Za-z]{1}$";
    public static final String EXCLUDED_COMMON_FIELDS = "createdBy,modifiedBy,createdOn,modifiedOn";
    public static final String COLLECTION_PROFESSIONS = "professions";
    public static final String COLLECTION_SUB_PROFESSIONS = "subProfessions";
    public static final String COLLECTION_GENDERS = "genders";

    public static final String MEDIA_IMAGE="image";
    public static final String MEDIA_VIDEO="video";
    public static final String MEDIA_FILE="file";

    public static final String DIRECTORY_ROOT="randomz-rsh";
    public static final String DIRECTORY_USERS=DIRECTORY_ROOT+ File.separator+"users";
    public static final String DIRECTORY_EXAMS=DIRECTORY_ROOT+ File.separator+"exams";
    public static final String DIRECTORY_CONVERSATION=DIRECTORY_ROOT+ File.separator+"conversation";

    private RandomzUtility() {
        log.info("Disabling object instantiation");
    }


    public static int generateOtp() {
        return ThreadLocalRandom.current().nextInt(100000, 999999);
    }

    public static int getIntegerLength(int number) {
        return (int) (Math.log10(number) + 1);
    }

    public static TokenInfo getToken(UserDetailsImpl userDetails, RefreshToken refreshToken, long expireIn, String jwt) {
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setAccessToken(jwt);
        tokenInfo.setRefreshToken(refreshToken.getToken());
        tokenInfo.setId(userDetails.getId());
        tokenInfo.setUsername(userDetails.getUsername());
        tokenInfo.setEmail(userDetails.getEmail());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        tokenInfo.setRoles(roles);
        tokenInfo.setExpireIn(expireIn);
        return tokenInfo;
    }


    public static String formatDate(Date date){
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
        return  outputFormat.format(date);
    }


    public static Locations getLocationCoordinate(double distance, double latitude, double longitude) {

        Locations locations = new Locations();

        List<Double> lats  = new ArrayList<>();
        List<Double> lngs  = new ArrayList<>();

        for(int i=1 ; i<=360 ;i++){
            try {

                double dist = distance / 6371.0;
                double brng = Math.toRadians(i);
                double lat1 = Math.toRadians(latitude);
                double lon1 = Math.toRadians(longitude);

                double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
                double lon2 = lon1 + (Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2)));

                lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

                lats.add(Math.toDegrees(lat2));
                lngs.add(Math.toDegrees(lon2));
            } catch (Exception e) {

            }
        }
        System.out.println(lats.stream().max(Double::compare).get());


        locations.setMaxLat(lats.stream().max(Double::compare).get());
        locations.setMinLat(lats.stream().min(Double::compare).get());

        locations.setMaxLng(lngs.stream().max(Double::compare).get());
        locations.setMinLng(lngs.stream().min(Double::compare).get());
        return locations;
    }

    public static boolean isBetween(double min, double max, double value) {
        return max > min ? value > min && value < max : value > max && value < min;
    }

    public static InputStream getInputStreamFromFluxDataBuffer(Flux<DataBuffer> data) throws IOException {
        PipedOutputStream osPipe = new PipedOutputStream();
        PipedInputStream isPipe = new PipedInputStream(osPipe);
        DataBufferUtils.write(data, osPipe)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnComplete(() -> {
                    try {
                        osPipe.close();
                    } catch (IOException ignored) {
                    }
                })
                .subscribe(DataBufferUtils.releaseConsumer());
        return isPipe;
    }

    public static String _defaultFileName(String type){
        if(type.equals(MEDIA_IMAGE))
            return "IMG_"+ Utility.getCustomTimestamp().getTime()+".jpg";
        else
            return "VID_"+ Utility.getCustomTimestamp().getTime()+".mp4";

    }

}
