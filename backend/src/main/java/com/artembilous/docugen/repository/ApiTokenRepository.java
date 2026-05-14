package com.artembilous.docugen.repository;

import com.artembilous.docugen.entity.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApiTokenRepository extends JpaRepository<ApiToken, Long> {

    @Query("SELECT t FROM ApiToken t LEFT JOIN FETCH t.permissions LEFT JOIN FETCH t.organisation WHERE t.hash = :hash")
    Optional<ApiToken> findByHashWithPermissionsAndOrganisation(String hash);

    List<ApiToken> findByOrganisationOrganisationIdOrderByCreatedAtDesc(Long orgId);
}
