/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.rif.builtin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.misc.Tuple;
import lupos.rif.RIFException;

@SuppressWarnings("unchecked")
public class RIFBuiltinFactory {
	private static final List<Class<? extends Object>> providers = Arrays
			.asList(ListPredicates.class, ListFunctions.class,
					BooleanPredicates.class, BooleanFunctions.class,
					SchemaDatatypeBuilders.class, LiteralPredicates.class,
					RDFDatatypesBuilder.class, LiteralFunctions.class,
					NumericFunctions.class, NumericPredicates.class,
					StringPredicates.class, StringFunctions.class,
					TimePredicates.class, TimeFunctions.class,
					NonStandardPredicates.class,
					NonStandardFunctions.class);
	private static final Map<String, Tuple<Builtin, Method>> builtins = new HashMap<String, Tuple<Builtin, Method>>();

	public static boolean canBind(URILiteral builtin) {
		if (builtins.isEmpty())
			initializeBuiltins();
		if (builtins.containsKey(builtin.originalString()))
			return builtins.get(builtin.originalString()).getFirst().Bindable();
		else
			return false;
	}

	public static boolean isIterable(URILiteral builtin) {
		if (builtins.isEmpty())
			initializeBuiltins();
		if (builtins.containsKey(builtin.originalString()))
			return builtins.get(builtin.originalString()).getFirst().Iterable();
		else
			return false;
	}

	public static boolean isDefined(URILiteral builtin) {
		if (builtins.isEmpty())
			initializeBuiltins();
		return builtins.containsKey(builtin.originalString());
	}

	public static Iterator<Literal> getIterator(URILiteral builtin,
			Literal... args) {
		Method methodToCall = null;
		for (final Method method : IteratorPredicates.class.getMethods())
			if (method.isAnnotationPresent(Builtin.class)
					&& method
							.getAnnotation(Builtin.class)
							.Name()
							.equals(builtin.originalString().substring(1,
									builtin.originalString().length() - 1))) {
				methodToCall = method;
				break;
			}
		try {
			return (Iterator<Literal>) methodToCall.invoke(null, args);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			throw new RIFException(e.getMessage());
		}
	}

	public static Item callBuiltin(URILiteral builtin, Argument arg) {
		if (builtins.isEmpty())
			initializeBuiltins();
		final Method method = builtins.get(builtin.originalString())
				.getSecond();
		try {
			final Item result = (Item) method.invoke(null, arg);
			return result;
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			throw new RIFException(e.getMessage());
		}
	}

	private static void initializeBuiltins() {
		builtins.clear();
		for (final Class clazz : providers) {
			final String nameSpace = ((Namespace) clazz
					.getAnnotation(Namespace.class)).value();
			for (final Method method : clazz.getDeclaredMethods())
				if (method.isAnnotationPresent(Builtin.class)) {
					final Builtin bi = method.getAnnotation(Builtin.class);
					if (!bi.Ignore())
						builtins.put("<" + nameSpace + bi.Name() + ">",
								new Tuple<Builtin, Method>(bi, method));
				}
		}
	}

	public static Argument createArgument(Literal... args) {
		return createArgument(null, null, args);
	}

	public static Argument createArgument(Bindings binding, Item... args) {
		return createArgument(binding, null, args);
	}

	public static Argument createArgument(Bindings binding, Literal result,
			Item... args) {
		final Argument arg = new Argument();
		arg.binding = binding;
		arg.result = result;
		arg.arguments = Arrays.asList(args);
		return arg;
	}

}
