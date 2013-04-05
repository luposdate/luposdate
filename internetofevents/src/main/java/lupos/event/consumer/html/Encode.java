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
package lupos.event.consumer.html;

/**
 * Class for handling the functions of Templates by Strings.
 */
public class Encode {

	public final static String VARNOTATION = "?";
	public final static String START = "%-";
	public final static String END = "-%";
	public final static String FORSTART = START + "FOR" + END;
	public final static String FOREND = START + "ENDFOR" + END;
	public final static String CONTENT = "CONTENT";
	public final static String ESCAPE = "ESCAPE";
	public final static String PREDICATE = "PREDICATE";
 	public final static String REFRESH = "REFRESH(time in seconds)";
	public final static String CHARTSTART = START + "CHART" + END;
 	public final static String CHARTEND = START + "ENDCHART" + END; 	
 	public final static String OPTIONS = START + "OPTIONS";
 	public final static String LEGEND = START + "LEGEND" + END;
 	public final static String IMAGE = START + "IMAGE" + END;

	/**
	 * Creates the String for the Content function.
	 * 
	 * @param varName name of the variable to get content of
	 * @return the construct of the content
	 */
	public static String CONTENT_OF(String varName) {
		return START + CONTENT + "(?" + varName + ")" + END;
	}
	
	/**
	 * Creates the String for the Predicate function.
	 * 
	 * @param varName name of the variable to get predicate of
	 * @return the construct of the content
	 */
	public static String PREDICATE_OF(String varName) {
		return START + PREDICATE + "(?" + varName + ")" + END;
	}	
	
	/**
	 * Creates the String for escape declaration.
	 * 
	 * @param varName name of the variable for expression
	 * @return the construct of escape declaration
	 */
	public static String ESCAPE_OF(String varName) {
		return START + ESCAPE + "(?" + varName + ")" + END;
	}

	/**
	 * Creates the String for variable definition.
	 * 
	 * @param varName name of the variable for definition
	 * @return the construct of variable definition
	 */
	public static String VAR(String varName) {
		return START + "(" + VARNOTATION + varName + ")" + END;
	}

	/**
	 * Creates the String for the for function.
	 * 
	 * @param content name the inner content between the for operation
	 * @return the construct of for function
	 */
	public static String FOR_OF(String content) {
		return FORSTART + "\n" + content + "\n" + FOREND;
	}

	/**
	 * Creates the String for the regular content.
	 * 
	 * @param varName name of the regular variable to get content of
	 * @return the regular construct of the content 
	 */
	public static String REG_CONTENT_OF(String varName) {
		return START + CONTENT + "\\(\\" + VARNOTATION + varName + "\\)" + END;
	}
		
	/**
	 * Creates the String for escape regular expression.
	 * 
	 * @param varName name of the variable for expression
	 * @return the construct of escape regular expression
	 */
	public static String REG_ESCAPE_OF(String varName) {
		return START + ESCAPE + "\\(\\" + VARNOTATION + varName + "\\)" + END;
	}

	/**
	 * Creates the regular expression String for the variable .
	 * 
	 * @param varName name of the variable
	 * @return the regular expression construct of variable
	 */
	public static String REG_VAR(String varName) {
		return START + "\\" + VARNOTATION + varName + END;
	}
	
	/**
	 * Creates the String for predicate regular expression.
	 * 
	 * @param varName name of the variable for expression
	 * @return the regular construct of the predicate
	 */
	public static String REG_PREDICATE_OF(String varName){
		return START + PREDICATE + "\\(\\" + VARNOTATION + varName + "\\)" + END;
	}
	
}
