package com.jobstream.api.Utils;

import com.jobstream.dto.JobDto;
import com.jobstream.dto.PagedJobResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public class PageUtils {
    public static <T> PagedJobResponse toPagedResponse(Page<T> page) {
        PagedJobResponse response = new PagedJobResponse();
        response.setContent((List<JobDto>) page.getContent());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setSize(page.getSize());
        response.setPage(page.getNumber());
        return response;
    }
}
