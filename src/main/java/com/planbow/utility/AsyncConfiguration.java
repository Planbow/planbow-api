/* * Copyright (c) 2022, SiriusXM Connected Vehicle Services Inc. All rights reserved. *
 *  * This software is the confidential and proprietary information of *
 * SiriusXM Connected Vehicle Services Inc. "Confidential Information".
 * You shall * not disclose such Confidential Information and shall use it only in *
 *  accordance with the terms of the intellectual property agreement *
 * you entered into with SiriusXM Connected Vehicle Services Inc. *
 *  THIS SOFTWARE IS INTENDED STRICTLY FOR INTERNAL USE BY SIRIUS XM CONNECTED *
 * VEHICLE SERVICES INC. AND ITS PARENT AND/OR SUBSIDIARY COMPANIES. SIRIUS XM *
 * MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, *
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF *
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. *
 * SIRIUS XM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY ANY PARTY AS A RESULT *
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * */
package com.planbow.utility;


import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Log4j2
@NoArgsConstructor
public class AsyncConfiguration {
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        log.info("Executing asyncExecutor() method");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("async-thread");
        executor.initialize();
        return executor;
    }

}
