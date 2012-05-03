package lupos.rif.builtin;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Namespace {
	String value();
}
