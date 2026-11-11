package com.union.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "documents")
public class Document {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "document_type",nullable = false)
    private String documentType;
    @Column(name = "file_path",nullable = false)
    private String filePath;
    @Column(name = "format_file")
    private String formatFile;
    @Column(name = "size_bytes",nullable = false)
    private Long sizeBytes;
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @ManyToOne()
    @JoinColumn(name = "application_id")
    private Application applicationId;
    
    public Document() {
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentType() {
        return documentType;
    }
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getFormatFile() {
        return formatFile;
    }
    public void setFormatFile(String formatFile) {
        this.formatFile = formatFile;
    }
    public Long getSizeBytes() {
        return sizeBytes;
    }
    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    public Application getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(Application applicationId) {
        this.applicationId = applicationId;
    }
    
    
    

    

}
