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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomEnumScanner {

    /**
     * 注解取值的方法名
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
                                                List<Class<? extends Annotation>> annotationTypes) {
        return enumDataList.stream()
                .filter(d -> type.isAssignableFrom(d.enumValue.getClass()))
                .filter(d -> d.getAnnotations().keySet().stream().anyMatch(annotationTypes::contains))
                .collect(Collectors.toMap(EnumData::getEnumValue,
                        d -> (T) d.getAnnotations().entrySet().stream()
                                .filter(e -> annotationTypes.contains(e.getKey())).findFirst().get().getValue()));
    }

    /**
     * 获取指定包下所有的 Resource
     */
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

    /**
     * 根据 Resource 获取枚举类，如果不是枚举类则返回 null
     */
    private Class<? extends Enum<? extends CustomEnum>> getClassFromResource(Resource resource) {
        try {
            MetadataReader reader = factory.getMetadataReader(resource);
            return (Class<? extends Enum<? extends CustomEnum>>) Class.forName(reader.getClassMetadata().getClassName());
        } catch (Exception ignore) { // only return enum-class
            return null;
        }
    }

    private List<EnumData> getEnumData(Class<? extends Enum<? extends CustomEnum>> type) {
        return Stream.of(type.getFields())
                .filter(Field::isEnumConstant)
                .map(EnumData::new)
                .collect(Collectors.toList());
    }

    @Immutable
    public static class EnumData {
        private final Enum<? extends CustomEnum> enumValue;
        private final Map<Class<? extends Annotation>, Object> annotations;

        EnumData(Field field) {
            try {
                this.enumValue = (Enum<? extends CustomEnum>) field.get(null);
                annotations = Stream.of(field.getAnnotations())
                        .collect(Collectors.toMap(Annotation::annotationType, this::getAnnotationValue));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Object getAnnotationValue(Annotation annotation) {
            try {
                return annotation.annotationType().getMethod(ANNOTATION_METHOD_NAME).invoke(annotation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Enum<? extends CustomEnum> getEnumValue() {
            return enumValue;
        }

        public Map<Class<? extends Annotation>, Object> getAnnotations() {
            return annotations;
        }
    }
}