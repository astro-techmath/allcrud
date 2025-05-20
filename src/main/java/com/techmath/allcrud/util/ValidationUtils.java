package com.techmath.allcrud.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class that provides helper methods for validation and object inspection.
 * <p>
 * This class contains a method to identify all {@code null} properties
 * within a given bean, which is commonly used in partial update flows.
 * <p>
 * Useful when constructing example-based queries or ignoring unset fields.
 *
 * @see org.springframework.beans.BeanWrapper
 * @see com.techmath.allcrud.common.UpdaterExample
 *
 * @author Matheus Maia
 */
public class ValidationUtils {

    private ValidationUtils() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Returns an array of property names that have null values in the given source object.
     *
     * @param source the object to inspect
     * @return an array of property names with null values
     */
    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (Objects.isNull(srcValue)) {
                emptyNames.add(pd.getName());
            }
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

}
