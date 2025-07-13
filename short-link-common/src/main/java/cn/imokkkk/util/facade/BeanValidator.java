package cn.imokkkk.util.facade;

import org.hibernate.validator.HibernateValidator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;

/**
 * @author wyliu
 * @date 2025/7/13 21:16
 * @since 1.0
 */
public class BeanValidator {
    private static final Validator validator =
            Validation.byProvider(HibernateValidator.class)
                    .configure()
                    .failFast(true)
                    .buildValidatorFactory()
                    .getValidator();

    public static void validate(Object object, Class<?>... groups) throws ValidationException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (constraintViolations.stream().findFirst().isPresent()) {
            throw new ValidationException(
                    constraintViolations.stream().findFirst().get().getMessage());
        }
    }
}
