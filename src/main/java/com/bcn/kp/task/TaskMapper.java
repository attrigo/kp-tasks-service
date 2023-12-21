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

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Tasks.
 * <p>
 * Brings multiple ways to map between {@link Task} and {@link TaskDTO}.
 * <p>
 * This class is an interface that only contains the operations signature, the final implementation is generated during compilation time by MapStruct tool.
 *
 * @author ttrigo
 * @since 0.1.0
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

    /**
     * Maps all fields from a {@link Task} to {@link TaskDTO}.
     *
     * @param task the source task.
     * @return the task DTO containing all fields mapped from the task entity.
     */
    TaskDTO toTaskDTO(Task task);

    /**
     * Maps all fields from a {@link TaskDTO} to {@link Task}.
     *
     * @param taskDTO the source task DTO.
     * @return the task entity containing all fields mapped from the task DTO.
     */
    Task toTask(TaskDTO taskDTO);

    /**
     * Maps all fields from a {@link TaskDTO} to {@link Task} except the id.
     *
     * @param taskDTO the source task DTO.
     * @return the task entity containing the fields mapped from the task DTO.
     */
    @Mapping(target = "id", ignore = true)
    Task toTaskIgnoreId(TaskDTO taskDTO);

}
