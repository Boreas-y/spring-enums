package org.boreas.common.enums;

/**
 * @author boreas
 */
public interface CustomValue extends CustomEnum {

    /**
     * 返回枚举类的自定义值
     */
    @SuppressWarnings("unchecked")
    default <T> T value() {
        return CustomEnumContext.valueOf((Enum<? extends CustomValue>) this);
    }

    /**
     * 根据自定义值返回枚举值
     *
     * @param type
     *         枚举类型
     * @param value
     *         自定义值
     */
    static <T extends Enum<? extends CustomValue>> T enumOf(Class<T> type, Object value) {
        return type.cast(CustomEnumContext.enumOf(type, value));
    }
}
