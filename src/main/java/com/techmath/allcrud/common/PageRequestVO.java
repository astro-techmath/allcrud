package com.techmath.allcrud.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
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
@NoArgsConstructor @AllArgsConstructor
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

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private Sort.Direction direction = Sort.Direction.ASC;

    @Builder.Default
    private String orderBy = "id";

}
