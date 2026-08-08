package com.nexcart.exception;

import java.time.LocalDateTime;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ErrorResponse {
    
    private LocalDateTime timestamp;

    private int status;

    private String message;
    
    private String path;

}
