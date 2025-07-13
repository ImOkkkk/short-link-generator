package cn.imokkkk.util.facade;

import cn.imokkkk.exception.CommonException;
import cn.imokkkk.response.CommonResponse;

import com.alibaba.fastjson.JSON;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.validation.ValidationException;

/**
 * @author wyliu
 * @date 2025/7/13 20:54
 * @since 1.0
 */
@Aspect
@Component
public class FacadeAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacadeAspect.class);

    @Around("@annotation(cn.imokkkk.util.facade.Facade)")
    public Object facade(ProceedingJoinPoint pjp)
            throws InvocationTargetException,
                    NoSuchMethodException,
                    InstantiationException,
                    IllegalAccessException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Class<?> returnType = method.getReturnType();
        Object[] args = pjp.getArgs();
        LOGGER.info(
                "start to execute , method = "
                        + method.getName()
                        + " , args = "
                        + JSON.toJSONString(args));
        for (Object arg : args) {
            try {
                BeanValidator.validate(arg);
            } catch (ValidationException e) {
                printLog(stopWatch, method, args, "failed to validate", null, e);
                return getFailedResponse(returnType, e);
            }
        }
        try {
            Object response = pjp.proceed();
            enrichObject(response);
            printLog(stopWatch, method, args, "end to execute", response, null);
            return response;
        } catch (Throwable e) {
            printLog(stopWatch, method, args, "failed to execute", null, e);
            return getFailedResponse(returnType, e);
        }
    }

    private void printLog(
            StopWatch stopWatch,
            Method method,
            Object[] args,
            String action,
            Object response,
            Throwable throwable) {
        try {
            // 因为此处有JSON.toJSONString，可能会有异常，需要进行捕获，避免影响主干流程
            LOGGER.info(
                    getInfoMessage(action, stopWatch, method, args, response, throwable),
                    throwable);
            // 如果校验失败，则返回一个失败的response
        } catch (Exception e) {
            LOGGER.error("log failed", e);
        }
    }

    private String getInfoMessage(
            String action,
            StopWatch stopWatch,
            Method method,
            Object[] args,
            Object response,
            Throwable exception) {
        StringBuilder stringBuilder = new StringBuilder(action);
        stringBuilder.append(" ,method = ");
        stringBuilder.append(method.getName());
        stringBuilder.append(" ,cost = ");
        stringBuilder.append(stopWatch.getTotalTimeMillis()).append(" ms");
        if (response instanceof CommonResponse<?> commonResponse) {
            stringBuilder.append(" ,success = ");
            stringBuilder.append(commonResponse.isSuccess());
        }
        if (exception != null) {
            stringBuilder.append(" ,success = ");
            stringBuilder.append(false);
        }
        stringBuilder.append(" ,args = ");
        stringBuilder.append(JSON.toJSONString(Arrays.toString(args)));

        if (response != null) {
            stringBuilder.append(" ,resp = ");
            stringBuilder.append(JSON.toJSONString(response));
        }

        if (exception != null) {
            stringBuilder.append(" ,exception = ");
            stringBuilder.append(exception.getMessage());
        }

        if (response instanceof CommonResponse<?>) {
            CommonResponse commonResponse = (CommonResponse) response;
            if (!commonResponse.isSuccess()) {
                stringBuilder.append(" , execute_failed");
            }
        }

        return stringBuilder.toString();
    }

    private void enrichObject(Object response) {
        if (response instanceof CommonResponse commonResponse) {
            if (commonResponse.isSuccess()) {

            } else {
                if (commonResponse.getErrorCode() == null) {
                    commonResponse.setErrorCode(-1);
                }
            }
        }
    }

    private Object getFailedResponse(Class returnType, Throwable throwable)
            throws NoSuchMethodException,
                    InvocationTargetException,
                    InstantiationException,
                    IllegalAccessException {
        if (returnType.getDeclaredConstructor().newInstance() instanceof CommonResponse) {
            CommonResponse response =
                    (CommonResponse) returnType.getDeclaredConstructor().newInstance();
            response.setSuccess(false);
            if (throwable instanceof CommonException commonException) {
                response.setMsg(commonException.getErrorMsg());
                response.setErrorCode(commonException.getErrorCode());
            } else {
                response.setMsg(throwable.toString());
                response.setErrorCode(-1);
            }
            return response;
        }
        LOGGER.error(
                "failed to getFailedResponse , returnType ("
                        + returnType
                        + ") is not instanceof CommonResponse");
        return null;
    }
}
