package org.boreas.common.enums;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomEnumScanner {

    /**
     * default method name
     */
    private static final String ANNOTATION_METHOD_NAME = "value";

    private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private MetadataReaderFactory factory = new CachingMetadataReaderFactory(resolver);
    private final List<EnumData> enumDataList;

    /**
     * @see org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider#DEFAULT_RESOURCE_PATTERN
     */
    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    /**
     * @see ClassUtils#PATH_SEPARATOR
     */
    private static final char PATH_SEPARATOR = '/';

    CustomEnumScanner(List<String> basePackage) {
        enumDataList = basePackage.stream()
                .map(this::getResources)
                .flatMap(Arrays::stream)
                .map(this::getClassFromResource)
                .filter(Objects::nonNull)
                .map(this::getEnumData)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    <T> Map<Enum<? extends CustomEnum>, T> scan(Class<? extends CustomEnum> type,
                                                LinkedHashMap<Class<? extends Annotation>, BiFunction<Enum, Annotation, Object>> annotationsTypes) {
        return enumDataList.stream()
                .filter(d -> type.isAssignableFrom(d.enumValue.getClass()))
                .filter(d -> annotationsTypes.keySet().stream().anyMatch(a -> d.annotations.containsKey(a)))
                .collect(Collectors.toMap(EnumData::getEnumValue, d -> (T) getValue(d, annotationsTypes)));
    }

    private Object getValue(EnumData enumData, LinkedHashMap<Class<? extends Annotation>, BiFunction<Enum, Annotation, Object>> annotationsTypes) {
        return annotationsTypes.entrySet().stream()
                .filter(entry -> enumData.annotations.containsKey(entry.getKey()))
                .map(entry -> entry.getValue() != null
                        ? entry.getValue().apply(enumData.enumValue, enumData.annotations.get(entry.getKey()))
                        : getDefaultValue(enumData.annotations.get(entry.getKey())))
                .reduce((a, b) -> b)
                .orElse(null);
    }

    private Object getDefaultValue(Annotation annotation) {
        try {
            return annotation.getClass().getMethod(ANNOTATION_METHOD_NAME).invoke(annotation);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Resource[] getResources(String basePackage) {
        String resourcePath = new StringBuilder(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX)
                .append(ClassUtils.convertClassNameToResourcePath(basePackage))
                .append(PATH_SEPARATOR)
                .append(DEFAULT_RESOURCE_PATTERN)
                .toString();
        try {
            return resolver.getResources(resourcePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<? extends Enum<? extends CustomEnum>> getClassFromResource(Resource resource) {
        try {
            MetadataReader reader = factory.getMetadataReader(resource);
            return (Class<? extends Enum<? extends CustomEnum>>) Class.forName(reader.getClassMetadata().getClassName());
        } catch (Exception ignore) { // only return enum-class
            return null;
        }
    }

    private List<EnumData> getEnumData(Class<? extends Enum<? extends CustomEnum>> type) {
        Map<Class<? extends Annotation>, Annotation> annotationsOnClass = Stream.of(type.getAnnotations())
                .collect(Collectors.toMap(Annotation::annotationType, Function.identity()));
        return Stream.of(type.getFields())
                .filter(Field::isEnumConstant)
                .map(field -> new EnumData(field, annotationsOnClass))
                .collect(Collectors.toList());
    }

    @Immutable
    public static class EnumData {
        private final Enum<? extends CustomEnum> enumValue;
        private final Map<Class<? extends Annotation>, Annotation> annotations;

        EnumData(Field field, Map<Class<? extends Annotation>, Annotation> annotationsOnClass) {
            try {
                this.enumValue = (Enum<? extends CustomEnum>) field.get(null);
                Map<Class<? extends Annotation>, Annotation> annotationsOnValue = Stream.of(field.getAnnotations())
                        .collect(Collectors.toMap(Annotation::annotationType, Function.identity()));
                Map<Class<? extends Annotation>, Annotation> temp = new HashMap<>();
                temp.putAll(annotationsOnValue);
                temp.putAll(annotationsOnClass);
                annotations = Collections.unmodifiableMap(temp);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Enum<? extends CustomEnum> getEnumValue() {
            return enumValue;
        }
    }
}