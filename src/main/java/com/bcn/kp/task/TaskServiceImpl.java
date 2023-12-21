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

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Default implementation of the {@link Task} operations.
 *
 * @author ttrigo
 * @since 0.1.0
 */

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    /**
     * Default constructor.
     * 
     * @param taskMapper     the mapper to between {@link Task} and {@link TaskDTO}, must not be {@literal null}.
     * @param taskRepository the repository to access task data, must not be {@literal null}.
     */
    public TaskServiceImpl(TaskMapper taskMapper, TaskRepository taskRepository) {
        this.taskMapper = taskMapper;
        this.taskRepository = taskRepository;
    }

    @Override
    public Mono<TaskDTO> findById(UUID id) {
        return Mono.just(id)
                   .flatMap(this.taskRepository::findById)
                   .map(this.taskMapper::toTaskDTO);
    }

    @Override
    public Flux<TaskDTO> findAll() {
        return this.taskRepository.findAll()
                                  .map(this.taskMapper::toTaskDTO);
    }

    @Override
    public Mono<TaskDTO> create(TaskDTO taskDTO) {
        return Mono.just(taskDTO)
                   .map(this.taskMapper::toTaskIgnoreId)
                   .flatMap(this.taskRepository::save)
                   .map(this.taskMapper::toTaskDTO);
    }

    @Override
    public Mono<TaskDTO> update(UUID id, TaskDTO taskDTO) {
        return Mono.just(id)
                   .flatMap(this.taskRepository::existsById)
                   .filter(Boolean::booleanValue)
                   .map(exists -> {
                       taskDTO.setId(id);
                       return this.taskMapper.toTask(taskDTO);
                   })
                   .flatMap(this.taskRepository::save)
                   .map(this.taskMapper::toTaskDTO);
    }

    @Override
    public Mono<Boolean> deleteById(UUID id) {
        return Mono.just(id)
                   .flatMap(this.taskRepository::deleteTaskById)
                   .map(deleteCount -> deleteCount > 0L);
    }

}
