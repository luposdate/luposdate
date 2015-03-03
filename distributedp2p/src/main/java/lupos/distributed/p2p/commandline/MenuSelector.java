/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.distributed.p2p.commandline;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * MenuSelector<T> is a helper class for creating menus in console.
 * @author Bjoern
 *
 * <br><br>For multiple selection, the following commands (comma-separated) are allowed:
 * <ul>
 * <li> x -> the command, where x is the input which executes the command
 * <li>1-10 -> all numeric input values from 1 to 10 (included) are processed
 * <li>1:10:2 -> all numeric input values from 1 to 10 (can be included) with step 2 are processed (odd/even)
 * </ul>
 *
 * @param <T> The type of results of the menu
 */
public class MenuSelector<T> {

	private T defaultValue;
	private String defaultValueString;

	/**
	 * New Menu
	 */
	public MenuSelector() {

	}

	/**
	 * Sets default values
	 * @param string the default value as String (for displaying)
	 * @param i the default value as datatype (for accessing / resulting)
	 * @return this
	 */
	public MenuSelector<T> setDefaultValue(final String string, final T i) {
		this.defaultValueString = string;
		this.defaultValue = i;
		return this;
	}



	private final List<MenuItem<T>> list = new ArrayList<MenuItem<T>>();
	private String title="";
	private PrintStream out = System.out;

	/**
	 * Sets the print stream to be used (default: System.out)
	 * @param ps the new print stream
	 * @return this
	 */
	public MenuSelector<T> setPrintStream(final PrintStream ps) {
		if (ps != null) {
			this.out = ps;
		}
		return this;
	}

	/**
	 * Sets the menu title / action descriptino
	 * @param s the title
	 * @return this
	 */
	public MenuSelector<T> setTitle(final String s) {
		if (s != null) {
			this.title = s;
		}
		return this;
	}

	/**
	 * Adds an menu item with the specified
	 * @param key key (to be pressed)
	 * @param value the value as result
	 * @param niceName the nice name to be displayed
	 * @return this
	 */
	public MenuSelector<T> addChoice(final String key, final T value, final String niceName) {
		final MenuItem<T> item = new MenuItem<T>(key.trim(),value,niceName);
		synchronized (this.list) {
			if (!this.list.contains(item)) {
				this.list.add(item);
			}
		}
		return this;
	}

	/**
	 * Adds an menu item with the specified
	 * @param key key (to be pressed)
	 * @param value the value as result (and its toString for displaying)
	 * @return this
	 */
	public MenuSelector<T> addChoice(final String key, final T value) {
		return this.addChoice(key,value,null);
	}

	private final AtomicInteger actualNumber = new AtomicInteger(1);

	/**
	 * Adds a numbered item in the menu
	 * @param value the value for the result, if choosen and its toString() for displaying
	 * @return this
	 */
	public MenuSelector<T> addNumberedChoice(final T value) {
		return this.addNumberedChoice(value,null);
	}

	/**
	 * Adds a numbered item in the menu
	 * @param value the value for the result, if choosen
	 * @param niceName the nice name to be displayed
	 * @return this
	 */
	public MenuSelector<T> addNumberedChoice(final T value,final String niceName) {
		final String key = ""+this.actualNumber.getAndIncrement();
		final MenuItem<T> item = new MenuItem<T>(key.trim(),value,niceName);
		synchronized (this.list) {
			if (!this.list.contains(item)) {
				this.list.add(item);
			}
		}
		return this;
	}


	private final List<InputValidator<T>> validators = new ArrayList<InputValidator<T>>();
	private boolean isCaseSensitiv = false;
	private boolean multiSelection = false;

	/**
	 * Adds custom free input type
	 * @param validator the validator for validating the input
	 * @return this
	 */
	public MenuSelector<T> addCustomInput(final InputValidator<T> validator) {
		synchronized (this.validators) {
			this.validators.add(validator);
		}
		return this;
	}


	/**
	 * Executed the menu
	 * @return the item which is selected
	 */
	public MenuItem<T> run() {
		return this.run(new Scanner(System.in));
	}

	/**
	 * Sets whether the input (keyboard key) is to be parsed without case-sensitive spelling
	 * @param enabled enables the parameter
	 * @return this
	 */
	public MenuSelector<T> setCaseSenstive(final boolean enabled) {
		this.isCaseSensitiv  = enabled;
		return this;
	}

	/**
	 * Sets whether multi input is allowed (select more than one item via comma seperated input)
	 * @param multiSelection enables the parameter
	 * @return this
	 */
	public MenuSelector<T> setMultiInput(final boolean multiSelection) {
		this.multiSelection  = multiSelection;
		return this;
	}

	/**
	 * Exceuted and displays the menu
	 * @param s the scanner to be used, if one already opened
	 * @return the item which is selected
	 */
	public MenuItem<T> run(final Scanner s) {
		this.out.println();
		this.out.println(this.title);
		synchronized (this.list) {
			final Iterator<MenuItem<T>> it = this.list.iterator();
			while (it.hasNext()) {
				final MenuItem<T> item = it.next();
				this.out.format("[%s]: %s %n", item.getKey(),item.getNiceName());
			}
		}
		if (this.defaultValue != null) {
			this.out.format("Ihre Eingabe [default: %s]: %n",this.defaultValue);
		} else {
			this.out.println("Ihre Eingabe:");
		}
		String input = s.nextLine().trim();
		if (input == null || input.equals("")) {
			input = ""+this.defaultValueString;
		}
		final MenuItem<T> item = new MenuItem<T>(input,null);
		if (this.storeOldInput != null) {
			this.storeOldInput.clear();
		}
		final MenuItem<T> result = this.parse(s, input, item);
		if (result == null) {
			this.out.format("Eingabe \"%s\" ist nicht gültig.",input);
			this.out.println();
			return this.run(s);
		}
		else {
			return result;
//
//		boolean keyFound = false;
//		synchronized (list) {
//			if (list.contains(item)) {
//				item = list.get(list.indexOf(item));
//				keyFound = true;
//			}
//		}
//		if (!keyFound && !isCaseSensitiv) {
//			String[] ignoreCaseSensitivList = {input.toLowerCase(),input.toUpperCase()};
//			for (String caseIgnored : ignoreCaseSensitivList) {
//				item = new MenuItem<T>(caseIgnored,null);
//				synchronized (list) {
//					if (list.contains(item)) {
//						item = list.get(list.indexOf(item));
//						keyFound = true;
//					}
//				}
//			}
//		}
//		if (!keyFound) {
//			synchronized (validators) {
//				for (InputValidator<T> validator : validators) {
//					T result = null;
//					try {
//						if ((result = validator.isValid(input)) != null) {
//							keyFound = true;
//							return new MenuItem<T>(null,result);
//						}
//					} catch (RuntimeException e) {
//						out.println("Fehlerhafte Eingabe: " + e.getLocalizedMessage());
//					}
//				}
//			}
//			out.format("Eingabe \"%s\" ist nicht gültig.",input);
//			out.println();
//			return run(s);
//		} else if (multiSelection){
//			String[] inputValues = input.split(",");
//
//		} else {
//			return item;
//		}
		}
	}

	private final List<String> storeOldInput = new ArrayList<>();

	@SuppressWarnings("unchecked")
	private MenuItem<T> parse(final Scanner s, final String input, MenuItem<T> item) {
		boolean keyFound = false;
		synchronized (this.list) {
			if (this.list.contains(item)) {
				item = this.list.get(this.list.indexOf(item));
				keyFound = true;
			}
		}
		if (!keyFound && !this.isCaseSensitiv) {
			final String[] ignoreCaseSensitivList = {input.toLowerCase(),input.toUpperCase()};
			for (final String caseIgnored : ignoreCaseSensitivList) {
				item = new MenuItem<T>(caseIgnored,null);
				synchronized (this.list) {
					if (this.list.contains(item)) {
						item = this.list.get(this.list.indexOf(item));
						keyFound = true;
					}
				}
			}
		}
		if (!keyFound) {
			synchronized (this.validators) {
				for (final InputValidator<T> validator : this.validators) {
					T result = null;
					try {
						if ((result = validator.isValid(input)) != null) {
							keyFound = true;
							return new MenuItem<T>(null,result);
						}
					} catch (final RuntimeException e) {
						this.out.println("Fehlerhafte Eingabe: " + e.getLocalizedMessage());
					}
				}
			}
			if (this.multiSelection){
				final List<MenuItem<T>> tmpList = new ArrayList<MenuItem<T>>();
				String[] inputValues = input.split(",");
				final List<String> tmpInputValues = new ArrayList<String>();
				for (final String str:inputValues) {
					if (str.matches("^\\d+-\\d+$")) {
						final String[] fromTo = str.split("-");
						final int a = Integer.parseInt(fromTo[0]);
						final int b = Integer.parseInt(fromTo[1]);
						for (int i = a; i <= b; i++) {
							tmpInputValues.add(i+"");
						}
					} else if (str.matches("^\\d+:\\d+:\\d+$")) {
						final String[] fromTo = str.split(":");
						final int a = Integer.parseInt(fromTo[0]);
						final int b = Integer.parseInt(fromTo[1]);
						final int step = Integer.parseInt(fromTo[2]);
						for (int i = a; i <= b; i=i+step) {
							tmpInputValues.add(i+"");
						}
					} else if (!this.storeOldInput.contains(str.trim())) {
						tmpInputValues.add(str.trim());
						this.storeOldInput.add(str.trim());
					}
				}
				inputValues = tmpInputValues.toArray(new String[tmpInputValues.size()]);
				for (final String str : inputValues) {
					final MenuItem<T> tmp = new MenuItem<T>(str,null);
					final MenuItem<T> result = this.parse(s,str,tmp);
					if (result != null) {
						tmpList.add(result);
					}
				}
				if (tmpList.size() > 0) {
					this.multiResult = tmpList.toArray(new MenuItem[tmpList.size()]);
					return tmpList.get(0);
				} else {
					return null;
				}
			} else {
				return null;
			}
		} else {
			return item;
		}
	}

	@SuppressWarnings("unchecked")
	private MenuItem<T>[]  multiResult = new MenuItem[0];

	/**
	 * Returns the menu-items selected on multi-input-support
	 * @return the menu items or null
	 */
	public MenuItem<T>[] getMenuResults() {
		return this.multiResult;
	}

	/**
	 * This is an MenuItems which is used as result of {@link MenuSelector#run()}.
	 * @author Bjoern
	 *
	 * @param <T> The type of result used in the menu
	 */
	public static class MenuItem<T> implements Comparable<MenuItem<T>>{
		private final T value;
		private final String key;
		private String niceName;

		private MenuItem(final String s, final T value) {
			this.key = s;
			this.value = value;
		}

		private MenuItem(final String s, final T value, final String niceName) {
			this.key = s;
			this.value = value;
			this.niceName = niceName;
		}

		/**
		 * Is the result a custom item (free input)
		 * @return yes/no
		 */
		public boolean isCustomInput() {
			return this.key == null;
		}

		/**
		 * Returns the nice name of this menu item (if available)
		 * @return the nice name, otherwise the toString of the value
		 */
		public String getNiceName() {
			return (this.niceName == null) ? this.value.toString() : this.niceName;
		}

		/**
		 * Returns the value
		 * @return the value of the menu's result
		 */
		public T getValue() {
			return this.value;
		}

		/**
		 * Returns the key (which was pressed), only available if not an {@link #isCustomInput()}
		 * @return the key
		 */
		public String getKey() {
			return this.key;
		}

		@Override
		public boolean equals(final Object o) {
			if (o != null && o instanceof MenuItem) {
				if (this.key == null || ((MenuItem<?>)o).key == null) {
					return false;
				}
				return this.key.equals(((MenuItem<?>)o).key);
			} else {
				return false;
			}
		}

		@Override
		public int compareTo(final MenuItem<T> o) {
			if (o != null) {
				if (this.key == null || ((MenuItem<?>)o).key == null) {
					return 0;
				}
				return this.key.compareTo(((MenuItem<?>)o).key);
			} else {
				return 0;
			}
		}



	}

	/**
	 * This is the interface for an InputValidator to be used in {@link MenuSelector#addCustomInput(InputValidator)}
	 * @author Bjoern
	 *
	 * @param <S> the result of the input after validation
	 */
	public interface InputValidator<S> {
		/**
		 * Parses an value in a free-text menu-entry.
		 * @param input The input of the console
		 * @return the valid menu-entry-type, if correct. Otherwise return {@code null}, so the
		 * input is not valid by this InputValidator.
		 * @throws RuntimeException Throws an exception, so that the error message is shown.
		 */
		public S isValid(String input) throws RuntimeException;
	}

	/**
	 * Is the result a multi selection? (more than one item is selected?)
	 * @return yes/no
	 */
	public boolean isMultiSelectedResult() {
		return this.multiResult != null && this.multiResult.length > 0;
	}
}