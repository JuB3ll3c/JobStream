package com.backend.jobstream.service;

/**
 * Interface for detecting country from location string.
 * Allows for multiple detection strategies.
 */
public interface CountryDetector {

    /**
     * Detects the country code from a location string.
     *
     * @param location the location string (may be null or blank)
     * @return the country code (e.g., "ch", "fr", "gb", "it", "de")
     */
    String detectCountry(String location);
}
