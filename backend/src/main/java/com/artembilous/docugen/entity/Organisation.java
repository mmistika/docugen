package com.artembilous.docugen.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "organisations")
@Getter
@Setter
@ToString(exclude = {"memberships", "roles"})
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long organisationId;

    @Column(nullable = false)
    private String name;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "organisation", fetch = FetchType.LAZY)
    private Set<Membership> memberships;

    @OneToMany(mappedBy = "organisation", fetch = FetchType.LAZY)
    private Set<Role> roles;
}