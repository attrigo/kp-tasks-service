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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class TaskControllerTests {

    @Mock
    private TaskService taskServiceMock;

    @InjectMocks
    private TaskRestController taskController;

    private UUID fakeTaskId;

    private LocalDateTime fakeTaskStartDate;

    @BeforeEach
    void beforeEach() {
        this.fakeTaskId = UUID.randomUUID();
        this.fakeTaskStartDate = LocalDateTime.now();
    }

    // getTaskById
    @Test
    @DisplayName("GIVEN id does not exists WHEN get a task by id THEN returns HTTP code NOT_FOUND And an empty body")
    void IdNotExists_GetTaskById_ReturnsCodeNotFoundAndEmptyBody() {
        // Given
        given(taskServiceMock.findById(any(UUID.class))).willReturn(Mono.empty());

        // When
        var idToFind = fakeTaskId;
        var result = taskController.getTaskById(idToFind);

        // Then
        ResponseEntity<TaskDTO> expected = ResponseEntity.notFound()
                                                         .build();
        StepVerifier.create(result)
                    .expectNext(expected)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN id exists WHEN get a task by id THEN returns HTTP code OK And a body with the task found")
    void IdExists_GetTaskById_ReturnsCodeOkAndTheTaskFound() {
        // Given
        var fakeTaskFound = TaskDTO.builder()
                                   .id(fakeTaskId)
                                   .title("UT Title")
                                   .description("UT Description")
                                   .startDateTime(fakeTaskStartDate)
                                   .build();
        given(taskServiceMock.findById(any(UUID.class))).willReturn(Mono.just(fakeTaskFound));

        // When
        var idToFind = fakeTaskId;
        var result = taskController.getTaskById(idToFind);

        // Then
        var expectedTask = TaskDTO.builder()
                                  .id(fakeTaskId)
                                  .title("UT Title")
                                  .description("UT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        ResponseEntity<TaskDTO> expectedResponseEntity = ResponseEntity.ok(expectedTask);
        StepVerifier.create(result)
                    .expectNext(expectedResponseEntity)
                    .verifyComplete();
    }

    // GetAllTasks
    @Test
    @DisplayName("GIVEN there are not tasks WHEN get all tasks THEN returns an empty body")
    void ThereAreNotTasks_GetAllTasks_ReturnsEmptyBody() {
        // Given
        given(taskServiceMock.findAll()).willReturn(Flux.empty());

        // When
        var result = taskController.getAllTasks();

        // Then
        StepVerifier.create(result)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN there are tasks WHEN get all tasks THEN returns a body with all tasks found")
    void ThereAreTasks_GetAllTasks_ReturnsCodeOkAndAllTasksFound() {
        var fakeTask1Id = UUID.randomUUID();
        var fakeTask2Id = UUID.randomUUID();
        var fakeTask3Id = UUID.randomUUID();

        // Given
        var fakeTask1ToFound = TaskDTO.builder()
                                      .id(fakeTask1Id)
                                      .title("UT Title 1")
                                      .description("UT Description 1")
                                      .startDateTime(fakeTaskStartDate)
                                      .build();
        var fakeTask2ToFound = TaskDTO.builder()
                                      .id(fakeTask2Id)
                                      .title("UT Title 2")
                                      .description("UT Description 2")
                                      .startDateTime(fakeTaskStartDate)
                                      .build();
        var fakeTask3ToFound = TaskDTO.builder()
                                      .id(fakeTask3Id)
                                      .title("UT Title 3")
                                      .description("UT Description 3")
                                      .startDateTime(fakeTaskStartDate)
                                      .build();
        given(taskServiceMock.findAll()).willReturn(Flux.just(fakeTask1ToFound, fakeTask2ToFound, fakeTask3ToFound));

        // When
        var result = taskController.getAllTasks();

        // Then
        var expectedTask1 = TaskDTO.builder()
                                   .id(fakeTask1Id)
                                   .title("UT Title 1")
                                   .description("UT Description 1")
                                   .startDateTime(fakeTaskStartDate)
                                   .build();
        var expectedTask2 = TaskDTO.builder()
                                   .id(fakeTask2Id)
                                   .title("UT Title 2")
                                   .description("UT Description 2")
                                   .startDateTime(fakeTaskStartDate)
                                   .build();
        var expectedTask3 = TaskDTO.builder()
                                   .id(fakeTask3Id)
                                   .title("UT Title 3")
                                   .description("UT Description 3")
                                   .startDateTime(fakeTaskStartDate)
                                   .build();
        StepVerifier.create(result)
                    .expectNext(expectedTask1)
                    .expectNext(expectedTask2)
                    .expectNext(expectedTask3)
                    .verifyComplete();
    }

    // CreateTask
    @Test
    @DisplayName("GIVEN task is valid WHEN create a task THEN returns HTTP code CREATED And a body with the task created")
    void TaskIsValid_CreateTask_ReturnsCodeCreatedAndTheTaskCreated() {
        // Given
        var fakeTaskCreated = TaskDTO.builder()
                                     .id(fakeTaskId)
                                     .title("UT Title")
                                     .description("UT Description")
                                     .startDateTime(fakeTaskStartDate)
                                     .build();
        given(taskServiceMock.create(any(TaskDTO.class))).willReturn(Mono.just(fakeTaskCreated));

        // When
        var taskToCreate = TaskDTO.builder()
                                  .title("UT Title")
                                  .description("UT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        var result = taskController.createTask(taskToCreate);

        // Then
        var expectedTask = TaskDTO.builder()
                                  .id(fakeTaskId)
                                  .title("UT Title")
                                  .description("UT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        StepVerifier.create(result)
                    .expectNext(expectedTask)
                    .verifyComplete();
    }

    // UpdateTask
    @Test
    @DisplayName("GIVEN id does not exists WHEN update a task THEN returns HTTP code NOT_FOUND And an empty body")
    void IdNotExists_UpdateTask_ReturnsCodeNotFoundAndEmptyBody() {
        // Given
        given(taskServiceMock.update(any(UUID.class), any(TaskDTO.class))).willReturn(Mono.empty());

        // When
        var idToUpdate = fakeTaskId;
        var taskToUpdate = TaskDTO.builder()
                                  .title("UT Title")
                                  .description("UT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        var result = taskController.updateTask(idToUpdate, taskToUpdate);

        // Then
        ResponseEntity<TaskDTO> expected = ResponseEntity.notFound()
                                                         .build();
        StepVerifier.create(result)
                    .expectNext(expected)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN id exists WHEN update a task THEN returns HTTP code OK And a body with the task updated")
    void IdExists_UpdateTask_ReturnsCodeOkAndTheTaskUpdated() {
        // Given
        var fakeTaskUpdated = TaskDTO.builder()
                                     .id(fakeTaskId)
                                     .title("UT Title")
                                     .description("UT Description")
                                     .startDateTime(fakeTaskStartDate)
                                     .build();
        given(taskServiceMock.update(any(UUID.class), any(TaskDTO.class))).willReturn(Mono.just(fakeTaskUpdated));

        // When
        var idToUpdate = fakeTaskId;
        var taskToUpdate = TaskDTO.builder()
                                  .title("UT Title")
                                  .description("UT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        var result = taskController.updateTask(idToUpdate, taskToUpdate);

        // Then
        var expectedTask = TaskDTO.builder()
                                  .id(fakeTaskId)
                                  .title("UT Title")
                                  .description("UT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        ResponseEntity<TaskDTO> expectedResponseEntity = ResponseEntity.ok(expectedTask);
        StepVerifier.create(result)
                    .expectNext(expectedResponseEntity)
                    .verifyComplete();
    }

    // DeleteTaskById
    @Test
    @DisplayName("GIVEN id does not exists WHEN delete a task by id THEN returns HTTP code NOT_FOUND And an empty body")
    void IdNotExists_DeleteTaskById_ReturnsCodeNotFoundAndEmptyBody() {
        // Given
        given(taskServiceMock.deleteById(any(UUID.class))).willReturn(Mono.just(false));

        // When
        var idToDelete = fakeTaskId;
        var result = taskController.deleteTaskById(idToDelete);

        // Then
        ResponseEntity<Void> expected = ResponseEntity.notFound()
                                                      .build();
        StepVerifier.create(result)
                    .expectNext(expected)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN id exists WHEN delete a task by id THEN returns HTTP code NO_CONTENT And an empty body")
    void IdExists_DeleteTaskById_ReturnsCodeNoContentAndEmptyBody() {
        // Given
        given(taskServiceMock.deleteById(any(UUID.class))).willReturn(Mono.just(true));

        // When
        var idToDelete = fakeTaskId;
        var result = taskController.deleteTaskById(idToDelete);

        // Then
        ResponseEntity<Void> expected = ResponseEntity.noContent()
                                                      .build();
        StepVerifier.create(result)
                    .expectNext(expected)
                    .verifyComplete();
    }

}
