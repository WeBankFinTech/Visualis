package edp.davinci.aop;

import edp.core.annotation.MethodLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Component
@Slf4j
public class MethodLogAspect {

    @Pointcut("@annotation(edp.core.annotation.MethodLog)")
    public void pointCut() {

    }

    // 日志打成一行，方便排错
    // list方法不需要加，只加增删改
    // 导入导出单独打日志
    // 操作日志单独写一个log，打到一个单独的日志文件中，回滚+清理（超过1年的清理）
    @Before(value = "pointCut()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MethodLog annotation = method.getAnnotation(MethodLog.class);
        if (Objects.isNull(annotation)) {
            return;
        }
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        log.info("============================= start ================================");
        log.info("URL: {}, Method  :{}", request.getRequestURL().toString(), methodName);
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {

                log.info("Param   :{},Value   :{}", paramNames[i], paramValues[i]);
            }
        }
    }

    @AfterReturning(returning = "ret", pointcut = "pointCut()")
    public void doAfter(Object ret) throws Throwable {
        log.info("Response :{}", ret);
        log.info("=============================  end  ================================");
    }
}
