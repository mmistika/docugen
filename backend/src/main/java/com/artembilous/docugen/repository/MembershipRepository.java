package com.artembilous.docugen.repository;

import com.artembilous.docugen.dto.OrganisationDTO;
import com.artembilous.docugen.entity.Membership;
import com.artembilous.docugen.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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

    @Query("""
        SELECT new com.artembilous.docugen.dto.OrganisationDTO(
            o.organisationId,
            o.name,
            (SELECT COUNT(m2) FROM Membership m2 WHERE m2.organisation = o)
        )
        FROM Membership m
        JOIN m.organisation o
        WHERE m.user = :user
    """)
    List<OrganisationDTO> findUserOrganisations(User user);
}
