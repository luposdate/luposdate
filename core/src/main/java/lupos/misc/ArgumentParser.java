/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.misc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Class to parse arguments. Use addFooOption(name, defaultValue) to define
 * which types of arguments are accepted and then use parse to parse the
 * arguments and getFoo(name) to retrieve a given argument. Argument names are
 * case insensitive. The argument --help will always be accepted and cause the
 * application to display the helptext and do nothing else.
 */
public class ArgumentParser {
	@SuppressWarnings("serial")
	private class InvalidArgumentException extends Exception {
		public InvalidArgumentException(final String str) {
			super(str);
		}

		@Override
		public String toString() {
			return "Argument error: " + this.getMessage();
		}
	}

	private String appDescription;

	public String getAppDescription() {
		return appDescription;
	}

	public void setAppDescription(final String appDescription) {
		this.appDescription = appDescription;
	}

	private final Map<String, Object> values = new HashMap<String, Object>();
	private final Map<String, String> helptexts = new HashMap<String, String>();

	/**
	 * Creates a new argument parser.
	 * 
	 * @param appDescription
	 *            A description of the application and how to call it, which
	 *            will be printed as part of the helptext.
	 */
	public ArgumentParser(final String appDescription) {
		this.appDescription = appDescription;
	}

	/**
	 * Creates a new argument parsern with an empty appdescription.
	 */
	public ArgumentParser() {
		appDescription = "";
	}

	private void add(final String name, final String helptext,
			final Object defaultValue) {
		values.put(name.toLowerCase(), defaultValue);
		helptexts.put(name.toLowerCase(), helptext);
	}

	/**
	 * Add a string argument with a default of "" Will accept arguments of the
	 * form --name value or --name=value
	 * 
	 * @param name
	 *            The name of the argument
	 * @param helptext
	 *            The helptext for the argument
	 */
	public void addStringOption(final String name, final String helptext) {
		addStringOption(name, helptext, "");
	}

	/**
	 * Add a string argument with the specified default value Will accept
	 * arguments of the form --name value or --name=value
	 * 
	 * @param name
	 *            The name of the argument
	 * @param helptext
	 *            The helptext for the argument
	 * @param defaultValue
	 *            The default value
	 */
	public void addStringOption(final String name, final String helptext,
			final String defaultValue) {
		add(name, helptext, defaultValue);
	}

	/**
	 * Add an integer argument with a default value of 0 Will accept arguments
	 * of the form --name value or --name=value where value is a valid integer
	 * representation.
	 * 
	 * @param name
	 *            The name of the argument
	 * @param helptext
	 *            The helptext for the argument
	 */
	public void addIntegerOption(final String name, final String helptext) {
		addIntegerOption(name, helptext, 0);
	}

	/**
	 * Add an integer argument with the specified default value Will accept
	 * arguments of the form --name value or --name=value where value is a valid
	 * integer representation.
	 * 
	 * @param name
	 *            The name of the argument
	 * @param helptext
	 *            The helptext for the argument
	 * @param defaultValue
	 *            The default value
	 */
	public void addIntegerOption(final String name, final String helptext,
			final Integer defaultValue) {
		add(name, helptext, defaultValue);
	}

	/**
	 * Add a boolean argument with a default of false. Will accept arguments of
	 * the form --name to set it to true or --no-name to set it to false
	 * 
	 * @param name
	 *            The name of the argument
	 * @param helptext
	 *            The helptext for the argument
	 */
	public void addBooleanOption(final String name, final String helptext) {
		addBooleanOption(name, helptext, false);
	}

	/**
	 * Add a boolean argument with the specified default value. Will accept
	 * arguments of the form --name to set it to true or --no-name to set it to
	 * false
	 * 
	 * @param name
	 *            The name of the argument
	 * @param helptext
	 *            The helptext for the argument
	 * @param defaultValue
	 *            The default value
	 */
	public void addBooleanOption(final String name, final String helptext,
			final Boolean defaultValue) {
		add(name, helptext, defaultValue);
	}

	/**
	 * Add an enum argument with the specified default value. Will accept
	 * arguments of the form --name value or --name=value where value is one of
	 * the values defined by the default value's enum type. The value name will
	 * be uppercased, so the enum type should only define upper case values.
	 * 
	 * @param name
	 *            The name of the argument
	 * @param helptext
	 *            The helptext for the argument
	 * @param defaultValue
	 *            The default value
	 */
	@SuppressWarnings("unchecked")
	public void addEnumOption(final String name, final String helptext,
			final Enum defaultValue) {
		add(name, helptext, defaultValue);
	}

	public boolean set(final String name, final Object value) {
		final Object oldValue = values.get(name.toLowerCase());
		boolean result = true;
		if (oldValue != null && oldValue.getClass() != value.getClass()) {
			System.out
					.println("ArgumentParser: The class of the value "
							+ value
							+ " for the argument parser is different from the class of the old value "
							+ oldValue);
			result = false;
			;
		}
		values.put(name.toLowerCase(), value);
		return result;
	}

	/**
	 * Get the argument with the given name. Note: this returns an object, you
	 * should use one of getString, getEnum, getBool or getInt to retrieve
	 * arguments.
	 * 
	 * @param name
	 *            Name of the argument
	 * @return The argument with the given name
	 */
	public Object get(final String name) {
		return values.get(name.toLowerCase());
	}

	/**
	 * Get the string argument with the given name.
	 * 
	 * @param name
	 *            The name of the argument.
	 * @return The argument with the given name.
	 */
	public String getString(final String name) {
		return (String) get(name);
	}

	/**
	 * Get the integer argument with the given name.
	 * 
	 * @param name
	 *            The name of the argument.
	 * @return The argument with the given name.
	 */
	public int getInt(final String name) {
		return (Integer) get(name);
	}

	/**
	 * Get the boolean argument with the given name.
	 * 
	 * @param name
	 *            The name of the argument.
	 * @return The argument with the given name.
	 */
	public boolean getBool(final String name) {
		return (Boolean) get(name);
	}

	/**
	 * Get the enum argument with the given name.
	 * 
	 * @param name
	 *            The name of the argument.
	 * @return The argument with the given name.
	 */
	@SuppressWarnings("unchecked")
	public Enum getEnum(final String name) {
		return (Enum) get(name);
	}

	/**
	 * Returns the complete helptext. If the app description contains the
	 * substring [arguments], but no arguments were registered, it will be
	 * removed.
	 * 
	 * @return The helptext.
	 */
	@SuppressWarnings("unchecked")
	public String helptext() {
		if (values.isEmpty())
			return appDescription.replaceAll("\\[arguments\\]", "");
		String result = appDescription
				+ "\nThe following arguments are accepted:\n";
		final TreeSet<String> set = new TreeSet<String>();
		set.addAll(values.keySet());
		for (final String arg : set) {
			final Object def = values.get(arg);
			if (def instanceof String) {
				result += "--" + arg + " " + "string - " + helptexts.get(arg)
						+ "\n";
			} else if (def instanceof Integer) {
				result += "--" + arg + " " + "int - " + helptexts.get(arg)
						+ "\n";
			} else if (def instanceof Boolean) {
				result += "--[no-]" + arg + " - " + helptexts.get(arg) + "\n";
			} else if (def.getClass().isEnum()) {
				result += "--" + arg + " ";
				final Object[] enums = ((Enum) def).getDeclaringClass()
						.getEnumConstants();
				for (int i = 0; i < enums.length - 1; i++) {
					result += enums[i] + "|";
				}
				result += enums[enums.length - 1] + " - " + helptexts.get(arg)
						+ "\n";
			}
		}
		return result;
	}

	/**
	 * Check the given array of strings for arguments parsed by this
	 * ArgumentParser and set the appropriate values accordingly.
	 * 
	 * @param args
	 *            The strings to be parsed.
	 * @param ignoreUnknownArguments
	 *            If this is set to true unknown arguments are ignored and a
	 *            list of them will be returned (so they can be parsed further
	 *            by the application). If set to false, unknown arguments will
	 *            cause the helptext to be displayed and the application to
	 *            quit.
	 * @return The remaining strings that were not parsed by this ArgumentParser
	 *         (if ignoreUnknownArguments is set to false, the method will
	 *         return an empty list or not return at all).
	 */
	@SuppressWarnings("unchecked")
	public List<String> parse(final String[] args,
			final boolean ignoreUnknownArguments) {
		final List<String> rest = new LinkedList<String>();
		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].compareTo("") != 0) {
					if (args[i].toLowerCase().equals("--help")) {
						System.out.println(helptext());
						System.exit(0);
					}
					boolean accepted = false;
					if (args[i].length() > 2
							&& args[i].substring(0, 2).equals("--")) {
						boolean negated = false;
						String name = args[i].substring(2);
						if (name.matches("no-?.*")) {
							negated = true;
							name = name.replaceAll("^no-?", "");
							System.out.println(name);
						}
						String arg = null;
						if (name.indexOf("=") > -1) {
							arg = name.split("=", 2)[1];
							name = name.split("=", 2)[0];
						}
						final Object def = values.get(name);
						if (def != null) {
							if (def instanceof Boolean) {
								values.put(name, !negated);
								accepted = true;
							} else if (!negated) { // "no-" prefix is only
								// accepted for boolean
								// arguments
								if (arg == null) {
									i++;
									if (i >= args.length) {
										throw (new InvalidArgumentException(
												"Option --"
														+ name
														+ " expects an argument."));
									}
									arg = args[i];
								}
								if (def instanceof Integer) {
									try {
										values.put(name, Integer.parseInt(arg));
									} catch (final NumberFormatException ex) {
										throw (new InvalidArgumentException(
												"Argument to --"
														+ name
														+ " needs to be an integer."));
									}
									accepted = true;
								} else if (def instanceof String) {
									values.put(name, arg);
									accepted = true;
								} else if (def.getClass().isEnum()) {
									try {
										values.put(name, Enum.valueOf(
												((Enum) def)
														.getDeclaringClass(),
												arg.toUpperCase()));
									} catch (final IllegalArgumentException ex) {
										throw (new InvalidArgumentException(
												"Argument to --"
														+ name
														+ " needs to be one of "
														+ Arrays
																.toString(((Enum) def)
																		.getDeclaringClass()
																		.getEnumConstants())));
									}
									accepted = true;
								}
							}
						}
					}
					if (!accepted) {
						if (ignoreUnknownArguments) {
							rest.add(args[i]);
						} else {
							throw (new InvalidArgumentException(
									"Unexpected argument: " + args[i]));
						}
					}
				}
			}
		} catch (final InvalidArgumentException ex) {
			System.err.println(ex.toString());
			System.err.println(helptext());
			System.exit(1);
		}
		/*
		 * for(int i=0; i<args.length; i++) {
		 * if(args[i].substring(0,2).equals("--") &&
		 * values.keySet().contains(args[i].substring(2))) { boolean negated =
		 * false; String name = args[i].substring(2); if(name.substring(0,
		 * 3).equals("no-")) { negated = true; name = name.substring(3); }
		 * Object def = values.get(name); if(def instanceof Boolean) {
		 * values.put(name, !negated); } else if(negated) { rest.add(name); }
		 * else { String arg; if(name.indexOf('=') > -1) { arg =
		 * name.split("=",2)[1]; name = name.split("=",2)[0]; } else { i++; if(i
		 * >= args.length) {
		 * System.err.println("Option --"+name+" expects an argument");
		 * System.exit(1); } arg = args[i]; } if(def instanceof Integer) {
		 * values.put(name, Integer.parseInt(arg)); } else if(def instanceof
		 * String) { values.put(name, arg); } else { values.put(name,
		 * Enum.valueOf(((Enum)def).getDeclaringClass(), arg.toUpperCase())); }
		 * } } else { rest.add(args[i]); } }
		 */
		return rest;
	}

	@Override
	public String toString() {
		return "The values of the arguments are: " + values;
	}

	@Override
	public ArgumentParser clone() {
		final ArgumentParser ap = new ArgumentParser(appDescription);
		ap.values.putAll(values);
		ap.helptexts.putAll(helptexts);
		return ap;
	}
}
