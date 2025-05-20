package com.techmath.allcrud.model.contract;

/**
 * Marker interface for entities that support soft delete behavior.
 * <p>
 * When an entity implements this interface, the framework will call the
 * {@code softDelete()} method in {@code CrudService} instead of performing a physical deletion.
 * <p>
 * The entity itself can define any logic or field (e.g., {@code active}, {@code deleted}, {@code deletedAt})
 * to represent its "soft-deleted" state.
 *
 * <p>
 * To enable soft delete:
 * <ul>
 *   <li>Implement this interface in your entity</li>
 *   <li>Override {@code softDelete()} in your service class</li>
 * </ul>
 *
 * @see com.techmath.allcrud.service.CrudService
 *
 * @author Matheus Maia
 */
public interface SoftDeletable {
}
