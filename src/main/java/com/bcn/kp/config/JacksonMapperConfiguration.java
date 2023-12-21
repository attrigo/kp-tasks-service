/**
* Copyright 2023 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.bcn.kp.config;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * JSON mapper configuration.
 *
 * @author ttrigo
 * @since 0.1.0
 */
@Configuration
public class JacksonMapperConfiguration {

    @Value("${spring.webflux.format.date}")
    private String dateFormat;

    @Value("${spring.webflux.format.date-time}")
    private String dateTimeFormat;

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
            builder.simpleDateFormat(this.dateTimeFormat)
                   .serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(this.dateFormat)),
                           new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(this.dateTimeFormat)))
                   .deserializers(new LocalDateDeserializer(DateTimeFormatter.ofPattern(this.dateFormat)),
                           new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(this.dateTimeFormat)))
                   .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }

}
