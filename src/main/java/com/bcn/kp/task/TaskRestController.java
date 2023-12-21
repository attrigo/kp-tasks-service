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
        return this.taskService.create(taskDTO);
    }

    @Override
    public Mono<ResponseEntity<TaskDTO>> updateTask(UUID id, TaskDTO taskDTO) {
        return this.taskService.update(id, taskDTO)
                               .map(ResponseEntity::ok)
                               .switchIfEmpty(Mono.just(ResponseEntity.notFound()
                                                                      .build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteTaskById(UUID id) {
        return this.taskService.deleteById(id)
                               .map(taskHasBeenDeleted -> ResponseEntity.status(
                                       Boolean.TRUE.equals(taskHasBeenDeleted) ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND)
                                                                        .build());
    }

}
