package com.artembilous.docugen.repository;

import com.artembilous.docugen.dto.OrganisationDTO;
import com.artembilous.docugen.entity.Membership;
import com.artembilous.docugen.entity.Organisation;
import com.artembilous.docugen.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    boolean existsByUser(User user);

    Optional<Membership> findByUserAndOrganisation(User user, Organisation org);

    Optional<Membership> findByMembershipIdAndOrganisationOrganisationId(Long membershipId, Long orgId);

    @Query("""
        SELECT COUNT(m) > 0
        FROM Membership m
        JOIN m.roles r
        WHERE m.organisation.organisationId = :orgId
        AND r.name = 'ADMIN'
        AND m.membershipId != :membershipId
    """)
    boolean existsAnotherAdmin(Long orgId, Long membershipId);

    @Query("""
        SELECT COUNT(p) > 0
        FROM Membership m
        JOIN m.roles r
        JOIN r.permissions p
        WHERE m.user.userId = :userId
          AND m.organisation.organisationId = :orgId
          AND p.name = :permission
    """)
    @Cacheable(value = "user_permissions", key = "{#userId, #orgId, #permission}")
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

    List<Membership> findByOrganisationOrganisationId(Long orgId);

    long countByOrganisationOrganisationId(Long orgId);
}
