package com.astro.allcrud.common;

import lombok.Builder;
import org.springframework.data.domain.Sort;

@Builder
public record PageRequestVO(int page, int size, Sort.Direction direction, String orderBy) {

    public static final String CURRENT_PAGE_HEADER = "currentPage";
    public static final String CURRENT_ELEMENTS_HEADER = "currentElements";
    public static final String TOTAL_ELEMENTS_HEADER = "totalElements";
    public static final String TOTAL_PAGES_HEADER = "totalPages";

}
