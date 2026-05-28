package com.backend.jobstream.mapper;

import com.backend.jobstream.config.ContractTypeMappingConfig;
import com.jobstream.dto.JobDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Mapper pour convertir les données d'Adzuna vers notre DTO JobDto.
 */
@Component
public class AdzunaMapper {

    private final ContractTypeMappingConfig contractTypeMappingConfig;

    public AdzunaMapper(ContractTypeMappingConfig contractTypeMappingConfig) {
        this.contractTypeMappingConfig = contractTypeMappingConfig;
    }

    /**
     * Map un job Adzuna vers notre JobDto
     */
    @SuppressWarnings("unchecked")
    public JobDto mapToJobDto(Map<String, Object> jobData) {
        // ID externe
        String externalId = getString(jobData, "id");
        String title = getString(jobData, "title");
        
        // Company - peut être String ou Map
        Object companyObj = jobData.get("company");
        String company;
        if (companyObj instanceof Map) {
            company = getString((Map<String, Object>) companyObj, "display_name");
        } else {
            company = companyObj != null ? companyObj.toString() : null;
        }
        
        // Location - peut être String ou Map
        Object locationObj = jobData.get("location");
        String location;
        if (locationObj instanceof Map) {
            location = getString((Map<String, Object>) locationObj, "display_name");
        } else {
            location = locationObj != null ? locationObj.toString() : null;
        }
        
        // Description
        String description = getString(jobData, "description");
        
        // Salary
        Integer salaryMin = extractNumber(jobData, "salary_min");
        Integer salaryMax = extractNumber(jobData, "salary_max");
        
        // Contract type
        String contractType = getString(jobData, "contract_time");
        if (contractType == null) {
            contractType = getString(jobData, "contract_type");
        }
        
        // Job URL
        String jobUrl = getString(jobData, "redirect_url");
        
        // Posted date
        String postedDate = getString(jobData, "created");
        
        // Category (as requirements/tags)
        Map<String, Object> categoryData = (Map<String, Object>) jobData.get("category");
        String category = categoryData != null ? getString(categoryData, "label") : null;

        JobDto job = new JobDto(externalId, title, company, location);
        job.setDescription(description);
        job.setSalaryMin(salaryMin);
        job.setSalaryMax(salaryMax);
        job.setContractType(contractTypeMappingConfig.normalize(contractType));
        job.setJobUrl(jobUrl);
        
        // Parse date - Adzuna format: "2026-03-25T00:00:00Z"
        if (postedDate != null && postedDate.length() >= 10) {
            try {
                String dateStr = postedDate.substring(0, 10); // "2026-03-25"
                LocalDate date = LocalDate.parse(dateStr);
                job.setPostedDate(date);
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        
        // Add category as a tag if present
        if (category != null) {
            job.setRequirements(List.of(category));
        }

        return job;
    }

    private String getString(Map<String, Object> data, String key) {
        if (data == null) return null;
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private Integer extractNumber(Map<String, Object> jobData, String key) {
        Object value = jobData.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
