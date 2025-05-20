package com.techmath.allcrud.repository;

import com.techmath.allcrud.entity.AbstractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EntityRepository<T extends AbstractEntity> extends JpaRepository<T, Long> {
}
