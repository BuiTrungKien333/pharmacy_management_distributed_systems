package com.pharmacy.shared.util;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Pagination {

    private int pageNumber;

    private int pageSize;

    private int totalRecords;

    public int getTotalPages() {
        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    public int getSkip() {
        return (pageNumber - 1) * pageSize;
    }

}