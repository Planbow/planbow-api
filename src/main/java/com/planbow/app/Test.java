package com.planbow.app;


import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.planbow.utility.PlanbowUtility.*;

public class Test {
    public static void main(String[] args) {

        Instant instant = Instant.parse("2024-03-24T00:00:00.000Z");

        Duration durationToAdd = Duration.ofHours(23)
                .plusMinutes(59)
                .plusSeconds(59)
                .plusMillis(999);


        instant=instant.plus(durationToAdd);
        System.out.println(instant);
        Instant now  = Instant.now();
        System.out.println(now);

        System.out.println(now.isAfter(instant));
        //System.out.println(convertStringToInstantUTC("22-03-2024 10:00 AM"));
    }

}
