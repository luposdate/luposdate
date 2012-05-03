package lupos.misc.argumentparser;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MixedParameterList {
	@SuppressWarnings("unchecked")
	public MixedParameterList(final List<Class> parameterTypes) {
		boolean accept = false;
		final int N = parameterTypes.size();
		for (int i = 0; i < N; ++i) {
			final Class parameterType = parameterTypes.get(i);
			if (parameterType == Boolean.class) {
				accept = true;
			} else if (parameterType == Integer.class) {
				accept = true;
			} else if (parameterType == String.class) {
				accept = true;
			} else if (parameterType.isEnum()) {
				accept = true;
			} else if (parameterType == ParameterList.class) {
				accept = true;
			}
			if (!accept) {
				throw new IllegalArgumentException("wrong parameter type");
			}
			if (parameterType == ParameterList.class && i + 2 == N) {
				throw new IllegalArgumentException(
						"ParameterList accepted only at end of MixedParameterList");
			}
			accept = false;
		}
		this.parameterTypes = parameterTypes;
		this.data = new LinkedList();
		this.minElementCount = parameterTypes.size();
	}

	public void setMinElementCount(final int minElementCount) {
		if (minElementCount < 0) {
			throw new IllegalArgumentException("minElementCount < 0");
		}
		if (this.parameterTypes.get(this.parameterTypes.size() - 1) != ParameterList.class
				&& minElementCount > this.parameterTypes.size()) {
			throw new IllegalArgumentException(
					"minElementCount > #ParameterTypes");
		}
		this.minElementCount = minElementCount;
	}

	public int getMinElementCount() {
		return this.minElementCount;
	}

	@SuppressWarnings("unchecked")
	public void set(final List defaultValue) {
		boolean accept = true;
		for (int i = 0; i < this.parameterTypes.size(); ++i) {
			accept = accept
					&& (defaultValue.get(i).getClass() == this.parameterTypes
							.get(i));
		}
		if (!accept) {
			throw new IllegalArgumentException(
					"mixed parameter type and type of default value do not match");
		}
		this.data = defaultValue;
	}

	public void add(final List<String> paramList) {
		if (paramList.size() < this.minElementCount) {
			throw new IllegalArgumentException(
					"not enough parameters for this option ( "
							+ this.minElementCount + " / " + paramList.size()
							+ " )");
		}
		this.add(new LinkedList<String>(paramList), 0);
	}

	@SuppressWarnings("unchecked")
	private void add(final List<String> paramList, final int currentPos) {
		if (paramList == null) {
			throw new IllegalArgumentException("cannot set null parameter");
		}
		if (paramList.size() > 0) {
			final Class parameterType = this.parameterTypes.get(currentPos);
			final String current = paramList.remove(0);
			if (parameterType == Boolean.class) {
				final Pattern p = Pattern.compile("^(true|false)$");
				final Matcher m = p.matcher(current.toLowerCase());
				if (!m.find()) {
					throw new IllegalArgumentException("wrong parameter type");
				}
				// this.data.add(Boolean.parseBoolean(current));
				if (currentPos < data.size()) {
					this.data.set(currentPos, Boolean.parseBoolean(current));
				} else {
					this.data.add(Boolean.parseBoolean(current));
				}
				this.add(paramList, currentPos + 1);
			} else if (parameterType == Integer.class) {
				// this.data.add(Integer.parseInt(current));
				if (currentPos < data.size()) {
					this.data.set(currentPos, Integer.parseInt(current));
				} else {
					this.data.add(Integer.parseInt(current));
				}
				this.add(paramList, currentPos + 1);
			} else if (parameterType == String.class) {
				if (currentPos < data.size()) {
					this.data.set(currentPos, current);
				} else {
					this.data.add(current);
				}
				this.add(paramList, currentPos + 1);
			} else if (parameterType.isEnum()) {
				// this.data.set(currentPos, Enum.valueOf(parameterType,
				// current));
				if (currentPos < data.size()) {
					this.data.set(currentPos, Enum.valueOf(parameterType,
							current));
				} else {
					this.data.add(Enum.valueOf(parameterType, current));
				}
				this.add(paramList, currentPos + 1);
			} else if (parameterType == ParameterList.class) {
				final ParameterList parameterList = new ParameterList(
						this.parameterTypes.get(currentPos + 1));
				// parameterList.add(paramList);
				parameterList.set(paramList);
				if (currentPos < data.size()) {
					this.data.set(currentPos, parameterList);
				} else {
					this.data.add(parameterList);
				}
			} else {
				throw new IllegalArgumentException("wrong parameter type");
			}
		}
	}

	public int size() {
		return data.size();
	}

	public boolean getBoolean(final int i) {
		return (Boolean) this.data.get(i);
	}

	public int getInteger(final int i) {
		return (Integer) this.data.get(i);
	}

	public String getString(final int i) {
		return (String) this.data.get(i);
	}

	@SuppressWarnings("unchecked")
	public Enum getEnum(final int i) {
		return (Enum) this.data.get(i);
	}

	public ParameterList getParameterList(final int i) {
		return (ParameterList) this.data.get(i);
	}

	@SuppressWarnings("unchecked")
	public List<Class> getParameterTypes() {
		return this.parameterTypes;
	}

	@SuppressWarnings("unchecked")
	protected final List<Class> parameterTypes;
	@SuppressWarnings("unchecked")
	protected List data;
	protected int minElementCount;
}
