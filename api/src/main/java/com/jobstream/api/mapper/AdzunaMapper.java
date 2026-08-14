package com.jobstream.api.mapper;

import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobSearchResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AdzunaMapper {
    /**
     * Map un job Adzuna vers JobDto
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
        job.setContractType(contractType);
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

    @SuppressWarnings("unchecked")
    public JobSearchResponse toJobSearchResponse(Map<String, Object> response){
        if (response == null) {
            return createEmptyResponse();
        }

        List<Map<String, Object>> jobDataList = (List<Map<String, Object>>) response.get("results");

        if (jobDataList == null || jobDataList.isEmpty()) {
            return createEmptyResponse();
        }

        List<JobDto> jobs = jobDataList.stream()
                .map(this::mapToJobDto)
                .toList();

        // Adzuna ne retourne pas toujours un total, on utilise count ou jobs.size()
        Integer count = extractNumber(response, "count");

        JobSearchResponse result = new JobSearchResponse();
        result.setJobs(jobs);
        result.setTotal(count != null ? count : jobs.size());
        result.setCount(jobs.size());

        return result;
    }

    private JobSearchResponse createEmptyResponse() {
        JobSearchResponse result = new JobSearchResponse();
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
