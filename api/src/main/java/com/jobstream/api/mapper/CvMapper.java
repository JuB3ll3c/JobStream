package com.jobstream.api.mapper;

import com.jobstream.api.entity.UserCv;
import com.jobstream.dto.CvMetadataDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CvMapper {
    @Mapping(target = "version", source = "revision")
    CvMetadataDto toDto(UserCv cv);
}
