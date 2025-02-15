package vn.tayjava.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageResponse<T> {
    private int page;
    private int size;
    private int  total;
    private T items;
}
