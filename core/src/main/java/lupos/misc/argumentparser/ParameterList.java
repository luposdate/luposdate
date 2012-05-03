package lupos.misc.argumentparser;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterList {
	@SuppressWarnings("unchecked")
	public ParameterList(final Class parameterType) {
		this.parameterType = parameterType;
		this.data = new LinkedList();
		this.minElemCount = 0;
	}

	@SuppressWarnings("unchecked")
	public Class getParameterType() {
		return this.parameterType;
	}

	public void setMinElementCount(final int minElementCount) {
		this.minElemCount = minElementCount;
	}

	public int getMinElementCount() {
		return this.minElemCount;
	}

	public void set(final List<String> paramList) {
		if (this.data.isEmpty()) {
			this.add(new LinkedList<String>(paramList));
		} else {
			final List<String> myParamList = new LinkedList<String>(paramList);
			for (int i = paramList.size(); i < this.data.size(); ++i) {
				myParamList.add(this.data.get(i).toString());
			}
			this.data.clear();
			this.set(myParamList);
		}
	}

	@SuppressWarnings("unchecked")
	protected void add(final List<String> paramList) {
		if (paramList == null) {
			throw new IllegalArgumentException("cannot set null parameter");
		}
		if (paramList.size() > 0) {
			final String current = paramList.remove(0);
			if (this.parameterType == Boolean.class) {
				final Pattern p = Pattern.compile("^(true|false)$");
				final Matcher m = p.matcher(current.toLowerCase());
				if (!m.find()) {
					throw new IllegalArgumentException("wrong parameter type");
				}
				data.add(Boolean.parseBoolean(current));
			} else if (this.parameterType == Integer.class) {
				data.add(Integer.parseInt(current));
			} else if (this.parameterType == String.class) {
				data.add(current);
			} else if (this.parameterType.isEnum()) {
				data.add(Enum.valueOf(this.parameterType, current));
			} else {
				throw new IllegalArgumentException("wrong parameter type");
			}
			add(paramList);
		} else {
			if (this.data.size() < this.minElemCount) {
				throw new IllegalArgumentException(
						"not enough parameters for this option ( "
								+ this.minElemCount + " / " + paramList.size()
								+ " )");
			}
		}
	}

	public List<Boolean> getBooleanList() {
		final List<Boolean> result = new LinkedList<Boolean>();
		for (int i = 0; i < data.size(); ++i) {
			final Boolean value = (Boolean) data.get(i);
			result.add(value);
		}
		return result;
	}

	public List<Integer> getIntegerList() {
		final List<Integer> result = new LinkedList<Integer>();
		for (int i = 0; i < data.size(); ++i) {
			final Integer value = (Integer) data.get(i);
			result.add(value);
		}
		return result;
	}

	public List<String> getStringList() {
		final List<String> result = new LinkedList<String>();
		for (int i = 0; i < data.size(); ++i) {
			final String value = (String) data.get(i);
			result.add(value);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Enum> getEnumList() {
		final List<Enum> result = new LinkedList<Enum>();
		for (int i = 0; i < data.size(); ++i) {
			final Enum value = (Enum) data.get(i);
			result.add(value);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected final Class parameterType;
	@SuppressWarnings("unchecked")
	protected final List data;
	protected int minElemCount;
}
