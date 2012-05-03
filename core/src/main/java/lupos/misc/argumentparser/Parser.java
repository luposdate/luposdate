package lupos.misc.argumentparser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>ArgumentParser</b> for parsing the command line
 * <ul>
 * <li>parses options (prefix --)
 * <li>options have a type (Boolean, Integer, String, Enum, ParameterList,
 * MixedParameterList
 * <li>options of type Boolean have no or one parameter and may be negated
 * (prefix --no-). of positive form implies parameter true, existence of
 * negative form implies parameter false
 * <li>options of type Integer, String and Enum have exactly one parameter (no
 * prefix --)
 * <li>options of type ParameterList have zero or more parameters. minimum
 * number may be specified. element type is specified and fix for all elements
 * <li>options of type MixedParameterList have fixed length. type of every
 * element must be specified. minimum number of parameters given at parse string
 * may be specified, not given parameters are defaulted
 * 
 * @author thimor bohn
 * 
 */
public class Parser {
	public Parser() {
		this("");
	}

	public Parser(final String appDescription) {
		this.appDescription = appDescription;
		this.helpTexts = new HashMap<String, String>();
		this.options = new HashMap<String, Option>();
	}

	protected List<String> lexxer(final String[] in) {// throws
		// InvalidArgumentException
		// {
		final LinkedList<String> out = new LinkedList<String>();

		for (int i = 0; i < in.length; ++i) {
			// string starts with -- followed by at least one alphabetic
			// character and does not contain any =
			Pattern p = Pattern.compile("^--[a-zA-Z]+[\\S&&[^=]]*$");
			Matcher m = p.matcher(in[i]);
			if (m.find()) {
				out.add(in[i].substring(m.start(), m.end()));
				continue;
			}

			// string starts with -- followed by at least one alphabetic
			// character and contains exactly one = between two lexems
			p = Pattern
					.compile("^(--[a-zA-Z]+[\\S&&[^=]]*)(?:=)([\\S&&[^=]]+)$");
			m = p.matcher(in[i]);
			if (m.find()) {
				out.add(m.group(1));
				out.add(m.group(2));
				continue;
			}

			// string does not start with --
			p = Pattern.compile("^[[^-]{2}]*[\\S]+");
			m = p.matcher(in[i]);
			if (m.find()) {
				out.add(in[i]);
				continue;
			}

			// throw new InvalidArgumentException("string is invalid: " +
			// in[i]);
			throw new IllegalArgumentException("string is invalid: " + in[i]);
		}

		return out;
	}

	protected List<String> parser(final List<String> in,
			final Map<String, Option> options) {// throws
		// InvalidArgumentException {
		final List<String> rest = new LinkedList<String>();

		Option option = null;
		List<String> paramList = new LinkedList<String>();
		for (final String lexem : in) {
			Pattern p = Pattern.compile("^--help|HELP*$");
			Matcher m = p.matcher(lexem);
			if (m.find()) {
				this.helptext();
				System.exit(0);
			}
			// string starts with --no- followed by at least one alphabetic
			// character and does not contain any =
			p = Pattern.compile("^--no-([a-zA-Z]+[\\S&&[^=]]*)$");
			m = p.matcher(lexem);
			if (m.find()) {
				if (option != null) {
					option.setParameter(paramList);
					paramList = new LinkedList<String>();
					option = null;
				}
				option = this.options.get(m.group(1));
				if (option == null) {
					throw new IllegalArgumentException("unkown option: "
							+ m.group(1));
				}
				paramList.add("FALSE");
			} else {
				// string starts with -- followed by at least one alphabetic
				// character and does not contain any =
				p = Pattern.compile("^--([a-zA-Z]+[\\S&&[^=]]*)$");
				m = p.matcher(lexem);
				if (m.find()) {
					if (option != null) {
						option.setParameter(paramList);
						paramList = new LinkedList<String>();
						option = null;
					}
					option = this.options.get(m.group(1));
					if (option == null) {
						// throw new InvalidArgumentException("unkown option: "
						// + m.group(1));
						paramList.add(lexem);
					}
				} else {
					// string does not start with --
					if (option == null) {
						rest.add(lexem);
					} else {
						paramList.add(lexem);
					}
				}
			}
		}
		if (option != null && paramList.size() > 0) {
			option.setParameter(paramList);
		}

		return rest;
	}

	public List<String> parse(final String[] in,
			final boolean ignoreUnknownArguments) {
		List<String> lexems = null;
		List<String> rest = null;
		// try {
		lexems = this.lexxer(in);
		rest = this.parser(lexems, this.options);
		if (!ignoreUnknownArguments && rest.size() > 0) {
			final StringBuilder restString = new StringBuilder();
			for (final String item : rest) {
				restString.append(" " + item);
			}
			// throw new InvalidArgumentException("Unexpected argument:"
			throw new IllegalArgumentException("Unexpected argument:"
					+ restString.toString());
		}
		// } catch (final InvalidArgumentException e) {
		// } catch (final IllegalArgumentException e) {
		// e.printStackTrace(System.err);
		// System.err.println(helptext());
		// }
		return rest;
	}

	@SuppressWarnings("unchecked")
	public void addOption(final String name, final String help,
			final Class... optionType) {
		this.options.put(name, new Option(name, optionType));
		this.helpTexts.put(name, help);
	}

	@SuppressWarnings("unchecked")
	public void addOption(final String name, final String help,
			final Class optionType, final String defaultValue) {
		final Option option = new Option(name, optionType);
		final List<String> paramList = new LinkedList<String>();
		paramList.add(defaultValue);
		option.setParameter(paramList);
		this.options.put(name, option);
		this.helpTexts.put(name, help);
	}

	public Option getOption(final String name) {
		return options.get(name);
	}

	public void addStringOption(final String name, final String helptext) {
		this.addOption(name, helptext, String.class);
	}

	public void addStringOption(final String name, final String helptext,
			final String defaultValue) {
		this.addOption(name, helptext, String.class, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public void addMixedParameterList(final String name, final String helptext,
			final Class... parameterTypes) {
		final Class[] myParameterTypes = new Class[1 + parameterTypes.length];
		myParameterTypes[0] = MixedParameterList.class;
		for (int i = 0; i < parameterTypes.length; ++i) {
			myParameterTypes[1 + i] = parameterTypes[i];
		}
		final Option option = new Option(name, myParameterTypes);
		this.options.put(name, option);
		this.helpTexts.put(name, helptext);
	}

	@SuppressWarnings("unchecked")
	public void addMixedParameterList(final String name, final String helptext,
			final List defaultValue, final int minElementCount) {
		final Class[] parameterTypes = new Class[defaultValue.size()];
		for (int i = 0; i < defaultValue.size(); ++i) {
			parameterTypes[i] = defaultValue.get(i).getClass();
		}
		this.addMixedParameterList(name, helptext, parameterTypes);
		final MixedParameterList mpl = this.getOption(name)
				.getMixedParameterList();
		mpl.setMinElementCount(minElementCount);
		mpl.set(defaultValue);
	}

	public void addIntegerOption(final String name, final String helptext) {
		this.addOption(name, helptext, Integer.class);
	}

	public void addIntegerOption(final String name, final String helptext,
			final Integer defaultValue) {
		this.addOption(name, helptext, Integer.class, Integer
				.toString(defaultValue));
	}

	public void addBooleanOption(final String name, final String helptext) {
		this.addOption(name, helptext, Boolean.class);
	}

	public void addBooleanOption(final String name, final String helptext,
			final boolean defaultValue) {
		this.addOption(name, helptext, Boolean.class, Boolean
				.toString(defaultValue));
	}

	@SuppressWarnings("unchecked")
	public void addEnumOption(final String name, final String helptext,
			final Enum defaultValue) {
		this.addOption(name, helptext, defaultValue.getDeclaringClass(),
				defaultValue.toString());
	}

	public void addStringListOption(final String name, final String helptext,
			final int minElementCount) {
		this.addOption(name, helptext, ParameterList.class, String.class);
		this.getOption(name).getParameterList().setMinElementCount(
				minElementCount);
	}

	public void addStringListOption(final String name, final String helptext) {
		this.addStringListOption(name, helptext, 0);
	}

	public void addStringListOption(final String name, final String helptext,
			final int minElementCount, final List<String> defaultValue) {
		this.addOption(name, helptext, ParameterList.class, String.class);
		this.getOption(name).getParameterList().setMinElementCount(
				minElementCount);
		this.getOption(name).setParameter(defaultValue);
	}

	public void addStringListOption(final String name, final String helptext,
			final List<String> defaultValue) {
		this.addStringListOption(name, helptext, 0, defaultValue);
	}

	public void addIntegerListOption(final String name, final String helptext,
			final int minElementCount) {
		this.addOption(name, helptext, ParameterList.class, Integer.class);
		this.getOption(name).getParameterList().setMinElementCount(
				minElementCount);
	}

	public void addIntegerListOption(final String name, final String helptext) {
		this.addIntegerListOption(name, helptext, 0);
	}

	public void addIntegerListOption(final String name, final String helptext,
			final int minElementCount, final List<Integer> defaultValue) {
		this.addOption(name, helptext, ParameterList.class, Integer.class);
		this.getOption(name).getParameterList().setMinElementCount(
				minElementCount);
		final List<String> stringList = new LinkedList<String>();
		for (int i = 0; i < defaultValue.size(); ++i) {
			stringList.add(Integer.toString(defaultValue.get(i)));
		}
		this.getOption(name).setParameter(stringList);
	}

	public void addIntegerListOption(final String name, final String helptext,
			final List<Integer> defaultValue) {
		this.addIntegerListOption(name, helptext, 0, defaultValue);
	}

	public void addBooleanListOption(final String name, final String helptext,
			final int minElementCount) {
		this.addOption(name, helptext, ParameterList.class, Boolean.class);
		this.getOption(name).getParameterList().setMinElementCount(
				minElementCount);
	}

	public void addBooleanListOption(final String name, final String helptext) {
		this.addBooleanListOption(name, helptext, 0);
	}

	public void addBooleanListOption(final String name, final String helptext,
			final int minElementCount, final List<Boolean> defaultValue) {
		this.addOption(name, helptext, ParameterList.class, Boolean.class);
		this.getOption(name).getParameterList().setMinElementCount(
				minElementCount);
		final List<String> stringList = new LinkedList<String>();
		for (int i = 0; i < defaultValue.size(); ++i) {
			stringList.add(Boolean.toString(defaultValue.get(i)));
		}
		this.getOption(name).setParameter(stringList);
	}

	public void addBooleanListOption(final String name, final String helptext,
			final List<Boolean> defaultValue) {
		this.addBooleanListOption(name, helptext, 0, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public void addEnumListOption(final String name, final String helptext,
			final Class optionType, final int minElementCount) {
		this.addOption(name, helptext, ParameterList.class, optionType);
		this.getOption(name).getParameterList().setMinElementCount(
				minElementCount);
	}

	@SuppressWarnings("unchecked")
	public void addEnumListOption(final String name, final String helptext,
			final Class optionType) {
		this.addEnumListOption(name, helptext, optionType, 0);
	}

	@SuppressWarnings("unchecked")
	public void addEnumListOption(final String name, final String helptext,
			final Class optionType, final int minElementCount,
			final List<Enum> defaultValue) {
		this.addOption(name, helptext, ParameterList.class, optionType);
		this.getOption(name).getParameterList().setMinElementCount(
				minElementCount);
		final List<String> stringList = new LinkedList<String>();
		for (int i = 0; i < defaultValue.size(); ++i) {
			stringList.add(defaultValue.get(i).toString());
		}
		this.getOption(name).setParameter(stringList);
	}

	@SuppressWarnings("unchecked")
	public void addEnumListOption(final String name, final String helptext,
			final Class optionType, final List<Enum> defaultValue) {
		this.addEnumListOption(name, helptext, optionType, 0, defaultValue);
	}

	public boolean set(final String name, final Object value) {
		// TODO really ugly code, make it better
		boolean result = true;
		Option option = options.get(name.toLowerCase());
		if (option != null && option.optionType != value.getClass()) {
			result = false;
		}
		option = new Option(name.toLowerCase(), value.getClass());
		option.set(value);
		this.options.put(name.toLowerCase(), option);
		return result;
	}

	public String getString(final String name) {
		return options.get(name).getString();
	}

	public int getInt(final String name) {
		return options.get(name).getInteger();
	}

	public boolean getBool(final String name) {
		return options.get(name).getBoolean();
	}

	@SuppressWarnings("unchecked")
	public Enum getEnum(final String name) {
		return options.get(name).getEnum();
	}

	public List<Boolean> getBooleanList(final String name) {
		return options.get(name).getParameterList().getBooleanList();
	}

	public List<Integer> getIntegerList(final String name) {
		return options.get(name).getParameterList().getIntegerList();
	}

	public List<String> getStringList(final String name) {
		return options.get(name).getParameterList().getStringList();
	}

	@SuppressWarnings("unchecked")
	public List<Enum> getEnumList(final String name) {
		return options.get(name).getParameterList().getEnumList();
	}

	public MixedParameterList getMixedList(final String name) {
		return options.get(name).getMixedParameterList();
	}

	@SuppressWarnings("unchecked")
	public String helptext() {
		if (options.isEmpty())
			return appDescription.replaceAll("\\[arguments\\]", "");
		String result = appDescription
				+ "\nThe following arguments are accepted:\n";
		final TreeSet<String> set = new TreeSet<String>();
		set.addAll(options.keySet());
		for (final String arg : set) {
			final Object def = options.get(arg);
			if (def instanceof String) {
				result += "--" + arg + " " + "string - " + helpTexts.get(arg)
						+ "\n";
			} else if (def instanceof Integer) {
				result += "--" + arg + " " + "int - " + helpTexts.get(arg)
						+ "\n";
			} else if (def instanceof Boolean) {
				result += "--[no-]" + arg + " - " + helpTexts.get(arg) + "\n";
			} else if (def.getClass().isEnum()) {
				result += "--" + arg + " ";
				final Object[] enums = ((Enum) def).getDeclaringClass()
						.getEnumConstants();
				for (int i = 0; i < enums.length - 1; i++) {
					result += enums[i] + "|";
				}
				result += enums[enums.length - 1] + " - " + helpTexts.get(arg)
						+ "\n";
			}
		}
		return result;
	}

	public String getAppDescription() {
		return appDescription;
	}

	public void setAppDescription(final String appDescription) {
		this.appDescription = appDescription;
	}

	@Override
	public String toString() {
		return "The values of the arguments are: " + options;
	}

	@Override
	public Parser clone() {
		final Parser ap = new Parser(appDescription);
		ap.options.putAll(options);
		ap.helpTexts.putAll(helpTexts);
		return ap;
	}

	protected final Map<String, String> helpTexts;
	protected final Map<String, Option> options;
	private String appDescription;
}
