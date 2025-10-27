package com.example.appecig.teacherRV;

import java.io.Serializable;

public class Course implements Serializable {
    private String name;
    private String notes;
    private String field;
    private String subject;
    private String group;
    private String videoUrl;
    private String pdfUrl;
    private String teacherName;

    public String getName() { return name; }
    public String getNotes() { return notes; }
    public String getField() { return field; }
    public String getSubject() { return subject; }
    public String getGroup() { return group; }
    public String getVideoUrl() { return videoUrl; }
    public String getPdfUrl() { return pdfUrl; }

    public String getTeacherName() { return teacherName; }


    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}