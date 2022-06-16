package edp.core.annotation;

import java.lang.annotation.*;

/**
 * 自定义 打印日志接口参数及返回结果
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MethodLog {
    //0:打印入参+返回结果 1:打印入参
    int type() default 0;
}
