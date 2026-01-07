package org.khodyko.quartzbot.exception;

/**
 * Исключение, выбрасываемое когда регион не найден
 */
public class AreaNotFoundException extends RuntimeException {

    public AreaNotFoundException(String message) {
        super(message);
    }
}

