package com.jobstream.api.mapper;

import com.jobstream.dto.AdzunaJobSearchResponse;
import com.jobstream.dto.JobDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Log4j2
public class AdzunaMapper {
    /**
     * Maps an Adzuna job to JobDto
     */

    public JobDto mapToJobDto(Map<String, Object> jobData) {
        // External ID
        String externalId = getString(jobData, "id");
        String title = getString(jobData, "title");

        // Company - can be a String or a Map
        Object companyObj = jobData.get("company");
        String company;
        if (companyObj instanceof Map) {
            company = getString((Map<String, Object>) companyObj, "display_name");
        } else {
            company = companyObj != null ? companyObj.toString() : null;
        }

        // Location - can be a String or a Map
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
        Object categoryObj = jobData.get("category");
        String category = null;
        if (categoryObj instanceof Map) {
            category = getString((Map<String, Object>) categoryObj, "label");
        }

        JobDto job = new JobDto(externalId, title, company, location);
        job.setDescription(description);
        job.setSalaryMin(salaryMin);
        job.setSalaryMax(salaryMax);
        job.setContractType(contractType);
        job.setJobUrl(jobUrl);

        // Parse date - Adzuna format: "2026-03-25T00:00:00Z"
        if (postedDate != null && postedDate.length() >= 10) {
            try {
                String dateStr = postedDate.substring(0, 10); // "2026-03-25"
                LocalDate date = LocalDate.parse(dateStr);
                job.setPostedDate(date);
            } catch (Exception e) {
                log.debug("Unparsable Adzuna date: '{}'", postedDate);
            }
        }

        // Add category as a tag if present
        if (category != null) {
            job.setRequirements(List.of(category));
        }

        return job;
    }

    @SuppressWarnings("unchecked")
    public AdzunaJobSearchResponse toJobSearchResponse(Map<String, Object> response){
        if (response == null) {
            return createEmptyResponse();
        }

        List<Map<String, Object>> jobDataList = (List<Map<String, Object>>) response.get("results");

        if (jobDataList == null || jobDataList.isEmpty()) {
            return createEmptyResponse();
        }

        List<JobDto> jobs = jobDataList.stream()
                .map(this::mapToJobDto)
                .filter(Objects::nonNull)
                .toList();

        // Adzuna does not always return a total, use count or jobs.size()
        Integer count = extractNumber(response, "count");

        AdzunaJobSearchResponse result = new AdzunaJobSearchResponse();
        result.setJobs(jobs);
        result.setTotal(count != null ? count : jobs.size());
        result.setCount(jobs.size());

        return result;
    }

    private AdzunaJobSearchResponse createEmptyResponse() {
        AdzunaJobSearchResponse result = new AdzunaJobSearchResponse();
        result.setJobs(new ArrayList<>());
        result.setTotal(0);
        result.setCount(0);
        return result;
    }

    private String getString(Map<String, Object> data, String key) {
        if (data == null) return null;
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

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
