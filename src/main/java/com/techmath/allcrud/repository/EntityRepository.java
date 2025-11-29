package com.techmath.allcrud.repository;

import com.techmath.allcrud.entity.AbstractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generic base repository interface for entities managed by Allcrud.
 * <p>
 * Extends {@link JpaRepository} to provide standard persistence operations
 * for any {@link AbstractEntity} using any type identifier.
 * <p>
 * Annotated with {@link NoRepositoryBean} to prevent Spring from instantiating this interface directly.
 * <p>
 * Custom repositories should extend this interface instead of {@code JpaRepository} directly.
 *
 * @param <T> the type of entity
 *
 * @author Matheus Maia
 */
@NoRepositoryBean
public interface EntityRepository<T extends AbstractEntity<ID>, ID> extends JpaRepository<T, ID> {
}
