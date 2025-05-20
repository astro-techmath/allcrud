package com.techmath.allcrud.converter;

import com.techmath.allcrud.entity.AbstractEntity;
import com.techmath.allcrud.entity.AbstractEntityVO;

/**
 * Defines a contract for converting between entities and value objects (VOs).
 * <p>
 * This interface promotes separation of concerns between persistence and data transfer layers.
 * It is typically implemented per domain class to define how to translate
 * between an {@link AbstractEntity} and its corresponding {@link AbstractEntityVO}.
 *
 * @param <T> the type of the entity
 * @param <VO> the type of the value object
 *
 * @author Matheus Maia
 */
public interface Converter<T extends AbstractEntity, VO extends AbstractEntityVO> {

    /**
     * Converts the given entity to its corresponding value object.
     *
     * @param entity the entity to convert
     * @return the converted value object
     */
    VO convertToVO(T entity);

    /**
     * Converts the given value object to its corresponding entity.
     *
     * @param vo the value object to convert
     * @return the converted entity
     */
    T convertToEntity(VO vo);

}
