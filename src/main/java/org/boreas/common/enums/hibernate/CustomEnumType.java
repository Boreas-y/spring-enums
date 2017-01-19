package org.boreas.common.enums.hibernate;

import org.boreas.common.enums.CustomValue;
import org.hibernate.HibernateException;
import org.hibernate.type.EnumType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author boreas
 */
public class CustomEnumType extends EnumType {

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        if (rs.wasNull())
            return null;
        Object object = rs.getObject(names[0]);
        return CustomValue.enumOf(returnedClass(), object);

    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        st.setObject(index, ((CustomValue) value).value());
    }

}