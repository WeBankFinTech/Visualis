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

    @Before(value = "pointCut()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        log.info("============================= start ================================");
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.info("URL      :{}", request.getRequestURL().toString());
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MethodLog annotation = method.getAnnotation(MethodLog.class);
        if (Objects.isNull(annotation)) {
            return;
        }
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        log.info("Method  :{}", methodName);
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
