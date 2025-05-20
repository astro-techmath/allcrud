package com.techmath.allcrud.common;

import java.io.Serializable;

/**
 * A simple value object that represents a standardized error response for controllers.
 * <p>
 * Contains a short {@code error} message and an optional {@code description} for additional context.
 * <p>
 * Typically returned as the body of 4xx or 5xx responses.
 *
 * @param error short error code or title
 * @param description detailed message for logging or debugging
 *
 * @author Matheus Maia
 */
public record ControllerErrorVO(String error, String description) implements Serializable {
}
