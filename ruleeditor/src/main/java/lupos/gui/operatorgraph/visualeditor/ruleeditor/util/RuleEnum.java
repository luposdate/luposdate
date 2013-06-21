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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.util;

public enum RuleEnum {
	Operator(lupos.engine.operators.BasicOperator.class),
	Join(lupos.engine.operators.multiinput.join.Join.class),
	HashMapIndexJoin(lupos.engine.operators.multiinput.join.HashMapIndexJoin.class),
	HashMapIndexOnLeftOperandJoin(lupos.engine.operators.multiinput.join.HashMapIndexOnLeftOperandJoin.class),
	Filter(lupos.engine.operators.singleinput.filter.Filter.class),
	RuleFilter(lupos.rif.operator.RuleFilter.class),
	Optional(lupos.engine.operators.multiinput.optional.Optional.class),
	UsingJoinOptional(lupos.engine.operators.multiinput.optional.UsingJoinOptional.class),
	BasicIndexOptional(lupos.engine.operators.multiinput.optional.BasicIndexScanOptional.class),
	Union(lupos.engine.operators.multiinput.Union.class),
	Index(lupos.engine.operators.index.BasicIndexScan.class),
	MemoryIndex(lupos.engine.operators.index.memoryindex.MemoryIndexScan.class),
	TriplePattern(lupos.engine.operators.tripleoperator.TriplePattern.class),
	PredicatePattern(lupos.rif.operator.PredicatePattern.class),
	AddBinding(lupos.engine.operators.singleinput.AddBinding.class),
	AddBindingFromOtherVar(lupos.engine.operators.singleinput.AddBindingFromOtherVar.class),
	Construct(lupos.engine.operators.singleinput.Construct.class),
	ConstructPredicate(lupos.rif.operator.ConstructPredicate.class),
	Generate(lupos.engine.operators.singleinput.generate.Generate.class),
	Distinct(lupos.engine.operators.singleinput.modifiers.distinct.Distinct.class),
	HashSetNonBlockingDistinctWithIndexAccess(lupos.engine.operators.singleinput.modifiers.distinct.HashSetNonBlockingDistinctWithIndexAccess.class), 
	ReplaceVar(lupos.engine.operators.singleinput.ReplaceVar.class),
	Projection(lupos.engine.operators.singleinput.Projection.class),
	Sort(lupos.engine.operators.singleinput.sort.Sort.class),
	Limit(lupos.engine.operators.singleinput.modifiers.Limit.class),
	Offset(lupos.engine.operators.singleinput.modifiers.Offset.class),
	SortLimit(lupos.engine.operators.singleinput.modifiers.SortLimit.class);

	private Class<? extends lupos.engine.operators.BasicOperator> clazz;

	private RuleEnum(Class<? extends lupos.engine.operators.BasicOperator> clazz) {
		this.clazz = clazz;
	}

	public Class<? extends lupos.engine.operators.BasicOperator> getOpClass() {
		return this.clazz;
	}
}