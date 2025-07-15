package com.fckingnotes.notesagain.controller;


import com.fckingnotes.notesagain.domain.model.Note;
import com.fckingnotes.notesagain.domain.repository.NoteRepository;
import com.fckingnotes.notesagain.web.dto.NoteApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class NoteControllerIT {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")//not necessary
                    .withUsername("testuser")//not necessary
                    .withPassword("testpass");//not necessary

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private NoteRepository noteRepository;

    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
    }

    @Test
    void should_save_note_to_db() {
        // given
        NoteApi.CreateDto createDto = NoteApi.createDto(
                "noteName",
                "noteBody",
                "NoteNameHtml",
                "noteBodyHtml",
                true
        );

        // when
        Note result = webTestClient
                .post()
                .uri("/notes")
                .bodyValue(createDto)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Note.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getId()).isGreaterThan(0);
        assertThat(result.getNoteName()).isEqualTo(createDto.getNoteName());
        assertThat(result.getNoteBody()).isEqualTo(createDto.getNoteBody());
        assertThat(result.getNoteNameHtml()).isEqualTo("<p>noteName</p>\n");
        assertThat(result.getNoteBodyHtml()).isEqualTo("<p>noteBody</p>\n");
        assertThat(result.isAutoConvertMarkdown()).isEqualTo(createDto.getIsAutoConvertMarkdown());
    }

    @Test
    void should_find_note_by_id() {
        // given
        Note savedNote = noteRepository.save(
                new Note(
                "test body",
                "test name",
                "<p>test name</p>",
                "<p>test body</p>",
                true
        )
        );

        // when
        Note resp = webTestClient
                .get()
                .uri("/notes/" + savedNote.getId())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Note.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(resp.getId()).isEqualTo(savedNote.getId());

        assertThat(resp.getNoteName()).isEqualTo(savedNote.getNoteName());
        assertThat(resp.getNoteBody()).isEqualTo(savedNote.getNoteBody());

        assertThat(resp.getNoteNameHtml()).isEqualTo(savedNote.getNoteNameHtml());
        assertThat(resp.getNoteBodyHtml()).isEqualTo(savedNote.getNoteBodyHtml());
        assertThat(resp.isAutoConvertMarkdown()).isEqualTo(savedNote.isAutoConvertMarkdown());
    }

    @Test
    void should_find_all() {
        // given
        noteRepository.saveAll(
                List.of(
                        new Note(
                                "test body1",
                                "test name1",
                                "<p>test name1</p>",
                                "<p>test body1</p>",
                                true
                        ),
                        new Note(
                                "test body2",
                                "test name2",
                                "<p>test name2</p>",
                                "<p>test body2</p>",
                                true
                        )
                )
        );

        // when
        List<Note> resp = webTestClient
                .get()
                .uri("/notes")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Note.class)
                .returnResult().getResponseBody();

        // then
        assertThat(resp).hasSize(2);
    }

    @Test
    void should_update() {
        // given
        // 1. Сначала сохраняем заметку в БД
        Note originalNote = noteRepository.save(
                new Note(
                        "Original body",
                        "Original title",
                        "<p>Original title</p>",
                        "<p>Original body</p>",
                        false
                )
        );

        // 2. Подготавливаем данные для обновления
        NoteApi.UpdateDto updateDto = NoteApi.updateDto(
                "Updated title",
                "Updated body",
                true
        );

        // when
        // 3. Отправляем PATCH-запрос
        Note resp = webTestClient
                .patch()
                .uri("/notes/" + originalNote.getId())
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Note.class)
                .returnResult().getResponseBody();

        // then
        // 4. Проверяем ответ
        // Проверяем, что ID остался прежним
        assertThat(resp.getId()).isEqualTo(originalNote.getId());

        // Проверяем обновленные поля
        assertThat(resp.getNoteName()).isEqualTo(updateDto.getNoteName());
        assertThat(resp.getNoteBody()).isEqualTo(updateDto.getNoteBody());
        assertThat(resp.isAutoConvertMarkdown()).isTrue();

        // Проверяем сгенерированные HTML-поля (если ваше приложение их автоматически генерирует)
        assertThat(resp.getNoteNameHtml()).isEqualTo("<p>Updated title</p>\n");
        assertThat(resp.getNoteBodyHtml()).isEqualTo("<p>Updated body</p>\n");
    }

    @Test
    void should_delete_by_id() {
        // given
        Note savedNote = noteRepository.save(new Note(
                "noteBody",
                "noteName",
                "NoteNameHtml",
                "noteBodyHtml",
                true
        ));

        // when & then
        webTestClient
                .delete()
                .uri("/notes/" + savedNote.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void should_return_full_html_for_note() {
        // given
        Note savedNote = noteRepository.save(new Note(
                "Test body content",
                "Test title",
                "<h1>Test title</h1>",
                "<div>Test body content</div>",
                true
        ));

        // Ожидаемый HTML в соответствии с вашей реализацией
        String expectedHtml = "<div class=\"notes\"><h1>Test title</h1><div>Test body content</div></div>";

        // when
        String resp = webTestClient
                .get()
                .uri("/notes/" + savedNote.getId() + "/full-html")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        // then
        assertThat(resp).isEqualTo(expectedHtml);
    }

    @Test
    void should_send_404_if_note_not_exists() {
        // given & when
        NoteApi.ErrorDto errorResp = webTestClient
                .get()
                .uri("/notes/123")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(NoteApi.ErrorDto.class)
                .returnResult().getResponseBody();

        // then
        assertThat(errorResp.getMessage()).isEqualTo("Note with id: 123 not found");
        assertThat(errorResp.getCode()).isEqualTo(NoteApi.ErrorCode.NOT_FOUND);
    }

    @Test
    void should_send_500_if_internal_server_error() {
        // given & when
        NoteApi.ErrorDto errorResp = webTestClient
                .get()
                .uri("/not-exists")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(NoteApi.ErrorDto.class)
                .returnResult().getResponseBody();


        // then
        assertThat(errorResp.getMessage()).contains("No static resource /not-exists");
        assertThat(errorResp.getCode()).isEqualTo(NoteApi.ErrorCode.UNDEFINED);
    }

    @Test
    void should_send_400_if_bad_request() {
        // given
        NoteApi.CreateDto createDto = NoteApi.createDto(
                null,
                "pepka",
                null,
                null,
                true
        );

        // when
        NoteApi.ErrorDto errorResp = webTestClient
                .post()
                .uri("/notes")
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(NoteApi.ErrorDto.class)
                .returnResult().getResponseBody();

        // then
        assertThat(errorResp.getMessage()).contains("It`s a bad request");
        assertThat(errorResp.getCode()).isEqualTo(NoteApi.ErrorCode.BAD_REQUEST);
    }
}


