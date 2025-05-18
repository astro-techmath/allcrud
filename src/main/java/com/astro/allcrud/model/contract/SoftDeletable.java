package com.astro.allcrud.model.contract;

/**
 * Marker interface for entities that support soft delete.
 * To enable soft delete behavior, implement this interface in your entity
 * and override the `softDelete()` method in your CrudService class.
 */
public interface SoftDeletable {
}
