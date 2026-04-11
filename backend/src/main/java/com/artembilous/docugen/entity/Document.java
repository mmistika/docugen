package com.artembilous.docugen.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@ToString
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne
    @JoinColumn(name = "template_version_id", nullable = false)
    private TemplateVersion templateVersion;

    @ManyToOne
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(nullable = false)
    private byte[] file;

    @CreationTimestamp
    private LocalDateTime createdAt;
}