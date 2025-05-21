package com.techmath.allcrud.entity;

/**
 * Alias interface for {@link AbstractEntityVO}, for developers who prefer the DTO naming convention.
 * <p>
 * This interface behaves exactly like {@code AbstractEntityVO} and is fully compatible
 * with the Allcrud framework. You may choose to implement either based on your team's preference.
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * public class UserDTO implements AbstractEntityDTO {
 *     private Long id;
 *     private String name;
 *     // getters and setters
 * }
 * }</pre>
 *
 * @see AbstractEntityVO
 * @author Matheus Maia
 */
public interface AbstractEntityDTO extends AbstractEntityVO {
}
