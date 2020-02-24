import java.util.Objects;

/**
 * Implements Pair for systems that don't have java-openjfx(or Java SE from Oracle) installed
 */
public class Pair<T, U> {
	private T key;
	private U value;

	public Pair(T key, U value) {
		this.key = key;
		this.value = value;
	}


	public T getKey() {
		return key;
	}

	public U getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return key.equals(pair.key) &&
				Objects.equals(value, pair.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}
}
