package online.cangjie.face.controller;

import online.cangjie.face.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FaceControllerAdvice {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @ExceptionHandler(value = Exception.class)
    public Result commandCreationExceptionHandler(Exception e) {
        logger.error(e.getMessage());
        return Result.fail(e.getMessage());
    }
}
