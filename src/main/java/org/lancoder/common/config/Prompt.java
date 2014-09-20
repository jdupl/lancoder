package org.lancoder.common.config;

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
}
