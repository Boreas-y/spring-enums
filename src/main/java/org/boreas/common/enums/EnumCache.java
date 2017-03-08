package org.boreas.common.enums;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author boreas
 */
class EnumCache<T> {
    private Map<Enum<? extends CustomEnum>, T> enum2ObjCache = new HashMap<>();
    private Map<Class<? extends Enum<? extends CustomEnum>>, Map<T, Enum<? extends CustomEnum>>> obj2EnumCache = new HashMap<>();

    private final Class<? extends CustomEnum> type;
    private final LinkedHashMap<Class<? extends Annotation>, BiFunction<Enum, Annotation, Object>> annotationTypes;

    EnumCache(Class<? extends CustomEnum> type,
              LinkedHashMap<Class<? extends Annotation>, BiFunction<Enum, Annotation, Object>> annotationTypes) {
        this.type = type;
        this.annotationTypes = annotationTypes != null ? annotationTypes : new LinkedHashMap<>();
    }

    T getObj(Enum<?> enumValue, T defaultValue) {
        T obj = enum2ObjCache.get(enumValue);
        return obj != null ? obj : defaultValue;
    }

    <P extends Enum<? extends CustomEnum>> P getEnum(Class<P> type, T value) {
        Map<T, Enum<? extends CustomEnum>> map = obj2EnumCache.get(type);
        if (map == null)
            return null;
        return type.cast(map.get(value));
    }

    void init(CustomEnumScanner scanner) {
        enum2ObjCache = scanner.scan(type, annotationTypes);
        obj2EnumCache.clear();
        for (Map.Entry<Enum<? extends CustomEnum>, T> entry : enum2ObjCache.entrySet()) {
            Enum<? extends CustomEnum> key = entry.getKey();
            Class<? extends Enum<? extends CustomEnum>> clazz = (Class<? extends Enum<? extends CustomEnum>>) key.getClass();
            if (!obj2EnumCache.containsKey(clazz))
                obj2EnumCache.put(clazz, new HashMap<>());
            obj2EnumCache.get(clazz).put(entry.getValue(), key);
        }
    }
}
