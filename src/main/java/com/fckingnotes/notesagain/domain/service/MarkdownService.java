package com.fckingnotes.notesagain.domain.service;

import com.fckingnotes.notesagain.domain.model.Note;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Service;

@Service
public class MarkdownService {
    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownService() {
        MutableDataHolder options = new MutableDataSet();

        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
    }

    public String markdownToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        return renderer.render(parser.parse(markdown));
    }

    // Изменённый метод - теперь возвращает Note
    public Note convertNoteToHtml(Note note) {
        if (note.isAutoConvertMarkdown()) {
            note.setNoteNameHtml(this.markdownToHtml(note.getNoteName()));
            note.setNoteBodyHtml(this.markdownToHtml(note.getNoteBody()));
        }

        return note;
    }
}