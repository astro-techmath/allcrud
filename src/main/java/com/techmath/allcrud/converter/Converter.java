package com.techmath.allcrud.converter;

import com.techmath.allcrud.entity.AbstractEntity;
import com.techmath.allcrud.entity.AbstractEntityVO;

public interface Converter<T extends AbstractEntity, VO extends AbstractEntityVO> {

    VO convertToVO(T entity);

    T convertToEntity(VO vo);

}
