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

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Defines the {@link Task} business operations.
 *
 * @author ttrigo
 * @since 0.1.0
 */
public interface TaskService {

    /**
     * Finds a {@link Task} by the given id.
     *
     * @param id the id of the task to be found, must not be {@literal null}.
     * @return {@link Mono} emitting the found task if the given id exists, otherwise emitting empty.
     */
    Mono<TaskDTO> findById(UUID id);

    /**
     * Finds all {@link Task}.
     *
     * @return {@link Flux} emitting all found task if there are task, otherwise emitting empty.
     */
    Flux<TaskDTO> findAll();

    /**
     * Creates the given {@link Task}.
     * <p>
     * Always creates the task with a new id, therefore in cases where the id of the given task is present it is ignored.
     *
     * @param taskDTO the task to be created, must be a valid task.
     * @return {@link Mono} emitting the task created with the new id.
     */
    Mono<TaskDTO> create(TaskDTO taskDTO);

    /**
     * Updates the {@link Task} with the given id.
     * <p>
     * The id of the task is never updated, so in cases where the given task has id it is ignored.
     *
     * @param id      the id of the task to be updated, must not be {@literal null}.
     * @param taskDTO the new task data.
     * @return {@link Mono} emitting the task updated with the new data if the given id exists, otherwise emitting empty.
     */
    Mono<TaskDTO> update(UUID id, TaskDTO taskDTO);

    /**
     * Deletes a {@link Task} by the given id.
     *
     * @param id the id of the task to be deleted, must not be {@literal null}.
     * @return {@link Mono} emitting true if the given id exists, otherwise emitting false.
     */
    Mono<Boolean> deleteById(UUID id);

}
