package com.example.simple_recorder.bean;

public class NotepadBean {
    private String id;
    private String group_id;
    private String notepadContent;
    private String notepadTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotepadContent() {
        return notepadContent;
    }

    public void setNotepadContent(String notepadContent) {
        this.notepadContent = notepadContent;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getNotepadTime() {
        return notepadTime;
    }

    public void setNotepadTime(String notepadTime) {
        this.notepadTime = notepadTime;
    }
}
