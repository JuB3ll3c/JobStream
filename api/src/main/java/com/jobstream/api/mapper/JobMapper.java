package com.jobstream.api.mapper;

import com.jobstream.api.entity.Job;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobRequestDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface JobMapper {
    JobDto toDto(Job job);
    Job toEntity(JobRequestDto jobRequestDto);
}
