package com.vivi.common.advice.exception;

import com.vivi.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DgException extends RuntimeException{
    private ExceptionEnum exceptionEnum;
}
