package com.smartalarm.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BadRequestException extends RuntimeException {
    private String message;

    public BadRequestException(String message) {
        super(message);
        this.message = message;
    }
}
