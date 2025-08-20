package com.trip_gg.common;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Pagination<T> {
    private List<T> content;    // 실제 데이터 목록
    private int page;           // 현재 페이지 번호
    private int size;           // 페이지 크기 (10개 등)
    private long totalElements; // 전체 데이터 수
    private int totalPages;     // 전체 페이지 수
}
