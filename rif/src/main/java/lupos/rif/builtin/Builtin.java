package lupos.rif.builtin;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Builtin {
	String Name();

	boolean Ignore() default false;

	boolean Bindable() default false;

	boolean Iterable() default false;
}
