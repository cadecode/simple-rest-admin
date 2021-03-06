package top.cadecode.sra.common.annotation;

import java.lang.annotation.*;

/**
 * @author Cade Li
 * @date 2022/5/10
 * @description 指定动态数据源注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicDS {
    String value();
}
