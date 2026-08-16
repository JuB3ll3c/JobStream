package com.jobstream.api.mapper;

import com.jobstream.dto.AdzunaJobSearchResponse;
import com.jobstream.dto.JobDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdzunaMapperTest {

    private AdzunaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AdzunaMapper();
    }

    @Test
    void mapToJobDto_shouldMapAllFields() {
        Map<String, Object> company = Map.of("display_name", "TechCorp");
        Map<String, Object> location = Map.of("display_name", "Paris, France");
        Map<String, Object> category = Map.of("label", "IT Jobs");

        Map<String, Object> jobData = new HashMap<>();
        jobData.put("id", "job_123");
        jobData.put("title", "Java Developer");
        jobData.put("company", company);
        jobData.put("location", location);
        jobData.put("description", "A great job");
        jobData.put("salary_min", 50000);
        jobData.put("salary_max", "80000");
        jobData.put("contract_time", "full_time");
        jobData.put("redirect_url", "https://example.com/job/123");
        jobData.put("created", "2026-03-25T00:00:00Z");
        jobData.put("category", category);

        JobDto job = mapper.mapToJobDto(jobData);

        assertThat(job.getExternalId()).isEqualTo("job_123");
        assertThat(job.getTitle()).isEqualTo("Java Developer");
        assertThat(job.getCompany()).isEqualTo("TechCorp");
        assertThat(job.getLocation()).isEqualTo("Paris, France");
        assertThat(job.getDescription()).isEqualTo("A great job");
        assertThat(job.getSalaryMin()).isEqualTo(50000);
        assertThat(job.getSalaryMax()).isEqualTo(80000);
        assertThat(job.getContractType()).isEqualTo("full_time");
        assertThat(job.getJobUrl()).isEqualTo("https://example.com/job/123");
        assertThat(job.getPostedDate()).isEqualTo(LocalDate.of(2026, 3, 25));
        assertThat(job.getRequirements()).containsExactly("IT Jobs");
    }

    @Test
    void mapToJobDto_shouldMapCompanyAsString() {
        Map<String, Object> jobData = Map.of(
                "id", "job_1",
                "title", "Title",
                "company", "Simple Company",
                "location", "Lyon"
        );

        JobDto job = mapper.mapToJobDto(jobData);

        assertThat(job.getCompany()).isEqualTo("Simple Company");
        assertThat(job.getLocation()).isEqualTo("Lyon");
    }

    @Test
    void mapToJobDto_shouldHandleMissingOptionalFieldsWithoutNpe() {
        Map<String, Object> jobData = Map.of(
                "id", "job_2",
                "title", "Title",
                "company", "Company",
                "location", "Lyon"
        );

        JobDto job = mapper.mapToJobDto(jobData);

        assertThat(job.getExternalId()).isEqualTo("job_2");
        assertThat(job.getTitle()).isEqualTo("Title");
        assertThat(job.getDescription()).isNull();
        assertThat(job.getSalaryMin()).isNull();
        assertThat(job.getSalaryMax()).isNull();
        assertThat(job.getContractType()).isNull();
        assertThat(job.getJobUrl()).isNull();
        assertThat(job.getPostedDate()).isNull();
        assertThat(job.getRequirements()).isEmpty();
    }

    @Test
    void mapToJobDto_shouldFallbackOnContractType() {
        Map<String, Object> jobData = Map.of(
                "id", "job_3",
                "title", "Title",
                "company", "Company",
                "location", "Lyon",
                "contract_type", "permanent"
        );

        JobDto job = mapper.mapToJobDto(jobData);

        assertThat(job.getContractType()).isEqualTo("permanent");
    }

    @Test
    void mapToJobDto_shouldParseValidDate() {
        Map<String, Object> jobData = jobWithCreated("2026-03-25T00:00:00Z");

        JobDto job = mapper.mapToJobDto(jobData);

        assertThat(job.getPostedDate()).isEqualTo(LocalDate.of(2026, 3, 25));
    }

    @Test
    void mapToJobDto_shouldKeepJobWhenDateIsInvalid() {
        Map<String, Object> jobData = jobWithCreated("not-a-date");

        JobDto job = mapper.mapToJobDto(jobData);

        assertThat(job).isNotNull();
        assertThat(job.getExternalId()).isEqualTo("job_x");
        assertThat(job.getPostedDate()).isNull();
    }

    @Test
    void mapToJobDto_shouldKeepJobWhenDateIsTooShort() {
        Map<String, Object> jobData = jobWithCreated("2026-03");

        JobDto job = mapper.mapToJobDto(jobData);

        assertThat(job).isNotNull();
        assertThat(job.getPostedDate()).isNull();
    }

    @Test
    void mapToJobDto_shouldParseStringSalary() {
        Map<String, Object> jobData = Map.of(
                "id", "job_4",
                "title", "Title",
                "company", "Company",
                "location", "Lyon",
                "salary_min", "45000",
                "salary_max", 55000
        );

        JobDto job = mapper.mapToJobDto(jobData);

        assertThat(job.getSalaryMin()).isEqualTo(45000);
        assertThat(job.getSalaryMax()).isEqualTo(55000);
    }

    @Test
    void mapToJobDto_shouldReturnNullSalaryForNonNumericString() {
        Map<String, Object> jobData = Map.of(
                "id", "job_5",
                "title", "Title",
                "company", "Company",
                "location", "Lyon",
                "salary_min", "non-numeric"
        );

        JobDto job = mapper.mapToJobDto(jobData);

        assertThat(job.getSalaryMin()).isNull();
    }

    @Test
    void mapToJobDto_shouldIgnoreCategoryWhenNotAMap() {
        Map<String, Object> jobData = Map.of(
                "id", "job_6",
                "title", "Title",
                "company", "Company",
                "location", "Lyon",
                "category", "IT Jobs"
        );

        JobDto job = mapper.mapToJobDto(jobData);

        assertThat(job).isNotNull();
        assertThat(job.getRequirements()).isEmpty();
    }

    @Test
    void toJobSearchResponse_shouldReturnEmptyForNullResponse() {
        AdzunaJobSearchResponse result = mapper.toJobSearchResponse(null);

        assertThat(result.getJobs()).isEmpty();
        assertThat(result.getTotal()).isZero();
        assertThat(result.getCount()).isZero();
    }

    @Test
    void toJobSearchResponse_shouldReturnEmptyForNullResults() {
        Map<String, Object> response = Map.of("count", 0);

        AdzunaJobSearchResponse result = mapper.toJobSearchResponse(response);

        assertThat(result.getJobs()).isEmpty();
        assertThat(result.getTotal()).isZero();
        assertThat(result.getCount()).isZero();
    }

    @Test
    void toJobSearchResponse_shouldReturnEmptyForEmptyResults() {
        Map<String, Object> response = Map.of("results", List.of(), "count", 0);

        AdzunaJobSearchResponse result = mapper.toJobSearchResponse(response);

        assertThat(result.getJobs()).isEmpty();
        assertThat(result.getTotal()).isZero();
        assertThat(result.getCount()).isZero();
    }

    @Test
    void toJobSearchResponse_shouldMapResultsWithCountAsTotal() {
        Map<String, Object> jobData = Map.of(
                "id", "job_7",
                "title", "Title",
                "company", "Company",
                "location", "Lyon"
        );
        Map<String, Object> response = Map.of("results", List.of(jobData), "count", 150);

        AdzunaJobSearchResponse result = mapper.toJobSearchResponse(response);

        assertThat(result.getJobs()).hasSize(1);
        assertThat(result.getJobs().get(0).getExternalId()).isEqualTo("job_7");
        assertThat(result.getTotal()).isEqualTo(150);
        assertThat(result.getCount()).isEqualTo(1);
    }

    @Test
    void toJobSearchResponse_shouldUseJobsSizeWhenCountMissing() {
        Map<String, Object> jobData = Map.of(
                "id", "job_8",
                "title", "Title",
                "company", "Company",
                "location", "Lyon"
        );
        Map<String, Object> response = Map.of("results", List.of(jobData, jobData));

        AdzunaJobSearchResponse result = mapper.toJobSearchResponse(response);

        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getCount()).isEqualTo(2);
    }

    private Map<String, Object> jobWithCreated(String created) {
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("id", "job_x");
        jobData.put("title", "Title");
        jobData.put("company", "Company");
        jobData.put("location", "Lyon");
        jobData.put("created", created);
        return jobData;
    }
}