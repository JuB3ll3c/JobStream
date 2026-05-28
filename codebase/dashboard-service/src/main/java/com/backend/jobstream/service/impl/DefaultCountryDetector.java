package com.backend.jobstream.service.impl;

import com.backend.jobstream.service.CountryDetector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Default implementation of CountryDetector.
 * Detects country from location string using keyword matching.
 */
@Component
public class DefaultCountryDetector implements CountryDetector {

    private final String defaultCountry;

    public DefaultCountryDetector(@Value("${adzuna.api.default-country:ch}") String defaultCountry) {
        this.defaultCountry = defaultCountry;
    }

    @Override
    public String detectCountry(String location) {
        if (location == null || location.isBlank()) {
            return defaultCountry;
        }

        String lowerLocation = location.toLowerCase();

        // Switzerland
        if (lowerLocation.contains("switzerland") ||
            lowerLocation.contains("suisse") ||
            lowerLocation.contains("genève") ||
            lowerLocation.contains("zurich") ||
            lowerLocation.contains("lausanne") ||
            lowerLocation.contains("bern")) {
            return "ch";
        }

        // France
        if (lowerLocation.contains("france") ||
            lowerLocation.contains("paris") ||
            lowerLocation.contains("lyon") ||
            lowerLocation.contains("marseille")) {
            return "fr";
        }

        // UK
        if (lowerLocation.contains("uk") ||
            lowerLocation.contains("united kingdom") ||
            lowerLocation.contains("london") ||
            lowerLocation.contains("manchester") ||
            lowerLocation.contains("birmingham")) {
            return "gb";
        }

        // Italy
        if (lowerLocation.contains("italy") ||
            lowerLocation.contains("italie") ||
            lowerLocation.contains("rome") ||
            lowerLocation.contains("milano") ||
            lowerLocation.contains("milan")) {
            return "it";
        }

        // Germany
        if (lowerLocation.contains("germany") ||
            lowerLocation.contains("allemagne") ||
            lowerLocation.contains("berlin") ||
            lowerLocation.contains("munich") ||
            lowerLocation.contains("münchen")) {
            return "de";
        }

        return defaultCountry;
    }
}
