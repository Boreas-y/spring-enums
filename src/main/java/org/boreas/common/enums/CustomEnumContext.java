package org.boreas.common.enums;

import org.boreas.common.enums.annotation.Alias;
import org.boreas.common.enums.annotation.Int;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author boreas
 */
class CustomEnumContext implements InitializingBean {


    private static EnumCache<Object> enumValueCache = new EnumCache<>(CustomValue.class, Int.class);
    private static EnumCache<String> enumAliasCache = new EnumCache<>(AliasName.class, Alias.class);
    private static CustomEnumScanner scanner;

    /**
     * 是否初始化自定义值
     */
    private boolean customValue = true;
    /**
     * 是否初始化别名
     */
    private boolean aliasName = true;

    @SuppressWarnings("unchecked")
    public static <T> T valueOf(Enum<? extends CustomEnum> enumValue) {
        return (T) enumValueCache.getObj(enumValue, enumValue.ordinal());
    }

    public static Enum<?> enumOf(Class<? extends Enum<? extends CustomEnum>> type, Object value) {
        return enumValueCache.getEnum(type, value);
    }

    public static String aliasOf(Enum<?> enumValue) {
        return enumAliasCache.getObj(enumValue, enumValue.toString());
    }

    public void setBasePackages(List<String> basePackages) {
        scanner = new CustomEnumScanner(basePackages);
    }

    public void setCustomValue(boolean customValue) {
        this.customValue = customValue;
    }

    public void setAliasName(boolean aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(scanner, "basePackages must be set first!");
        if (customValue)
            enumValueCache.init(scanner);
        if (aliasName)
            enumAliasCache.init(scanner);
    }
}