package org.boreas.common.enums.annotation;

import java.lang.annotation.*;

/**
 * define alias of enum value
 *
 * @author boreas
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Alias {
    /**
     * alias
     */
    String value();
}