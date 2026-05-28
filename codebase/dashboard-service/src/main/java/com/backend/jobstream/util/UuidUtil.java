package com.backend.jobstream.util;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Utility class for UUID operations
 */
public final class UuidUtil {

    private UuidUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Parses a UUID string, throwing appropriate exception if invalid
     *
     * @param id the UUID string to parse
     * @return the parsed UUID
     * @throws ResponseStatusException with BAD_REQUEST status if the ID is invalid
     */
    public static UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID invalide");
        }
    }
}
