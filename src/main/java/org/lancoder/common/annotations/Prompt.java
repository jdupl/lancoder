package org.lancoder.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Prompt {
	/**
	 * Message to show to the user when prompting for value.
	 * 
	 * @return
	 */
	String message();

	/**
	 * The priority of the prompt to show to the user. Lower has more priority.
	 * 
	 * @return
	 */

	int priority() default 1000;

	/**
	 * Is this option part of the advanced option.
	 * 
	 * @return
	 */
	boolean advanced() default false;
}
