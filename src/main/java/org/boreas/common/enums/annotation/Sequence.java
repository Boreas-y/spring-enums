package org.boreas.common.enums.annotation;

import java.lang.annotation.*;

/**
 * define for enum type, indicate that the enums values are a integer sequence start from {@link #start()}
 * <p>
 * <pre>
 * Note: if @Int and @Sequence exist at the same time, @Sequence will be overrode
 *
 * &#064;Sequence
 * XEnum implements CustomValue {
 *     ONE,
 *     TWO,
 *     THREE,
 *     &#064;Int(8)
 *     EIGHT,
 *     FIVE
 * }
 *
 * </pre>
 *
 * @author boreas
 * @see org.boreas.common.enums.CustomValue CustomValuez
 * @see Int
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sequence {
    /**
     * the start value of sequence, default 1
     */
    int start() default 1;
}
