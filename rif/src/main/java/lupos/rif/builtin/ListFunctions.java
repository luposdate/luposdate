/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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

import java.util.ArrayList;
import java.util.List;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.rif.IExpression;
import lupos.rif.model.Constant;
import lupos.rif.model.RuleList;
import lupos.rif.model.RuleVariable;

import com.google.common.collect.Lists;

@Namespace(value = "http://www.w3.org/2007/rif-builtin-function#")
public class ListFunctions {

	@Builtin(Name = "make-list")
	public static RuleList make_list(final Argument arg) {
		final RuleList result = new RuleList();
		for (final Item item : arg.arguments) {
			if (item instanceof Variable)
				result.addItem(new RuleVariable(item.getName()));
			else if (item instanceof Literal)
				result.addItem(new Constant((Literal) item, result));
			else if (item instanceof IExpression)
				result.addItem((IExpression) item);
		}
		return result;
	}

	@Builtin(Name = "count")
	public static Literal count(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof RuleList)
			return BuiltinHelper.createXSLiteral(
					((RuleList) arg.arguments.get(0)).getItems().size(),
					"integer");
		else
			return null;
	}

	@Builtin(Name = "get")
	public static Item get(final Argument arg) {
		if (arg.arguments.size() == 2
				&& arg.arguments.get(0) instanceof RuleList
				&& arg.arguments.get(1) instanceof TypedLiteral) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			int index = BuiltinHelper.getInteger((TypedLiteral) arg.arguments
					.get(1));
			if (index < 0)
				index += list.getItems().size();
			if (index < 0 || index >= list.getItems().size())
				return null;
			else {
				final IExpression expr = list.getItems().get(index);
				return (Item) expr.evaluate(arg.binding);
			}
		} else
			return null;
	}

	@Builtin(Name = "sublist")
	public static RuleList sublist(final Argument arg) {
		if (arg.arguments.size() > 1 && arg.arguments.size() < 4
				&& arg.arguments.get(0) instanceof RuleList
				&& arg.arguments.get(1) instanceof TypedLiteral) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			final int start = BuiltinHelper
					.getInteger((TypedLiteral) arg.arguments.get(1));
			int stop = list.getItems().size() - 1;
			if (arg.arguments.size() == 3
					&& arg.arguments.get(2) instanceof TypedLiteral)
				stop = BuiltinHelper.getInteger((TypedLiteral) arg.arguments
						.get(2));
			List<IExpression> subList = list.getItems().subList(start, stop);
			final RuleList result = new RuleList();
			result.getItems().addAll(subList);
			return result;
		}
		return null;
	}

	@Builtin(Name = "append")
	public static RuleList append(final Argument arg) {
		if (arg.arguments.size() > 1
				&& arg.arguments.get(0) instanceof RuleList) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			boolean first = true;
			for (final Item item : arg.arguments) {
				if (first) {
					first = false;
					continue;
				}
				if (item instanceof Variable)
					list.addItem(new RuleVariable(item.getName()));
				else if (item instanceof Literal)
					list.addItem(new Constant((Literal) item, list));
				else if (item instanceof IExpression)
					list.addItem((IExpression) item);
			}
			return list;
		}
		return null;
	}

	@Builtin(Name = "concatenate")
	public static RuleList concatenate(final Argument arg) {
		if (arg.arguments.size() > 0
				&& arg.arguments.get(0) instanceof RuleList) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			boolean first = true;
			for (final Item item : arg.arguments) {
				if (first) {
					first = false;
					continue;
				}
				list.getItems().addAll(((RuleList) item).getItems());
			}
			return list;
		}
		return null;
	}

	@Builtin(Name = "insert-before")
	public static RuleList insert_before(final Argument arg) {
		if (arg.arguments.size() == 3
				&& arg.arguments.get(0) instanceof RuleList
				&& arg.arguments.get(1) instanceof TypedLiteral
				&& arg.arguments.get(2) instanceof Item) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			int index = BuiltinHelper.getInteger((TypedLiteral) arg.arguments
					.get(1));
			final Item item = arg.arguments.get(2);
			if (index < 0)
				index += list.getItems().size();
			if (index < 0 || index >= list.getItems().size())
				return null;
			List<IExpression> original = new ArrayList<IExpression>(
					list.getItems());
			list.getItems().clear();
			for (int i = 0; i < original.size(); i++) {
				final IExpression expr = original.get(i);
				if (i == index) {
					if (item instanceof Variable)
						list.addItem(new RuleVariable(item.getName()));
					else if (item instanceof Literal)
						list.addItem(new Constant((Literal) item, list));
					else if (item instanceof IExpression)
						list.addItem((IExpression) item);
				}
				list.addItem(expr);
			}
			return list;
		}
		return null;
	}

	@Builtin(Name = "remove")
	public static RuleList remove(final Argument arg) {
		if (arg.arguments.size() == 2
				&& arg.arguments.get(0) instanceof RuleList
				&& arg.arguments.get(1) instanceof TypedLiteral) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			int index = BuiltinHelper.getInteger((TypedLiteral) arg.arguments
					.get(1));
			if (index < 0)
				index += list.getItems().size();
			if (index < 0 || index >= list.getItems().size())
				return null;
			else {
				list.getItems().remove(index);
				return list;
			}
		} else
			return null;
	}

	@Builtin(Name = "reverse")
	public static RuleList reverse(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof RuleList) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			List<IExpression> reversed = new ArrayList<IExpression>(
					Lists.reverse(list.getItems()));
			list.getItems().clear();
			list.getItems().addAll(reversed);
			return list;
		} else
			return null;
	}

	@Builtin(Name = "index-of")
	public static RuleList index_of(final Argument arg) {
		if (arg.arguments.size() == 2
				&& arg.arguments.get(0) instanceof RuleList
				&& arg.arguments.get(1) instanceof Item) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			final Item item = arg.arguments.get(1);
			IExpression lookingFor = null;
			if (item instanceof Variable)
				lookingFor = new RuleVariable(item.getName());
			else if (item instanceof Literal)
				lookingFor = new Constant((Literal) item, list);
			else if (item instanceof IExpression)
				lookingFor = (IExpression) item;
			final RuleList results = new RuleList();
			for (int i = 0; i < list.getItems().size(); i++) {
				if (list.getItems().get(i).equals(lookingFor))
					results.addItem(new Constant(BuiltinHelper
							.createXSLiteral(i, "integer"), results));
			}
			return results;
		}
		return null;
	}

	@Builtin(Name = "union")
	public static RuleList union(final Argument arg) {
		if (arg.arguments.size() > 0
				&& arg.arguments.get(0) instanceof RuleList) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			final List<IExpression> newList = Lists.newArrayList();
			for (final IExpression expr : list.getItems())
				if (!newList.contains(expr))
					newList.add(expr);
			list.getItems().clear();
			list.getItems().addAll(newList);
			boolean first = true;
			for (final Item item : arg.arguments) {
				if (first) {
					first = false;
					continue;
				}
				for (final IExpression expr : ((RuleList) item).getItems())
					if (!list.getItems().contains(expr))
						list.addItem(expr);
			}
			return list;
		}
		return null;
	}

	@Builtin(Name = "distinct-values")
	public static RuleList distinct_values(final Argument arg) {
		if (arg.arguments.size() == 1
				&& arg.arguments.get(0) instanceof RuleList) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			final List<IExpression> newList = Lists.newArrayList();
			for (final IExpression expr : list.getItems())
				if (!newList.contains(expr))
					newList.add(expr);
			list.getItems().clear();
			list.getItems().addAll(newList);
			return list;
		}
		return null;
	}

	@Builtin(Name = "intersect")
	public static RuleList intersect(final Argument arg) {
		if (arg.arguments.size() > 0
				&& arg.arguments.get(0) instanceof RuleList) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			final List<IExpression> newList = Lists.newArrayList();
			for (final Item item : arg.arguments) {
				for (final IExpression expr : ((RuleList) item).getItems()) {
					boolean contained = true;
					for (final Item item1 : arg.arguments)
						if (!((RuleList) item1).getItems().contains(expr)) {
							contained = false;
							break;
						}
					if (contained && !newList.contains(expr))
						newList.add(expr);
				}
			}
			list.getItems().clear();
			list.getItems().addAll(newList);
			return list;
		}
		return null;
	}

	@Builtin(Name = "except")
	public static RuleList except(final Argument arg) {
		if (arg.arguments.size() == 2
				&& arg.arguments.get(0) instanceof RuleList
				&& arg.arguments.get(1) instanceof RuleList) {
			final RuleList list = (RuleList) arg.arguments.get(0);
			final RuleList listE = (RuleList) arg.arguments.get(1);
			for (final IExpression expr : listE.getItems())
				list.getItems().remove(expr);
			return list;
		}
		return null;
	}
}
