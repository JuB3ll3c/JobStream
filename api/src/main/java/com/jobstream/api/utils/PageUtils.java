package com.jobstream.api.utils;

import com.jobstream.dto.JobDto;
import com.jobstream.dto.PagedJobResponse;
import org.springframework.data.domain.Page;

public class PageUtils {
    public static PagedJobResponse toPagedResponse(Page<JobDto> page) {
        PagedJobResponse response = new PagedJobResponse();
        response.setContent(page.getContent());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setSize(page.getSize());
        response.setPage(page.getNumber());
        return response;
    }
}
