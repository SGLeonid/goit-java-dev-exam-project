package org.forestwizard.urlshortener.url;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface IShortenedUrlRepository extends JpaRepository<ShortenedUrl, Long> {
    @Query(value = "SELECT * FROM url_shortener_db.shortened_url WHERE username = :username", nativeQuery = true)
    List<ShortenedUrl> findAllByUsername(@Param("username") String username);

    @Query(value = "SELECT * FROM url_shortener_db.shortened_url WHERE username = :username AND expires_at > CURRENT_TIMESTAMP", nativeQuery = true)
    List<ShortenedUrl> findAllValidByUsername(@Param("username") String username);

    @Query(
            value = "SELECT * FROM url_shortener_db.shortened_url WHERE username = :username AND id = :id",
            nativeQuery = true
    )
    Optional<ShortenedUrl> findByUsernameAndId(@Param("username") String username, @Param("id") Long id);

    @Query(
            value = "SELECT created_at FROM url_shortener_db.shortened_url WHERE username = :username AND id = :id",
            nativeQuery = true
    )
    Optional<Instant> findCreatedAtByUsernameAndId(@Param("username") String username, @Param("id") Long id);

    @Modifying
    @Query(
            value = "UPDATE url_shortener_db.shortened_url SET original_url = :originalUrl, expires_at = :expiresAt " +
                    "WHERE username = :username AND id = :id",
            nativeQuery = true
    )
    int updateByUsernameAndId(
            @Param("username") String username,
            @Param("id") Long id,
            String originalUrl,
            OffsetDateTime expiresAt
    );

    @Modifying
    @Query(
            value = "UPDATE url_shortener_db.shortened_url SET visit_times = visit_times + 1 WHERE id = :id",
            nativeQuery = true
    )
    int incrementUrlVisitTimesById(@Param("id") Long id);

    Optional<ShortenedUrl> findByShortUrl(String shortUrl);

    boolean existsByShortUrl(String shortUrl);
}
