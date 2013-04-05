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
/* Generated By:JJTree&JavaCC: Do not edit this line. TSVParserConstants.java */
package lupos.endpoint.client.formatreader.tsv;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface TSVParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int IRI_REF = 4;
  /** RegularExpression Id. */
  int VARNAME = 5;
  /** RegularExpression Id. */
  int PNAME_NS = 6;
  /** RegularExpression Id. */
  int PNAME_LN = 7;
  /** RegularExpression Id. */
  int BLANK_NODE_LABEL = 8;
  /** RegularExpression Id. */
  int VAR = 9;
  /** RegularExpression Id. */
  int VAR1 = 10;
  /** RegularExpression Id. */
  int VAR2 = 11;
  /** RegularExpression Id. */
  int LANGTAG = 12;
  /** RegularExpression Id. */
  int INTEGER = 13;
  /** RegularExpression Id. */
  int DECIMAL = 14;
  /** RegularExpression Id. */
  int DOUBLE = 15;
  /** RegularExpression Id. */
  int EXPONENT = 16;
  /** RegularExpression Id. */
  int STRING_LITERAL1 = 17;
  /** RegularExpression Id. */
  int STRING_LITERAL2 = 18;
  /** RegularExpression Id. */
  int STRING_LITERAL_LONG1 = 19;
  /** RegularExpression Id. */
  int STRING_LITERAL_LONG2 = 20;
  /** RegularExpression Id. */
  int NIL = 21;
  /** RegularExpression Id. */
  int WS = 22;
  /** RegularExpression Id. */
  int ANON = 23;
  /** RegularExpression Id. */
  int PN_CHARS_BASE = 24;
  /** RegularExpression Id. */
  int PN_CHARS_U = 25;
  /** RegularExpression Id. */
  int PN_CHARS = 26;
  /** RegularExpression Id. */
  int PN_PREFIX = 27;
  /** RegularExpression Id. */
  int PN_LOCAL = 28;
  /** RegularExpression Id. */
  int PLX = 29;
  /** RegularExpression Id. */
  int PERCENT = 30;
  /** RegularExpression Id. */
  int HEX = 31;
  /** RegularExpression Id. */
  int PN_LOCAL_ESC = 32;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\r\"",
    "<token of kind 3>",
    "<IRI_REF>",
    "<VARNAME>",
    "<PNAME_NS>",
    "<PNAME_LN>",
    "<BLANK_NODE_LABEL>",
    "<VAR>",
    "<VAR1>",
    "<VAR2>",
    "<LANGTAG>",
    "<INTEGER>",
    "<DECIMAL>",
    "<DOUBLE>",
    "<EXPONENT>",
    "<STRING_LITERAL1>",
    "<STRING_LITERAL2>",
    "<STRING_LITERAL_LONG1>",
    "<STRING_LITERAL_LONG2>",
    "<NIL>",
    "<WS>",
    "<ANON>",
    "<PN_CHARS_BASE>",
    "<PN_CHARS_U>",
    "<PN_CHARS>",
    "<PN_PREFIX>",
    "<PN_LOCAL>",
    "<PLX>",
    "<PERCENT>",
    "<HEX>",
    "<PN_LOCAL_ESC>",
    "\"\\n\"",
    "\"\\t\"",
    "\"^^\"",
    "\"+\"",
    "\"-\"",
    "\"true\"",
    "\"false\"",
  };

}
