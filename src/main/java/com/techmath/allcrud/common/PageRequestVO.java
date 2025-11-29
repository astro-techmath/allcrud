package com.techmath.allcrud.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

/**
 * Represents a pageable request for list endpoints within the Allcrud framework.
 * <p>
 * Encapsulates pagination and sorting parameters such as page number, size,
 * sorting direction, and ordering field.
 * <p>
 * Also includes constants to standardize pagination headers used in responses.
 *
 * <p> Default values:
 * <ul>
 *   <li>page = 0</li>
 *   <li>size = 20</li>
 *   <li>direction = ASC</li>
 *   <li>orderBy = "id"</li>
 * </ul>
 *
 * <p> This class is designed to be safely deserialized from JSON, ignoring unknown fields.
 *
 * @author Matheus Maia
 */
@Getter @Setter @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageRequestVO implements Serializable {

    /** Header used to indicate the current page number in responses. */
    public static final String CURRENT_PAGE_HEADER = "currentPage";

    /** Header used to indicate the number of elements in the current page. */
    public static final String CURRENT_ELEMENTS_HEADER = "currentElements";

    /** Header used to indicate the total number of elements. */
    public static final String TOTAL_ELEMENTS_HEADER = "totalElements";

    /** Header used to indicate the total number of pages. */
    public static final String TOTAL_PAGES_HEADER = "totalPages";

    /** The page number (zero-based). Default is 0. */
    @Builder.Default
    private int page = 0;

    /** The number of elements per page. Default is 20. */
    @Builder.Default
    private int size = 20;

    /** The sorting direction. Default is ASC. */
    @Builder.Default
    private Sort.Direction direction = Sort.Direction.ASC;

    /** The field to sort by. Default is "id". */
    @Builder.Default
    private String orderBy = "id";

    /**
     * Default constructor with default values.
     */
    public PageRequestVO() {
        this.page = 0;
        this.size = 20;
        this.direction = Sort.Direction.ASC;
        this.orderBy = "id";
    }

    /**
     * Constructor with all parameters.
     *
     * @param page the page number
     * @param size the page size
     * @param direction the sorting direction
     * @param orderBy the field to sort by
     */
    public PageRequestVO(int page, int size, Sort.Direction direction, String orderBy) {
        this.page = page;
        this.size = size;
        this.direction = direction;
        this.orderBy = orderBy;
    }

}
