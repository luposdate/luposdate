package lupos.misc.argumentparser;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Option {
	@SuppressWarnings("unchecked")
	public Option(final String name, final Class optionType) {
		this(name, new Class[] { optionType });
	}

	@SuppressWarnings("unchecked")
	public Option(final String name, final Class[] optionType) {
		boolean accept = false;
		if (optionType.length == 1) {
			if (optionType[0] == Boolean.class) {
				accept = true;
			} else if (optionType[0] == Integer.class) {
				accept = true;
			} else if (optionType[0] == String.class) {
				accept = true;
			} else if (optionType[0].isEnum()) {
				accept = true;
			}
		} else if (optionType.length == 2
				&& optionType[0] == ParameterList.class) {
			accept = true;
			this.data = new ParameterList(optionType[1]);
		} else if (optionType.length > 1
				&& optionType[0] == MixedParameterList.class) {
			accept = true;
			final List<Class> parameterTypes = new LinkedList<Class>();
			for (int i = 1; i < optionType.length; ++i) {
				parameterTypes.add(optionType[i]);
			}
			this.data = new MixedParameterList(parameterTypes);
		}
		if (!accept) {
			throw new IllegalArgumentException("wrong optionType");
		}
		this.name = name;
		this.optionType = optionType[0];
	}

	// @SuppressWarnings("unchecked")
	// public Option(final String name, final Class... optionType) {
	// boolean accept = false;
	// if (optionType.length == 1) {
	// if (optionType[0] == Boolean.class) {
	// accept = true;
	// } else if (optionType[0] == Integer.class) {
	// accept = true;
	// } else if (optionType[0] == String.class) {
	// accept = true;
	// } else if (optionType[0].isEnum()) {
	// accept = true;
	// }
	// } else if (optionType.length == 2
	// && optionType[0] == ArgumentParser.ParameterList.class) {
	// accept = true;
	// this.data = new ParameterList(optionType[1]);
	// } else if (optionType.length > 1
	// && optionType[0] == ArgumentParser.MixedParameterList.class) {
	// accept = true;
	// final List<Class> parameterTypes = new LinkedList<Class>();
	// for (int i = 1; i < optionType.length; ++i) {
	// parameterTypes.add(optionType[i]);
	// }
	// this.data = new MixedParameterList(parameterTypes);
	// }
	// if (!accept) {
	// throw new IllegalArgumentException("wrong optionType");
	// }
	// this.name = name;
	// this.optionType = optionType[0];
	// }

	public String getName() {
		return this.name;
	}

	@SuppressWarnings("unchecked")
	public Class getOptionType() {
		return this.optionType;
	}

	@SuppressWarnings("unchecked")
	public void setParameter(final List<String> paramList) {
		if (paramList == null) {
			throw new IllegalArgumentException("cannot set null parameter");
		}
		if (this.optionType == Boolean.class) {
			if (paramList.size() != 0 && paramList.size() != 1) {
				throw new IllegalArgumentException("wrong paramter count");
			}
			if (paramList.size() == 0) {
				data = Boolean.TRUE;
			} else {
				final Pattern p = Pattern.compile("^(true|false)$");
				final Matcher m = p.matcher(paramList.get(0).toLowerCase());
				if (!m.find()) {
					throw new IllegalArgumentException("wrong parameter type");
				}
				data = Boolean.parseBoolean(paramList.get(0));
			}
		} else if (this.optionType == Integer.class) {
			if (paramList.size() != 1) {
				throw new IllegalArgumentException("wrong paramter count");
			}
			data = Integer.parseInt(paramList.get(0));
		} else if (this.optionType == String.class) {
			if (paramList.size() != 1) {
				throw new IllegalArgumentException("wrong paramter count");
			}
			data = paramList.get(0);
		} else if (this.optionType.isEnum()) {
			if (paramList.size() != 1) {
				throw new IllegalArgumentException("wrong paramter count");
			}
			// data = Enum.valueOf(this.optionType, paramList.get(0));
			data = Enum
					.valueOf(this.optionType, paramList.get(0).toUpperCase());
		} else if (this.optionType == ParameterList.class) {
			final ParameterList parameterList = (ParameterList) data;
			// parameterList.add(paramList);
			parameterList.set(paramList);
		} else if (this.optionType == MixedParameterList.class) {
			final MixedParameterList mixedParameterList = (MixedParameterList) data;
			mixedParameterList.add(paramList);
		}
	}

	public boolean getBoolean() {
		return (Boolean) data;
	}

	public int getInteger() {
		return (Integer) data;
	}

	public String getString() {
		return (String) data;
	}

	@SuppressWarnings("unchecked")
	public Enum getEnum() {
		return (Enum) data;
	}

	public ParameterList getParameterList() {
		return (ParameterList) data;
	}

	public MixedParameterList getMixedParameterList() {
		return (MixedParameterList) data;
	}

	public boolean set(final Object value) {
		boolean result = true;
		if (value.getClass() != optionType) {
			result = false;
		} else {

		}
		// TODO check if overwrite only for matching types
		data = value;
		return result;
	}

	@SuppressWarnings("unchecked")
	protected final Class optionType;
	protected final String name;
	protected Object data;
}
