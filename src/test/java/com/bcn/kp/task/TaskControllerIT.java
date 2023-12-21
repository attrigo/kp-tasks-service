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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import com.bcn.kp.KpTasksServiceApplication;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = KpTasksServiceApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class TaskControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private WebTestClient webTestClient;

    private UUID fakeTaskId;

    private LocalDateTime fakeTaskStartDate;

    @BeforeEach
    void beforeEach() {
        taskRepository.deleteAll()
                      .block();

        this.fakeTaskId = UUID.randomUUID();
        this.fakeTaskStartDate = LocalDateTime.now()
                                              .truncatedTo(ChronoUnit.MILLIS);
    }

    // GetTaskById
    @Test
    @DisplayName("GIVEN id is empty WHEN get task by id THEN returns HTTP code NOT_FOUND And a body with the problem details")
    void IdIsEmpty_GetTaskById_ReturnsCodeNotFoundAndBodyWithProblemDetails() {
        // When & Then
        webTestClient.get()
                     .uri("/v1/tasks/")
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .exchange()
                     .expectStatus()
                     .isNotFound()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Not Found")
                     .jsonPath("$.status")
                     .isEqualTo("404")
                     .jsonPath("$.detail")
                     .isEqualTo("No static resource v1/tasks.")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks/");
    }

    @Test
    @DisplayName("GIVEN id is not a valid UUID WHEN get task by id THEN returns HTTP code BAD_REQUEST And a body with the problem details")
    void IdIsNotUUID_GetTaskById_ReturnsCodeBadRequestAndBodyWithProblemDetails() {
        // When & Then
        var idToFound = 1L;

        webTestClient.get()
                     .uri("/v1/tasks/{id}", idToFound)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Bad Request")
                     .jsonPath("$.status")
                     .isEqualTo("400")
                     .jsonPath("$.detail")
                     .isEqualTo("Type mismatch.")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks/" + idToFound);
    }

    @Test
    @DisplayName("GIVEN id does not exists WHEN get task by id THEN returns HTTP code NOT_FOUND And an empty body")
    void IdNotExists_GetTaskById_ReturnsCodeNotFoundAndEmptyBody() {
        // When & Then
        var idToFound = fakeTaskId;

        webTestClient.get()
                     .uri("/v1/tasks/{id}", idToFound)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .exchange()
                     .expectStatus()
                     .isNotFound()
                     .expectBody()
                     .isEmpty();
    }

    @Test
    @DisplayName("GIVEN id exists WHEN get task by id THEN gets the task by given id And returns HTTP code NOT_FOUND And a body with the task found")
    void IdExists_GetTaskById_GetsTheTaskByIdAndReturnsCodeOKAndBodyWithTheTaskFound() {
        // Given
        var dummyTaskToBeFound = new Task(null, "IT Title", "IT Description", fakeTaskStartDate);

        var dummyTask = taskRepository.save(dummyTaskToBeFound)
                                      .block();

        Assertions.assertNotNull(dummyTask);

        // When & Then
        var idToFound = dummyTask.id();

        var expectedTask = TaskDTO.builder()
                                  .id(dummyTask.id())
                                  .title("IT Title")
                                  .description("IT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();

        webTestClient.get()
                     .uri("/v1/tasks/{id}", idToFound)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_JSON)
                     .expectBody(TaskDTO.class)
                     .isEqualTo(expectedTask);
    }

    // GetAllTasks
    @Test
    @DisplayName("GIVEN there are not tasks WHEN get all tasks THEN gets all tasks returns HTTP code OK And an empty body")
    void ThereAreNotTasks_GetAllTasks_GetsAllTasksAndReturnsCodeOKAndEmptyBody() {
        // When & Then
        webTestClient.get()
                     .uri("/v1/tasks")
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(TaskDTO.class)
                     .hasSize(0);
    }

    @Test
    @DisplayName("GIVEN there are tasks WHEN get all tasks THEN gets all tasks And returns HTTP code OK And a body with all task found")
    void ThereAreTasks_GetAllTasks_GetsAllTasksReturnsCodeOKAndTheBodyWithTheTasksFound() {
        // Given
        var dummyTask1ToBeFound = new Task(null, "IT Title 1", "IT Description 1", fakeTaskStartDate);
        var dummyTask2ToBeFound = new Task(null, "IT Title 2", "IT Description 2", fakeTaskStartDate);
        var dummyTask3ToBeFound = new Task(null, "IT Title 3", "IT Description 3", fakeTaskStartDate);
        var dummyTasksToBeFound = Flux.just(dummyTask1ToBeFound, dummyTask2ToBeFound, dummyTask3ToBeFound);

        var dummyTaskIds = taskRepository.saveAll(dummyTasksToBeFound)
                                         .map(Task::id)
                                         .collectList()
                                         .block();

        Assertions.assertNotNull(dummyTaskIds);
        Assertions.assertEquals(3L, dummyTaskIds.size());

        // When & Then
        var expectedTask1 = TaskDTO.builder()
                                   .id(dummyTaskIds.get(0))
                                   .title("IT Title 1")
                                   .description("IT Description 1")
                                   .startDateTime(fakeTaskStartDate)
                                   .build();
        var expectedTask2 = TaskDTO.builder()
                                   .id(dummyTaskIds.get(1))
                                   .title("IT Title 2")
                                   .description("IT Description 2")
                                   .startDateTime(fakeTaskStartDate)
                                   .build();
        var expectedTask3 = TaskDTO.builder()
                                   .id(dummyTaskIds.get(2))
                                   .title("IT Title 3")
                                   .description("IT Description 3")
                                   .startDateTime(fakeTaskStartDate)
                                   .build();
        var expectedTasks = Arrays.asList(expectedTask1, expectedTask2, expectedTask3);

        webTestClient.get()
                     .uri("/v1/tasks")
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_JSON)
                     .expectBodyList(TaskDTO.class)
                     .hasSize(3)
                     .isEqualTo(expectedTasks);
    }

    // CreateTask
    @Test
    @DisplayName("GIVEN task is not a valid Json WHEN create a task THEN does not create the task And returns HTTP code Unsupported Media Type And a body with the problem details")
    void TaskIsNotJson_CreateTask_DoesNotCreateTheTaskAndReturnsCodeUnsupportedMediaTypeAndBodyWithProblemDetails() {
        // When & Then
        var taskToCreate = "";

        webTestClient.post()
                     .uri("/v1/tasks")
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                     .bodyValue(taskToCreate)
                     .exchange()
                     .expectStatus()
                     .is4xxClientError()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Unsupported Media Type")
                     .jsonPath("$.status")
                     .isEqualTo("415")
                     .jsonPath("$.detail")
                     .isEqualTo("Content-Type 'text/plain' is not supported.")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks");

        StepVerifier.create(taskRepository.findAll())
                    .expectNextCount(0)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task is not present WHEN create a task THEN does not create the task And returns HTTP code BAD_REQUEST And a body with the problem details")
    void TaskIsNotPresent_CreateTask_DoesNotCreateTheTaskAndReturnsCodeBadRequestAndBodyWithProblemDetails() {
        // When & Then
        var taskToCreate = "";

        webTestClient.post()
                     .uri("/v1/tasks")
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .bodyValue(taskToCreate)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Bad Request")
                     .jsonPath("$.status")
                     .isEqualTo("400")
                     .jsonPath("$.detail")
                     .isEqualTo("Invalid request content")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks");

        StepVerifier.create(taskRepository.findAll())
                    .expectNextCount(0)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task has not mandatory fields WHEN create a task THEN does not create the task And returns HTTP code BAD_REQUEST And a body with the problem details")
    void TaskHasNotMandatoryFields_CreateTask_DoesNotCreateTheTaskAndReturnsCodeBadRequestAndBodyWithProblemDetails() {
        // When & Then
        var taskToCreate = """
                {
                "description": "IT Description",
                "startDateTime": "2011-11-11T11:11:11.111Z"
                }
                """;

        webTestClient.post()
                     .uri("/v1/tasks")
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .bodyValue(taskToCreate)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Bad Request")
                     .jsonPath("$.status")
                     .isEqualTo("400")
                     .jsonPath("$.detail")
                     .value(value -> Assertions.assertTrue(value.toString()
                                                                .contains("The title of the task is mandatory")))
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks");

        StepVerifier.create(taskRepository.findAll())
                    .expectNextCount(0)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task mandatory fields are empty WHEN create a task THEN does not create the task And returns HTTP code BAD_REQUEST And a body with the problem details")
    void TaskMandatoryFieldsAreEmpty_CreateTask_DoesNotCreateTheTaskAndReturnsCodeBadRequestAndBodyWithProblemDetails() {
        // When & Then
        var taskToCreate = TaskDTO.builder()
                                  .description("IT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();

        webTestClient.post()
                     .uri("/v1/tasks")
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .body(Mono.just(taskToCreate), TaskDTO.class)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Bad Request")
                     .jsonPath("$.status")
                     .isEqualTo("400")
                     .jsonPath("$.detail")
                     .value(value -> Assertions.assertTrue(value.toString()
                                                                .contains("The title of the task is mandatory")))
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks");

        StepVerifier.create(taskRepository.findAll())
                    .expectNextCount(0)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task start date has invalid format WHEN create a task THEN does not create the task And returns HTTP code BAD_REQUEST And a body containing the problem details")
    void TasStartDateHasInvalidFormat_CreateTask_DoesNotCreateTheTaskAndReturnsCodeBadRequestAndBodyWithProblemDetails() {
        // When & Then
        var taskToCreate = """
                {
                "title": "IT Title"
                "description": "IT Description",
                "startDateTime": "2011-11-11T11:11:11"
                }
                """;

        webTestClient.post()
                     .uri("/v1/tasks")
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .body(Mono.just(taskToCreate), TaskDTO.class)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Bad Request")
                     .jsonPath("$.status")
                     .isEqualTo("400")
                     .jsonPath("detail")
                     .isEqualTo("Failed to read HTTP message")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks");

        StepVerifier.create(taskRepository.findAll())
                    .expectNextCount(0)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task has id WHEN create a task THEN creates the task ignoring the given task id And returns HTTP code CREATED And a body with the task created")
    void TaskHasId_CreateTask_CreatesTheTaskAndReturnsCodeCreatedAndBodyWithTheTaskCreated() {
        var anotherFakeTaskId = UUID.randomUUID();

        // When & Then
        var taskToCreate = TaskDTO.builder()
                                  .id(anotherFakeTaskId)
                                  .title("IT Title")
                                  .description("IT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();

        var result = webTestClient.post()
                                  .uri("/v1/tasks")
                                  .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                                  .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                  .body(Mono.just(taskToCreate), TaskDTO.class)
                                  .exchange()
                                  .expectStatus()
                                  .isCreated()
                                  .expectHeader()
                                  .contentType(MediaType.APPLICATION_JSON)
                                  .expectBody(TaskDTO.class)
                                  .value(task -> assertThat(task.getId(), allOf(not(anotherFakeTaskId), notNullValue())))
                                  .value(task -> assertThat(task.getTitle(), equalTo("IT Title")))
                                  .value(task -> assertThat(task.getDescription(), equalTo("IT Description")))
                                  .value(task -> assertThat(task.getStartDateTime(), equalTo(fakeTaskStartDate)))
                                  .returnResult()
                                  .getResponseBody();

        Assertions.assertNotNull(result);

        var expectedRepositoryTask = new Task(result.getId(), "IT Title", "IT Description", fakeTaskStartDate);
        StepVerifier.create(taskRepository.findById(result.getId()))
                    .expectNext(expectedRepositoryTask)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task fields are valid WHEN create a task THEN creates the task And returns HTTP code CREATED And a body with the task created")
    void TaskFieldsAreValid_CreateTask_CreatesTheTaskAndReturnsCodeCreatedAndBodyWithTheTaskCreated() {
        // When & Then
        var taskToCreate = TaskDTO.builder()
                                  .title("IT Title")
                                  .description("IT Description")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();

        var result = webTestClient.post()
                                  .uri("/v1/tasks")
                                  .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                                  .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                  .body(Mono.just(taskToCreate), TaskDTO.class)
                                  .exchange()
                                  .expectStatus()
                                  .isCreated()
                                  .expectHeader()
                                  .contentType(MediaType.APPLICATION_JSON)
                                  .expectBody(TaskDTO.class)
                                  .value(task -> assertThat(task.getId(), notNullValue()))
                                  .value(task -> assertThat(task.getTitle(), equalTo("IT Title")))
                                  .value(task -> assertThat(task.getDescription(), equalTo("IT Description")))
                                  .value(task -> assertThat(task.getStartDateTime(), equalTo(fakeTaskStartDate)))
                                  .returnResult()
                                  .getResponseBody();

        Assertions.assertNotNull(result);

        var expectedRepositoryTask = new Task(result.getId(), "IT Title", "IT Description", fakeTaskStartDate);
        StepVerifier.create(taskRepository.findById(result.getId()))
                    .expectNext(expectedRepositoryTask)
                    .verifyComplete();
    }

    // UpdateTask
    @Test
    @DisplayName("GIVEN id is empty WHEN update task THEN returns HTTP code NOT_FOUND And a body containing the problem details")
    void IdIsEmpty_UpdateTask_ReturnsCodeNotFoundAndBodyWithProblemDetails() {
        // When & Then
        var taskToUpdate = TaskDTO.builder()
                                  .title("IT Title 2")
                                  .description("IT Description 2")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();

        webTestClient.put()
                     .uri("/v1/tasks/")
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .body(Mono.just(taskToUpdate), TaskDTO.class)
                     .exchange()
                     .expectStatus()
                     .isNotFound()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Not Found")
                     .jsonPath("$.status")
                     .isEqualTo("404")
                     .jsonPath("$.detail")
                     .isEqualTo("No static resource v1/tasks.")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks/");
    }

    @Test
    @DisplayName("GIVEN id is not a valid UUID WHEN update task THEN returns HTTP code BAD_REQUEST And a body with the problem details")
    void IdIsNotUUID_UpdateTask_ReturnsCodeBadRequestAndBodyWithProblemDetails() {
        // When & Then
        var idToUpdate = 1L;
        var taskToUpdate = TaskDTO.builder()
                                  .title("IT Title 2")
                                  .description("IT Description 2")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();

        webTestClient.put()
                     .uri("/v1/tasks/{id}", idToUpdate)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .body(Mono.just(taskToUpdate), TaskDTO.class)
                     .exchange()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Bad Request")
                     .jsonPath("$.status")
                     .isEqualTo("400")
                     .jsonPath("$.detail")
                     .isEqualTo("Type mismatch.")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks/" + idToUpdate);
    }

    @Test
    @DisplayName("GIVEN id does not exists WHEN update task THEN returns HTTP code NOT_FOUND And an empty body")
    void IdNotExists_UpdateTask_ReturnsCodeNotFoundAndEmptyBody() {
        // When & Then
        var idToUpdate = fakeTaskId;
        var taskToUpdate = TaskDTO.builder()
                                  .title("IT Title 2")
                                  .description("IT Description 2")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();

        webTestClient.put()
                     .uri("/v1/tasks/{id}", idToUpdate)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .body(Mono.just(taskToUpdate), TaskDTO.class)
                     .exchange()
                     .expectStatus()
                     .isNotFound()
                     .expectBody()
                     .isEmpty();
    }

    @Test
    @DisplayName("GIVEN task is not a valid Json WHEN update a task THEN does not update the task And returns HTTP code Unsupported Media Type And a body containing the problem details")
    void TaskIsNotJson_UpdateTask_DoesNotUpdateTheTaskAndReturnsCodeUnsupportedMediaTypeAndBodyWithProblemDetails() {
        // Given
        var dummyTaskToBeUpdate = new Task(null, "IT Title", "IT Description", fakeTaskStartDate);

        var dummyTask = taskRepository.save(dummyTaskToBeUpdate)
                                      .block();

        Assertions.assertNotNull(dummyTask);

        // When & Then
        var idToUpdate = dummyTask.id();
        var taskToUpdate = "";

        webTestClient.put()
                     .uri("/v1/tasks/{id}", idToUpdate)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                     .bodyValue(taskToUpdate)
                     .exchange()
                     .expectStatus()
                     .is4xxClientError()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Unsupported Media Type")
                     .jsonPath("$.status")
                     .isEqualTo("415")
                     .jsonPath("$.detail")
                     .isEqualTo("Content-Type 'text/plain' is not supported.")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks/" + idToUpdate);

        StepVerifier.create(taskRepository.findById(dummyTask.id()))
                    .expectNext(dummyTask)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task is not present WHEN update a task THEN does not update the task And returns HTTP code BAD_REQUEST And a body containing the problem details")
    void TaskIsNotPresent_UpdateTask_DoesNotUpdateTheTaskAndReturnsCodeBadRequestAndBodyWithProblemDetails() {
        // Given
        var dummyTaskToBeUpdate = new Task(null, "IT Title", "IT Description", fakeTaskStartDate);

        var dummyTask = taskRepository.save(dummyTaskToBeUpdate)
                                      .block();

        Assertions.assertNotNull(dummyTask);

        // When & Then
        var idToUpdate = dummyTask.id();
        var taskToUpdate = "";

        webTestClient.put()
                     .uri("/v1/tasks/{id}", idToUpdate)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .bodyValue(taskToUpdate)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Bad Request")
                     .jsonPath("$.status")
                     .isEqualTo("400")
                     .jsonPath("$.detail")
                     .isEqualTo("Invalid request content")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks/" + idToUpdate);

        StepVerifier.create(taskRepository.findById(dummyTask.id()))
                    .expectNext(dummyTask)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task has not mandatory fields WHEN update a task THEN does not update the task And returns HTTP code BAD_REQUEST And a body containing the problem details")
    void TaskHasNotMandatoryFields_UpdateTask_DoesNotUpdateTheTaskAndReturnsCodeBadRequestAndBodyWithProblemDetails() {
        // Given
        var dummyTaskToBeUpdate = new Task(null, "IT Title", "IT Description", fakeTaskStartDate);

        var dummyTask = taskRepository.save(dummyTaskToBeUpdate)
                                      .block();

        Assertions.assertNotNull(dummyTask);

        // When & Then
        var idToUpdate = dummyTask.id();
        var taskToUpdate = """
                {
                "description": "IT Description 2",
                "startDateTime": "2011-11-11T11:11:11.111Z"
                }
                """;

        webTestClient.put()
                     .uri("/v1/tasks/{id}", idToUpdate)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .bodyValue(taskToUpdate)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Bad Request")
                     .jsonPath("$.status")
                     .isEqualTo("400")
                     .jsonPath("$.detail")
                     .value(value -> Assertions.assertTrue(value.toString()
                                                                .contains("The title of the task is mandatory")))
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks/" + idToUpdate);

        StepVerifier.create(taskRepository.findById(dummyTask.id()))
                    .expectNext(dummyTask)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task mandatory fields are empty WHEN update a task THEN does not update the task And returns HTTP code BAD_REQUEST And a body containing the problem details")
    void TaskMandatoryFieldsAreEmpty_UpdateTask_DoesNotUpdateTheTaskAndReturnsCodeBadRequestAndBodyWithProblemDetails() {
        // Given
        var dummyTaskToBeUpdate = new Task(null, "IT Title", "IT Description", fakeTaskStartDate);

        var dummyTask = taskRepository.save(dummyTaskToBeUpdate)
                                      .block();

        Assertions.assertNotNull(dummyTask);

        // When & Then
        var idToUpdate = dummyTask.id();
        var taskToUpdate = TaskDTO.builder()
                                  .title("")
                                  .description("IT Description 2")
                                  .startDateTime(fakeTaskStartDate)
                                  .build();

        webTestClient.put()
                     .uri("/v1/tasks/{id}", idToUpdate)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .body(Mono.just(taskToUpdate), TaskDTO.class)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Bad Request")
                     .jsonPath("$.status")
                     .isEqualTo("400")
                     .jsonPath("$.detail")
                     .value(value -> Assertions.assertTrue(value.toString()
                                                                .contains("The title of the task is mandatory")))
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks/" + idToUpdate);

        StepVerifier.create(taskRepository.findById(dummyTask.id()))
                    .expectNext(dummyTask)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task start date has invalid format WHEN update a task THEN does not update the task And returns HTTP code BAD_REQUEST And a body containing the problem details")
    void TasStartDateHasInvalidFormat_UpdateTask_DoesNotUpdateTheTaskAndReturnsCodeBadRequestAndBodyWithProblemDetails() {
        // Given
        var dummyTaskToBeUpdate = new Task(null, "IT Title", "IT Description", fakeTaskStartDate);

        var dummyTask = taskRepository.save(dummyTaskToBeUpdate)
                                      .block();

        Assertions.assertNotNull(dummyTask);

        // When & Then
        var idToUpdate = dummyTask.id();
        var taskToUpdate = """
                {
                "title": "IT Title 2"
                "description": "IT Description 2",
                "startDateTime": "2011-11-11T11:11:11"
                }
                """;

        webTestClient.put()
                     .uri("/v1/tasks/{id}", idToUpdate)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .body(Mono.just(taskToUpdate), TaskDTO.class)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Bad Request")
                     .jsonPath("$.status")
                     .isEqualTo("400")
                     .jsonPath("detail")
                     .isEqualTo("Failed to read HTTP message")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks/" + idToUpdate);

        StepVerifier.create(taskRepository.findById(dummyTask.id()))
                    .expectNext(dummyTask)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task has id WHEN update a task THEN updates all fields of the task except the id And returns HTTP code OK And a body containing the task updated")
    void TaskHasId_UpdateTask_UpdatesTheTaskAndReturnsCodeOkAndBodyWithTheTaskUpdated() {
        // Given
        var dummyTaskToBeUpdate = new Task(null, "IT Title", "IT Description", fakeTaskStartDate);

        var dummyTask = taskRepository.save(dummyTaskToBeUpdate)
                                      .block();

        Assertions.assertNotNull(dummyTask);

        // When & Then
        var newStartDate = LocalDateTime.now()
                                        .truncatedTo(ChronoUnit.MILLIS);
        var idToUpdate = dummyTask.id();
        var taskToUpdate = TaskDTO.builder()
                                  .id(UUID.randomUUID())
                                  .title("IT Title 2")
                                  .description("IT Description 2")
                                  .startDateTime(newStartDate)
                                  .build();

        var expectedTask = TaskDTO.builder()
                                  .id(idToUpdate)
                                  .title("IT Title 2")
                                  .description("IT Description 2")
                                  .startDateTime(newStartDate)
                                  .build();

        webTestClient.put()
                     .uri("/v1/tasks/{id}", idToUpdate)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .body(Mono.just(taskToUpdate), TaskDTO.class)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_JSON)
                     .expectBody(TaskDTO.class)
                     .isEqualTo(expectedTask);

        var expectedRepositoryTask = new Task(dummyTask.id(), "IT Title 2", "IT Description 2", newStartDate);
        StepVerifier.create(taskRepository.findById(dummyTask.id()))
                    .expectNext(expectedRepositoryTask)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN task is valid WHEN update a task THEN updates the tasks And returns HTTP code OK And a body containing the task updated")
    void TaskIsValid_UpdateTask_UpdatesTheTaskReturnsCodeOkAndBodyWithTheTaskUpdated() {
        // Given
        var dummyTaskToBeUpdate = new Task(null, "IT Title", "IT Description", fakeTaskStartDate);

        var dummyTask = taskRepository.save(dummyTaskToBeUpdate)
                                      .block();

        Assertions.assertNotNull(dummyTask);

        // When & Then
        var newStartDate = LocalDateTime.now()
                                        .truncatedTo(ChronoUnit.MILLIS);
        var idToUpdate = dummyTask.id();
        var taskToUpdate = TaskDTO.builder()
                                  .title("IT Title 2")
                                  .description("IT Description 2")
                                  .startDateTime(newStartDate)
                                  .build();

        var expectedTask = TaskDTO.builder()
                                  .id(idToUpdate)
                                  .title("IT Title 2")
                                  .description("IT Description 2")
                                  .startDateTime(newStartDate)
                                  .build();

        webTestClient.put()
                     .uri("/v1/tasks/{id}", idToUpdate)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .body(Mono.just(taskToUpdate), TaskDTO.class)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_JSON)
                     .expectBody(TaskDTO.class)
                     .isEqualTo(expectedTask);

        var expectedRepositoryTask = new Task(dummyTask.id(), "IT Title 2", "IT Description 2", newStartDate);
        StepVerifier.create(taskRepository.findById(dummyTask.id()))
                    .expectNext(expectedRepositoryTask)
                    .verifyComplete();
    }

    // DeleteTaskById
    @Test
    @DisplayName("GIVEN id is empty WHEN delete task by id THEN returns HTTP code NOT_FOUND And a body containing the problem details")
    void IdIsEmpty_DeleteTaskById_ReturnsCodeNotFoundAndBodyWithProblemDetails() {
        // When & Then
        webTestClient.delete()
                     .uri("/v1/tasks/")
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .exchange()
                     .expectStatus()
                     .isNotFound()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Not Found")
                     .jsonPath("$.status")
                     .isEqualTo("404")
                     .jsonPath("$.detail")
                     .isEqualTo("No static resource v1/tasks.")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks/");
    }

    @Test
    @DisplayName("GIVEN id is not a valid UUID WHEN delete task by id THEN returns HTTP code BAD_REQUEST And a body with the problem details")
    void IdIsNotUUID_DeleteTaskById_ReturnsCodeBadRequestAndBodyWithProblemDetails() {
        // When & Then
        var idToDelete = 1L;

        webTestClient.delete()
                     .uri("/v1/tasks/{id}", idToDelete)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectHeader()
                     .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .jsonPath("$.type")
                     .isEqualTo("about:blank")
                     .jsonPath("$.title")
                     .isEqualTo("Bad Request")
                     .jsonPath("$.status")
                     .isEqualTo("400")
                     .jsonPath("$.detail")
                     .isEqualTo("Type mismatch.")
                     .jsonPath("$.instance")
                     .isEqualTo("/kp-tasks-service/v1/tasks/" + idToDelete);
    }

    @Test
    @DisplayName("GIVEN id does not exists WHEN delete a task by id THEN does not delete any task And returns HTTP code NOT_FOUND And an empty body")
    void IdNotExists_DeleteTaskById_DoesNotDeleteTheTaskAndReturnsCodeNotFoundAndEmptyBody() {
        // Given
        var dummyTaskToBeDeleted = new Task(null, "IT Title", "IT Description", fakeTaskStartDate);

        var dummyTaskId = taskRepository.save(dummyTaskToBeDeleted)
                                        .map(Task::id)
                                        .block();

        Assertions.assertNotNull(dummyTaskId);

        // When & Then
        var idToDelete = UUID.randomUUID();

        webTestClient.delete()
                     .uri("/v1/tasks/{id}", idToDelete)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .exchange()
                     .expectStatus()
                     .isNotFound()
                     .expectBody()
                     .isEmpty();

        StepVerifier.create(taskRepository.findById(dummyTaskId))
                    .expectNextCount(1)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN id exists WHEN delete a task by id THEN deletes the task with the given id And returns HTTP code NO_CONTENT And an empty body")
    void IdExists_DeleteTaskById_ReturnsCodeNoContentAndEmptyBody() {
        // Given
        var dummyTaskToBeDeleted = new Task(null, "IT Title", "IT Description", fakeTaskStartDate);

        var dummyTask = taskRepository.save(dummyTaskToBeDeleted)
                                      .block();

        Assertions.assertNotNull(dummyTask);

        // When & Then
        var idToDelete = dummyTask.id();

        webTestClient.delete()
                     .uri("/v1/tasks/{id}", idToDelete)
                     .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                     .exchange()
                     .expectStatus()
                     .isNoContent()
                     .expectBody()
                     .isEmpty();

        StepVerifier.create(taskRepository.findById(dummyTask.id()))
                    .expectNextCount(0)
                    .verifyComplete();
    }

}
