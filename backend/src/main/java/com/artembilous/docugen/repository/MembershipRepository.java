package com.artembilous.docugen.repository;

import com.artembilous.docugen.entity.Membership;
import com.artembilous.docugen.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    boolean existsByUser(User user);

    @Query("""
        SELECT COUNT(p) > 0
        FROM Membership m
        JOIN m.roles r
        JOIN r.permissions p
        WHERE m.user.userId = :userId
          AND m.organisation.organisationId = :orgId
          AND p.name = :permission
    """)
    boolean existsPermission(Long userId, Long orgId, String permission);
}
