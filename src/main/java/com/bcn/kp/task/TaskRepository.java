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

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

/**
 * Repository that provides CRUD operations to manage {@link Task}.
 *
 * @author ttrigo
 * @since 0.1.0
 */
@Repository
public interface TaskRepository extends R2dbcRepository<Task, UUID> {

    /**
     * Deletes a {@link Task} with the given id.
     *
     * @param id the id of the task to be deleted, must not be {@literal null}.
     * @return {@link Mono} signaling one when the task has been deleted, otherwise signaling zero.
     */
    Mono<Long> deleteTaskById(UUID id);

}
