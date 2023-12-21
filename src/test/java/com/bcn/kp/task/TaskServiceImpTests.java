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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class TaskServiceImpTests {

    @Mock
    private TaskRepository taskRepositoryMock;

    @Spy
    private TaskMapperImpl taskMapperSpy;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UUID fakeTaskId;

    private LocalDateTime fakeTaskStartDate;

    @BeforeEach
    void beforeEach() {
        given(taskMapperSpy.toTaskDTO(any(Task.class))).willCallRealMethod();
        given(taskMapperSpy.toTask(any(TaskDTO.class))).willCallRealMethod();
        given(taskMapperSpy.toTaskIgnoreId(any(TaskDTO.class))).willCallRealMethod();

        this.fakeTaskId = UUID.randomUUID();
        this.fakeTaskStartDate = LocalDateTime.now();
    }

    // findById
    @Test
    @DisplayName("GIVEN id does not exists WHEN find a task by id THEN Finds the task by the given id And returns empty")
    void IdNotExists_FindTaskById_FindsTaskByGivenIdAndReturnsEmpty() {
        // Given
        given(taskRepositoryMock.findById(any(UUID.class))).willReturn(Mono.empty());

        // When
        var idToFind = fakeTaskId;
        var result = taskService.findById(idToFind);

        // Then
        StepVerifier.create(result)
                    .verifyComplete();

        then(taskRepositoryMock).should(times(1))
                                .findById(fakeTaskId);
    }

    @Test
    @DisplayName("GIVEN id exists WHEN find a task by id THEN finds the task by the given id And returns the task found")
    void IdExists_FindTaskById_FindsTaskByGivenIdAndReturnsTheTaskFound() {
        // Given
        var fakeTaskFound = new Task(fakeTaskId, "UT Title", "UT Description", fakeTaskStartDate);
        given(taskRepositoryMock.findById(any(UUID.class))).willReturn(Mono.just(fakeTaskFound));

        // When
        var idToFind = fakeTaskId;
        var result = taskService.findById(idToFind);

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

        then(taskRepositoryMock).should(times(1))
                                .findById(fakeTaskId);
    }

    // findAll
    @Test
    @DisplayName("GIVEN there are not tasks WHEN find all tasks THEN finds all tasks And returns empty")
    void ThereAreNotTasks_FindAllTask_FindsAllTasksAndReturnsEmpty() {
        // Given
        given(taskRepositoryMock.findAll()).willReturn(Flux.empty());

        // When
        var result = taskService.findAll();

        // Then
        StepVerifier.create(result)
                    .verifyComplete();

        then(taskRepositoryMock).should(times(1))
                                .findAll();
    }

    @Test
    @DisplayName("GIVEN there are tasks WHEN find all tasks THEN finds all tasks And returns all tasks found")
    void ThereAreTasks_FindAllTask_FindsAllTasksAndReturnsAllTasksFound() {
        var fakeTask1Id = UUID.randomUUID();
        var fakeTask2Id = UUID.randomUUID();
        var fakeTask3Id = UUID.randomUUID();

        // Given
        var fakeTask1Found = new Task(fakeTask1Id, "UT Title 1", "UT Description 1", fakeTaskStartDate);
        var fakeTask2Found = new Task(fakeTask2Id, "UT Title 2", "UT Description 2", fakeTaskStartDate);
        var fakeTask3Found = new Task(fakeTask3Id, "UT Title 3", "UT Description 3", fakeTaskStartDate);
        given(taskRepositoryMock.findAll()).willReturn(Flux.just(fakeTask1Found, fakeTask2Found, fakeTask3Found));

        // When
        var result = taskService.findAll();

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

        then(taskRepositoryMock).should(times(1))
                                .findAll();
    }

    // Create
    @Test
    @DisplayName("GIVEN task id is not null WHEN create a task THEN creates the task ignoring the given task id And returns the task created with a new id")
    void TaskIdIsNotNull_CreateTask_CreatesTheTaskIgnoringTheGivenTaskIdAndReturnsTheTaskCreated() {
        var anotherFakeTaskId = UUID.randomUUID();

        // Given
        var fakeTaskCreated = new Task(anotherFakeTaskId, "UT Title", "UT Description", fakeTaskStartDate);
        given(taskRepositoryMock.save(any(Task.class))).willReturn(Mono.just(fakeTaskCreated));

        // When
        var taskToCreate = TaskDTO.builder()
                                  .id(fakeTaskId)
                                  .title("UT Title")
                                  .description("UT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        var result = taskService.create(taskToCreate);

        // Then
        var expectedTask = TaskDTO.builder()
                                  .id(anotherFakeTaskId)
                                  .title("UT Title")
                                  .description("UT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        StepVerifier.create(result)
                    .expectNext(expectedTask)
                    .verifyComplete();

        ArgumentCaptor<Task> taskArgumentCaptor = ArgumentCaptor.forClass(Task.class);
        then(taskRepositoryMock).should(times(1))
                                .save(taskArgumentCaptor.capture());
        Task taskArgument = taskArgumentCaptor.getValue();
        assertNull(taskArgument.id());
    }

    @Test
    @DisplayName("GIVEN task id is null WHEN create a task THEN creates the task And returns the task created with a new id")
    void TaskIdIsNull_CreateTask_CreatesTheTaskAndReturnsTheTaskCreated() {
        // Given
        var fakeTaskCreated = new Task(fakeTaskId, "UT Title", "UT Description", fakeTaskStartDate);
        given(taskRepositoryMock.save(any(Task.class))).willReturn(Mono.just(fakeTaskCreated));

        // When
        var taskToCreate = TaskDTO.builder()
                                  .id(null)
                                  .title("UT Title")
                                  .description("UT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        var result = taskService.create(taskToCreate);

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

        ArgumentCaptor<Task> taskArgumentCaptor = ArgumentCaptor.forClass(Task.class);
        then(taskRepositoryMock).should(times(1))
                                .save(taskArgumentCaptor.capture());
        Task taskArgument = taskArgumentCaptor.getValue();
        assertNull(taskArgument.id());
    }

    // Update
    @Test
    @DisplayName("GIVEN id does not exists And task id is not null WHEN update a task THEN does not update the task And returns empty")
    void IdNotExistsAndTaskIdIsNotNull_UpdateTask_DoesNotUpdateTheTaskAndReturnsEmpty() {
        // Given
        given(taskRepositoryMock.existsById(any(UUID.class))).willReturn(Mono.just(false));

        // When
        var idToUpdate = fakeTaskId;
        var taskToUpdate = TaskDTO.builder()
                                  .id(UUID.randomUUID())
                                  .title("UT Title")
                                  .description("UT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        var result = taskService.update(idToUpdate, taskToUpdate);

        // Then
        StepVerifier.create(result)
                    .verifyComplete();

        then(taskRepositoryMock).should(never())
                                .save(any(Task.class));
    }

    @Test
    @DisplayName("GIVEN id does not exists And task id is null WHEN update a task THEN does not update the task And returns empty")
    void IdNotExistsAndTaskIdIsNull_UpdateTask_DoesNotUpdateTheTaskAndReturnsEmpty() {
        // Given
        given(taskRepositoryMock.existsById(any(UUID.class))).willReturn(Mono.just(false));

        // When
        var idToUpdate = fakeTaskId;
        var taskToUpdate = TaskDTO.builder()
                                  .id(null)
                                  .title("UT Title")
                                  .description("UT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        var result = taskService.update(idToUpdate, taskToUpdate);

        // Then
        StepVerifier.create(result)
                    .verifyComplete();

        then(taskRepositoryMock).should(never())
                                .save(any(Task.class));
    }

    @Test
    @DisplayName("GIVEN id exists And task id is not null WHEN update a task THEN updates all fields of the task except the id And Returns the task updated with the new values")
    void IdExistsAndTaskIdIsNotNull_UpdateTask_UpdatesAllFieldsExceptTheIdAndReturnsTheTaskUpdated() {
        // Given
        given(taskRepositoryMock.existsById(any(UUID.class))).willReturn(Mono.just(true));

        var fakeTaskUpdated = new Task(fakeTaskId, "UT Title 2", "UT Description 2", fakeTaskStartDate);
        given(taskRepositoryMock.save(any(Task.class))).willReturn(Mono.just(fakeTaskUpdated));

        // When
        var idToUpdate = fakeTaskId;
        var taskToUpdate = TaskDTO.builder()
                                  .id(UUID.randomUUID())
                                  .title("UT Title 2")
                                  .description("UT Description 2")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        var result = taskService.update(idToUpdate, taskToUpdate);

        // Then
        var expectedTask = TaskDTO.builder()
                                  .id(fakeTaskId)
                                  .title("UT Title 2")
                                  .description("UT Description 2")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        StepVerifier.create(result)
                    .expectNext(expectedTask)
                    .verifyComplete();

        ArgumentCaptor<Task> taskArgumentCaptor = ArgumentCaptor.forClass(Task.class);
        then(taskRepositoryMock).should(times(1))
                                .save(taskArgumentCaptor.capture());
        Task taskArgument = taskArgumentCaptor.getValue();
        assertEquals(idToUpdate, taskArgument.id());
    }

    @Test
    @DisplayName("GIVEN id exists And task id is null WHEN update a task THEN updates all fields of the task except the id And Returns the task updated with the new values")
    void IdExistsAndTaskIdIsNull_UpdateTask_UpdatesAllFieldsExceptTheIdAndReturnsTheTaskUpdated() {
        // Given
        given(taskRepositoryMock.existsById(any(UUID.class))).willReturn(Mono.just(true));

        var fakeTaskUpdated = new Task(fakeTaskId, "UT Title 2", "UT Description 2", fakeTaskStartDate);
        given(taskRepositoryMock.save(any(Task.class))).willReturn(Mono.just(fakeTaskUpdated));

        // When
        var idToUpdate = fakeTaskId;
        var taskToUpdate = TaskDTO.builder()
                                  .id(null)
                                  .title("UT Title 2")
                                  .description("UT Description 2")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        var result = taskService.update(idToUpdate, taskToUpdate);

        // Then
        var expectedTask = TaskDTO.builder()
                                  .id(fakeTaskId)
                                  .title("UT Title 2")
                                  .description("UT Description 2")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();
        StepVerifier.create(result)
                    .expectNext(expectedTask)
                    .verifyComplete();

        ArgumentCaptor<Task> taskArgumentCaptor = ArgumentCaptor.forClass(Task.class);
        then(taskRepositoryMock).should(times(1))
                                .save(taskArgumentCaptor.capture());
        Task taskArgument = taskArgumentCaptor.getValue();
        assertEquals(idToUpdate, taskArgument.id());
    }

    // Delete
    @Test
    @DisplayName("GIVEN id does not exists WHEN delete a task by id THEN does not delete any task And returns false")
    void IdNotExists_DeleteTaskById_DoesNotDeleteAnyTaskAndReturnsFalse() {
        // Given
        given(taskRepositoryMock.deleteTaskById(any(UUID.class))).willReturn(Mono.just(0L));

        // When
        var idToDelete = fakeTaskId;
        var result = taskService.deleteById(idToDelete);

        // Then
        StepVerifier.create(result)
                    .expectNext(false)
                    .verifyComplete();

        then(taskRepositoryMock).should(times(1))
                                .deleteTaskById(idToDelete);
    }

    @Test
    @DisplayName("GIVEN id exists WHEN delete a task by id THEN deletes the task with the given id And returns true")
    void IdExists_DeleteTaskById_DeletesTheTaskWithTheGivenIdAndReturnsTrue() {
        // Given
        given(taskRepositoryMock.deleteTaskById(any(UUID.class))).willReturn(Mono.just(1L));

        // When
        var idToDelete = fakeTaskId;
        var result = taskService.deleteById(idToDelete);

        // Then
        StepVerifier.create(result)
                    .expectNext(true)
                    .verifyComplete();

        then(taskRepositoryMock).should(times(1))
                                .deleteTaskById(idToDelete);
    }

}
