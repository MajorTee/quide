package io.gitlab.arturbosch.quide.platform.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Artur Bosch
 */
@SuppressWarnings("unused")
public abstract class TypeToken<T> {
	private final Type type;

	protected TypeToken() {
		this.type = extractType();
	}

	private TypeToken(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	private Type extractType() {
		Type t = getClass().getGenericSuperclass();
		if (!(t instanceof ParameterizedType)) {
			throw new RuntimeException("Invalid TypeToken; must specify type parameters");
		}
		ParameterizedType pt = (ParameterizedType) t;
		if (pt.getRawType() != TypeToken.class) {
			throw new RuntimeException("Invalid TypeToken; must directly extend TypeToken");
		}
		return pt.getActualTypeArguments()[0];
	}

	/**
	 * Gets type token for the given {@code Class} instance.
	 */
	public static <T> TypeToken<T> get(Class<T> type) {
		return new SimpleTypeToken<>(type);
	}

	/**
	 * Gets type token for the given {@code Type} instance.
	 */
	public static <T> TypeToken<T> get(Type type) {
		return new SimpleTypeToken<>(type);
	}

	private static class SimpleTypeToken<T> extends TypeToken<T> {
		public SimpleTypeToken(Type type) {
			super(type);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof TypeToken) && type.equals(((TypeToken<?>) obj).type);
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}
}
