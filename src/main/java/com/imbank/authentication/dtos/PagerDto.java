package com.imbank.authentication.dtos;

import com.imbank.authentication.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagerDto {
    private int page = 0;
    private int pageSize = Constants.PAGE_SIZE;
    private Sort.Direction sortOrder = Sort.Direction.DESC;
    private String sortBy = "id";
    private String action;
}
