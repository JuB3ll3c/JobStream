package com.backend.jobstream.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for contract type normalization mappings
 */
@Configuration
@ConfigurationProperties(prefix = "adzuna.contract-mapping")
public class ContractTypeMappingConfig {

    private Map<String, String> mappings = new HashMap<>();

    public ContractTypeMappingConfig() {
        // Default mappings
        mappings.put("full_time", "Full-time");
        mappings.put("fulltime", "Full-time");
        mappings.put("part_time", "Part-time");
        mappings.put("parttime", "Part-time");
        mappings.put("contract", "Contract");
        mappings.put("contract_time", "Contract");
        mappings.put("internship", "Internship");
        mappings.put("intern", "Internship");
        mappings.put("permanent", "CDI");
    }

    public Map<String, String> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    /**
     * Normalizes a contract type using the configured mappings
     *
     * @param contractType the contract type to normalize
     * @return the normalized contract type, or the original if no mapping exists
     */
    public String normalize(String contractType) {
        if (contractType == null) {
            return null;
        }
        String normalized = mappings.get(contractType.toLowerCase());
        return normalized != null ? normalized : contractType;
    }
}
