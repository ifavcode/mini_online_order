package cn.guetzjb.onlineorderingjava.utils;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.util.*;

@Component
public class NullAwareBeanUtils {

    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        String[] nullPropertyNames = getNullPropertyNames(source);
        List<String> ignoreList = new ArrayList<>(List.of(nullPropertyNames));
        ignoreList.addAll(Arrays.asList(ignoreProperties));
        String[] array = ignoreList.toArray(String[]::new);
        BeanUtils.copyProperties(source, target, array);
    }

    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }
}
