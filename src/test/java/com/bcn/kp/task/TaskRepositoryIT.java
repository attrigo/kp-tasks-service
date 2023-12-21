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

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import reactor.test.StepVerifier;

@Testcontainers(disabledWithoutDocker = true)
@DataR2dbcTest
class TaskRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void beforeEach() {
        taskRepository.deleteAll()
                      .block();
    }

    @Test
    @DisplayName("GIVEN id not exists WHEN delete a task by id THEN does not delete the task And returns zero")
    void IdNotExists_DeleteTaskById_DoesNotDeleteTheTaskAndReturnsZero() {
        // When
        var idToDelete = UUID.randomUUID();

        var result = taskRepository.deleteTaskById(idToDelete);

        // Then
        StepVerifier.create(result)
                    .expectNext(0L)
                    .verifyComplete();
    }

    @Test
    @DisplayName("GIVEN id exists WHEN delete a task by id THEN deletes the task And returns the amount of tasks deleted")
    void IdExists_DeleteTaskById_DeletesTheTaskAndReturnsTheAmountOfTasksDeleted() {
        // Given
        var dummyTaskToBeDeleted = new Task(null, "Title Test", "Description Test", LocalDateTime.now());

        var dummyTask = taskRepository.save(dummyTaskToBeDeleted)
                                      .block();

        Assertions.assertNotNull(dummyTask);

        // When
        var idToDelete = dummyTask.id();

        var result = taskRepository.deleteTaskById(idToDelete);

        // Then
        StepVerifier.create(result)
                    .expectNext(1L)
                    .verifyComplete();

        StepVerifier.create(taskRepository.findById(dummyTask.id()))
                    .expectNextCount(0)
                    .verifyComplete();
    }

}
