package com.fckingnotes.notesagain.domain.model;

import com.fckingnotes.notesagain.web.dto.NoteApi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notes")
public class Note {
    //Конструктор
    public Note(String noteBody, String noteName, String noteNameHtml, String noteBodyHtml, boolean autoConvertMarkdown) {
        this.noteBody = noteBody;
        this.noteName = noteName;
        this.noteNameHtml = noteNameHtml;
        this.noteBodyHtml = noteBodyHtml;
        this.autoConvertMarkdown = autoConvertMarkdown;
    }

    @Id
    private Long id;
    private String noteName;
    private String noteBody;
    private String noteNameHtml;
    private String noteBodyHtml;

    @Transient
    private boolean autoConvertMarkdown = true;

    public static Note from(NoteApi.CreateDto createDto) {
        return new Note(
                createDto.getNoteBody(),
                createDto.getNoteName(),
                createDto.getNoteNameHtml(),
                createDto.getNoteBodyHtml(),
                createDto.getIsAutoConvertMarkdown()
        );
    }

    // Метод для получения полного HTML-представления
    public String getFullHtml() {
        return "<div class=\"notes\">" +
                (noteNameHtml != null ? noteNameHtml : "<h1>" + escapeHtml(noteName) + "</h1>") +
                (noteBodyHtml != null ? noteBodyHtml : "") +
                "</div>";
    }

    // Метод для экранирования HTML
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}