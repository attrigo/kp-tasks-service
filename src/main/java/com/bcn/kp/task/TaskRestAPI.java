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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Defines the endpoints to handle tasks related requests and responses.
 * <p>
 * Web layer relies on Spring Webflux to manage Request and Responses in a reactive.
 *
 * @author ttrigo
 * @since 0.1.0
 */
@Tag(name = "Tasks operations", description = "Defines the endpoints to handle tasks related requests")
@RequestMapping({ "/v1/tasks" })
public interface TaskRestAPI {

    /**
     * Get a {@link TaskDTO} by id.
     * <p>
     * Response codes:
     * <ul>
     * <li>200-OK : Task has been found</li>
     * <li>404-NOT_FOUND : Task not found</li>
     * </ul>
     *
     * @param id the id of the task to get.
     * @return a {@link ResponseEntity} wrapping the found {@link TaskDTO} or empty if not found and the appropriate HTTP headers.
     */
    @Operation(summary = "Get a task by id", description = "Returns the task with the given id or empty if not found")
    @ApiResponse(responseCode = "200", description = "Task has been found", content = { @Content(schema = @Schema(implementation = TaskDTO.class)) })
    @ApiResponse(responseCode = "404", description = "Task not found", content = { @Content })
    @GetMapping(value = { "/{id}" }, produces = { "application/json" })
    Mono<ResponseEntity<TaskDTO>> getTaskById(@Parameter(description = "Id of the task to get") @PathVariable("id") UUID id);

    /**
     * Get all tasks.
     * <p>
     * Response codes:
     * <ul>
     * <li>200-OK : All found tasks</li>
     * </ul>
     *
     * @return all found {@link TaskDTO} or empty if there aren't task and the appropriate HTTP headers.
     */
    @Operation(summary = "Get all tasks", description = "Returns all tasks or empty if there aren't tasks")
    @ApiResponse(responseCode = "200", description = "All found tasks", content = { @Content(schema = @Schema(implementation = TaskDTO.class)) })
    @GetMapping(value = { "" }, produces = { "application/json" })
    @ResponseStatus(HttpStatus.OK)
    Flux<TaskDTO> getAllTasks();

    /**
     * Create a {@link TaskDTO}.
     * <p>
     * Response codes:
     * <ul>
     * <li>201-CREATED : Task has been created</li>
     * </ul>
     *
     * @param taskDTO the task to create.
     * @return the created {@link TaskDTO} and the appropriate HTTP headers.
     */
    @Operation(summary = "Create a task", description = "Returns the created task with a new id")
    @ApiResponse(responseCode = "201", description = "Task has been created", content = { @Content(schema = @Schema(implementation = TaskDTO.class)) })
    @PostMapping(value = { "" }, consumes = { "application/json" }, produces = { "application/json" })
    @ResponseStatus(HttpStatus.CREATED)
    Mono<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO);

    /**
     * Updates a {@link TaskDTO}.
     * <p>
     * Response codes:
     * <ul>
     * <li>200-OK : Task has been updated</li>
     * </ul>
     *
     * @param id      the identifier of the task to update.
     * @param taskDTO the new task data.
     * @return a {@link ResponseEntity} wrapping the updated {@link TaskDTO} or empty if not found and the appropriate HTTP headers.
     */
    @Operation(summary = "Update a task", description = "Returns the updated task or empty if not found")
    @ApiResponse(responseCode = "200", description = "Task has been updated", content = { @Content(schema = @Schema(implementation = TaskDTO.class)) })
    @PutMapping(value = { "/{id}" }, consumes = { "application/json" }, produces = { "application/json" })
    Mono<ResponseEntity<TaskDTO>> updateTask(@Parameter(description = "Identifier of the task to update") @PathVariable("id") UUID id,
            @Valid @RequestBody TaskDTO taskDTO);

    /**
     * Delete a {@link TaskDTO} by id.
     * <p>
     * Response codes:
     * <ul>
     * <li>204-NO_CONTENT : Task has been deleted</li>
     * <li>404-NOT_FOUND : Task not found</li>
     * </ul>
     *
     * @param id the id of the task to delete.
     * @return a {@link ResponseEntity} wrapping empty and the appropriate HTTP headers.
     */
    @Operation(summary = "Delete a task by id", description = "Returns the empty")
    @ApiResponse(responseCode = "204", description = "Task has been deleted", content = { @Content })
    @ApiResponse(responseCode = "404", description = "Task not found", content = { @Content })
    @DeleteMapping(value = { "/{id}" }, produces = { "application/json" })
    Mono<ResponseEntity<Void>> deleteTaskById(@Parameter(description = "Id of the task to delete") @PathVariable("id") UUID id);

}
