package org.boreas.common.enums;

import org.boreas.common.enums.annotation.Alias;
import org.boreas.common.enums.annotation.Boolean;
import org.boreas.common.enums.annotation.Int;
import org.boreas.common.enums.annotation.Sequence;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author boreas
 */
class CustomEnumContext implements InitializingBean {

    private static LinkedHashMap<Class<? extends Annotation>, BiFunction<Enum, Annotation, Object>> valueAnnotations;
    private static LinkedHashMap<Class<? extends Annotation>, BiFunction<Enum, Annotation, Object>> aliasAnnotations;

    static {
        // init default
        valueAnnotations = new LinkedHashMap<>();
        valueAnnotations.put(Sequence.class, (t, a) -> ((Sequence) a).start() + t.ordinal());
        valueAnnotations.put(Int.class, null);
        valueAnnotations.put(Boolean.class, null);

        aliasAnnotations = new LinkedHashMap<>();
        aliasAnnotations.put(Alias.class, null);
    }

    private static EnumCache<Object> enumValueCache;
    private static EnumCache<String> enumAliasCache;
    private static CustomEnumScanner scanner;

    /**
     * initialize enum custom value or not
     */
    private static boolean customValue = true;
    /**
     * initialize enum alias name or not
     */
    private static boolean aliasName = true;

    @SuppressWarnings("unchecked")
    public static <T> T valueOf(Enum<? extends CustomEnum> enumValue) {
        return (T) enumValueCache.getObj(enumValue, enumValue.ordinal());
    }

    public static Enum<?> enumOf(Class<? extends Enum<? extends CustomEnum>> type, Object value) {
        return enumValueCache.getEnum(type, value);
    }

    public static String aliasOf(Enum<?> enumValue) {
        return enumAliasCache.getObj(enumValue, enumValue.name());
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

    public void setValueAnnotations(LinkedHashMap<Class<? extends Annotation>, BiFunction<Enum, Annotation, Object>> valueAnnotations) {
        CustomEnumContext.valueAnnotations = valueAnnotations;
    }

    public void setAliasAnnotations(LinkedHashMap<Class<? extends Annotation>, BiFunction<Enum, Annotation, Object>> aliasAnnotations) {
        CustomEnumContext.aliasAnnotations = aliasAnnotations;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(scanner, "basePackages must be set first!");
        enumValueCache = new EnumCache<>(CustomValue.class, valueAnnotations);
        enumAliasCache = new EnumCache<>(AliasName.class, aliasAnnotations);
        if (customValue)
            enumValueCache.init(scanner);
        if (aliasName)
            enumAliasCache.init(scanner);
    }
}