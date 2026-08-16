package com.jobstream.api.mapper;

import com.jobstream.api.entity.Job;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobMapperTest {

    private JobMapper jobMapper;

    @BeforeEach
    void setUp() {
        jobMapper = Mappers.getMapper(JobMapper.class);
    }

    @Test
    void toDto_shouldMapAllFields() {
        Job job = new Job();
        job.setId(42L);
        job.setExternalId("job_1");
        job.setTitle("Java Developer");
        job.setCompany("TechCorp");
        job.setLocation("Paris");
        job.setDescription("Full description");
        job.setSalaryMin(50000);
        job.setSalaryMax(80000);
        job.setContractType("CDI");
        job.setPostedDate(LocalDate.of(2026, 3, 25));
        job.setJobUrl("https://example.com/job/1");
        job.setRequirements(List.of("Java 17", "Spring Boot"));
        job.setCreatedAt(LocalDateTime.of(2026, 3, 25, 10, 0));
        job.setUpdatedAt(LocalDateTime.of(2026, 3, 26, 11, 30));

        JobDto dto = jobMapper.toDto(job);

        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getExternalId()).isEqualTo("job_1");
        assertThat(dto.getTitle()).isEqualTo("Java Developer");
        assertThat(dto.getCompany()).isEqualTo("TechCorp");
        assertThat(dto.getLocation()).isEqualTo("Paris");
        assertThat(dto.getDescription()).isEqualTo("Full description");
        assertThat(dto.getSalaryMin()).isEqualTo(50000);
        assertThat(dto.getSalaryMax()).isEqualTo(80000);
        assertThat(dto.getContractType()).isEqualTo("CDI");
        assertThat(dto.getPostedDate()).isEqualTo(LocalDate.of(2026, 3, 25));
        assertThat(dto.getJobUrl()).isEqualTo("https://example.com/job/1");
        assertThat(dto.getRequirements()).containsExactly("Java 17", "Spring Boot");
        assertThat(dto.getCreatedAt()).isEqualTo(job.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(job.getUpdatedAt());
    }

    @Test
    void toEntity_shouldMapRequest() {
        JobRequestDto request = new JobRequestDto("job_1", "Java Developer", "TechCorp", "Paris");
        request.setDescription("Full description");
        request.setSalaryMin(50000);
        request.setSalaryMax(80000);
        request.setContractType("CDI");
        request.setPostedDate(LocalDate.of(2026, 3, 25));
        request.setJobUrl("https://example.com/job/1");
        request.setRequirements(List.of("Java 17"));

        Job job = jobMapper.toEntity(request);

        assertThat(job.getId()).isNull();
        assertThat(job.getExternalId()).isEqualTo("job_1");
        assertThat(job.getTitle()).isEqualTo("Java Developer");
        assertThat(job.getCompany()).isEqualTo("TechCorp");
        assertThat(job.getLocation()).isEqualTo("Paris");
        assertThat(job.getDescription()).isEqualTo("Full description");
        assertThat(job.getSalaryMin()).isEqualTo(50000);
        assertThat(job.getSalaryMax()).isEqualTo(80000);
        assertThat(job.getContractType()).isEqualTo("CDI");
        assertThat(job.getPostedDate()).isEqualTo(LocalDate.of(2026, 3, 25));
        assertThat(job.getJobUrl()).isEqualTo("https://example.com/job/1");
        assertThat(job.getRequirements()).containsExactly("Java 17");
    }
}