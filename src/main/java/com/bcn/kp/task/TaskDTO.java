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
package com.bcn.kp.task;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a Task DTO.
 *
 * @author ttrigo
 * @since 0.1.0
 */
@Data
@Builder
public class TaskDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4310500786963515624L;

    /**
     * The id of the task.
     */
    private UUID id;

    /**
     * The title of the task, must not be {@literal null}.
     */
    @NotBlank(message = "The title of the task is mandatory")
    private String title;

    /**
     * The description of the task.
     */
    private String description;

    /**
     * The start date of the task.
     */
    private LocalDateTime startDateTime;

}
