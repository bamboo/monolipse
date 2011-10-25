package monolipse.core.foundation;

public class Strings {

	public static String commaSeparatedList(Iterable<?> items) {
		return join(items, ",");
	}

	public static String join(Iterable<?> items, final String separator) {
		final StringBuilder builder = new StringBuilder();
		for (Object item : items) {
			if (builder.length() > 0) {
				builder.append(separator);
			}
			builder.append(item);
		}
		return builder.toString();
	}

}
