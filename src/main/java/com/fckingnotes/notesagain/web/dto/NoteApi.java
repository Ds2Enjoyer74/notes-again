package com.fckingnotes.notesagain.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

public sealed class NoteApi permits NoteApi.UpdateDto, NoteApi.CreateDto, NoteApi.ErrorDto {
    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static final class CreateDto extends NoteApi {
        @JsonProperty(required = true) private String noteName;
        @JsonProperty(required = true) private String noteBody;
        private String noteNameHtml;
        private String noteBodyHtml;
        private Boolean isAutoConvertMarkdown;
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static final class UpdateDto extends NoteApi {
        private String noteName;
        private String noteBody;
        private Boolean isAutoConvertMarkdown;
        //TODO add HTML logic to markdown service
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    public static final class ErrorDto extends NoteApi {
        private String message;
        private ErrorCode code;
    }

    public static CreateDto createDto(
            String noteName,
            String noteBody,
            String noteNameHtml,
            String noteBodyHtml,
            Boolean isAutoConvertMarkdown
    ) {
        return new CreateDto(
                noteName,
                noteBody,
                noteNameHtml,
                noteBodyHtml,
                isAutoConvertMarkdown
        );
    }

    public static UpdateDto updateDto(String noteName, String noteBody, Boolean isAutoConvertMarkdown) {
        return new UpdateDto(
                noteName,
                noteBody,
                isAutoConvertMarkdown
        );
    }

    public static ErrorDto error(String message, ErrorCode code) {
        return new ErrorDto(message, code);
    }

    public enum ErrorCode {
        NOT_FOUND,
        BAD_REQUEST,
        UNDEFINED
    }
}

