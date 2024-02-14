package com.astro.allcrud.converter;

import com.astro.allcrud.entity.AbstractEntity;
import com.astro.allcrud.entity.AbstractEntityVO;

public interface Converter<T extends AbstractEntity, VO extends AbstractEntityVO> {

    VO convertToVO(T entity);

    T convertToEntity(VO vo);

}
