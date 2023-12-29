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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Default implementation of the Task API.
 *
 * @author ttrigo
 * @since 0.1.0
 */
@RestController
public class TaskRestController implements TaskRestAPI {

    private static final Logger logger = LoggerFactory.getLogger(TaskRestController.class);

    private final TaskService taskService;

    /**
     * Default constructor.
     *
     * @param taskService the service that brings task's business operations, must not be {@literal null}.
     */
    public TaskRestController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override

    public Mono<ResponseEntity<TaskDTO>> getTaskById(UUID id) {
        return this.taskService.findById(id)
                               .map(ResponseEntity::ok)
                               .switchIfEmpty(Mono.just(ResponseEntity.notFound()
                                                                      .build()));
    }

    @Override
    public Flux<TaskDTO> getAllTasks() {
        return this.taskService.findAll();
    }

    @Override
    public Mono<TaskDTO> createTask(TaskDTO taskDTO) {
        logger.info("Creating a new task ...");
        return this.taskService.create(taskDTO)
                               .doOnSuccess(taskCreated -> logger.info("Task {} created successfully", taskCreated.getId()));
    }

    @Override
    public Mono<ResponseEntity<TaskDTO>> updateTask(UUID id, TaskDTO taskDTO) {
        logger.info("Updating the task {} ...", id);
        return this.taskService.update(id, taskDTO)
                               .doOnSuccess(taskUpdated -> logger.info("Task {} updated successfully", id))
                               .map(ResponseEntity::ok)
                               .switchIfEmpty(Mono.just(ResponseEntity.notFound()
                                                                      .build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteTaskById(UUID id) {
        logger.info("Deleting the task {} ...", id);
        return this.taskService.deleteById(id)
                               .doOnSuccess(taskHasBeenDeleted -> logger.info("Task {} deleted successfully", id))
                               .map(taskHasBeenDeleted -> ResponseEntity.status(
                                       Boolean.TRUE.equals(taskHasBeenDeleted) ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND)
                                                                        .build());

    }

}
