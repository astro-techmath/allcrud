package com.techmath.allcrud.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PageRequestVOTest {

    @Test
    void givenNoArgsConstructor_thenDefaultValuesAreApplied() {
        PageRequestVO request = new PageRequestVO();

        assertThat(request.getPage()).isZero();
        assertThat(request.getSize()).isEqualTo(20);
        assertThat(request.getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(request.getOrderBy()).isEqualTo("id");
    }

    @Test
    void givenAllArgsConstructor_thenFieldsArePopulated() {
        PageRequestVO request = new PageRequestVO(2, 50, Sort.Direction.DESC, "name");

        assertThat(request.getPage()).isEqualTo(2);
        assertThat(request.getSize()).isEqualTo(50);
        assertThat(request.getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(request.getOrderBy()).isEqualTo("name");
    }

    @Test
    void givenBuilderWithNoOverrides_thenDefaultsFromBuilderDefaultAreApplied() {
        PageRequestVO request = PageRequestVO.builder().build();

        assertThat(request.getPage()).isZero();
        assertThat(request.getSize()).isEqualTo(20);
        assertThat(request.getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(request.getOrderBy()).isEqualTo("id");
    }

    @Test
    void givenSetters_thenValuesAreUpdated() {
        PageRequestVO request = new PageRequestVO();

        request.setPage(5);
        request.setSize(10);
        request.setDirection(Sort.Direction.DESC);
        request.setOrderBy("price");

        assertThat(request.getPage()).isEqualTo(5);
        assertThat(request.getSize()).isEqualTo(10);
        assertThat(request.getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(request.getOrderBy()).isEqualTo("price");
    }

}
