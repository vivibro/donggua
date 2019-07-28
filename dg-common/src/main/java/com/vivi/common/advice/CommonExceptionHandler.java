package com.vivi.common.advice;

import com.vivi.common.advice.exception.DgException;
import com.vivi.common.enums.ExceptionEnum;
import com.vivi.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//自动拦截所有web，作通用的异常处理
@ControllerAdvice
public class CommonExceptionHandler {


//    可以拦截不同的异常进行不同的处理，这里拦截的是DgException
    @ExceptionHandler(DgException.class)
    public ResponseEntity<ExceptionResult> handleException(DgException e){
        ExceptionEnum exceptionEnum = e.getExceptionEnum();
        return ResponseEntity.status(exceptionEnum.getCode()).body(new ExceptionResult(e.getExceptionEnum()));
    }
}
