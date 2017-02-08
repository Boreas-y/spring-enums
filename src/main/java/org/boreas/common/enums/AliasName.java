package org.boreas.common.enums;

import org.springframework.util.StringUtils;

/**
 * define the Enum values has an alias
 *
 * @author boreas
 */
public interface AliasName extends CustomEnum {
    default String alias() {
        Enum<?> enumVal = (Enum<?>) this;
        String alias = CustomEnumContext.aliasOf(enumVal);
        return StringUtils.isEmpty(alias) ? enumVal.name() : alias;
    }
}