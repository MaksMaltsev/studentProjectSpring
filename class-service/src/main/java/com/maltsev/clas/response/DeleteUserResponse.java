package com.maltsev.clas.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DeleteUserResponse {
    private String result;
}
