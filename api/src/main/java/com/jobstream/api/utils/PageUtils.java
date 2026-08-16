package com.jobstream.api.utils;

import com.jobstream.dto.JobDto;
import com.jobstream.dto.PagedJobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

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

    public static Pageable toPageable(int page, int size, List<String> sort) {
        return PageRequest.of(page, size, toSort(sort));
    }

    public static Sort toSort(List<String> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return Sort.by(sort.stream().map(PageUtils::parseSort).toList());
    }

    private static Sort.Order parseSort(String sort) {
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (property.isEmpty()) {
            throw new IllegalArgumentException("Invalid sort property");
        }
        if (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")) {
            return Sort.Order.desc(property);
        }
        return Sort.Order.asc(property);
    }
}
