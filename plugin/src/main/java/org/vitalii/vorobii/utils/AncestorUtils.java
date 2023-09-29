package org.vitalii.vorobii.utils;

import java.util.Collection;
import java.util.List;

public class AncestorUtils {
	public static final String UNCHECKED = "unchecked";

	@SuppressWarnings(UNCHECKED)
	public static  <T> List<T> getAncestors(Collection<?> objects, Class<T> clz) {
		return objects.stream()
				.filter(e -> isAssignable(clz, e))
				.map(e -> (T) e)
				.toList();
	}

	public static <T> boolean isAssignable(Class<T> clz, Object e) {
		return clz.isAssignableFrom(e.getClass());
	}

}
