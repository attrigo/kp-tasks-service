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

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

/**
 * Open API configuration.
 *
 * @author ttrigo
 * @since 0.1.0
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Task Service API", version = "0.1.0-SNAPSHOT", description = "Provides CRUD operations for Tasks", license = @License(name = "Apache-2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")))
public class OpenApiConfiguration {}
