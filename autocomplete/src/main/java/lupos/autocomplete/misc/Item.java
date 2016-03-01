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
package lupos.autocomplete.misc;
public class Item {
	
	String value;
	boolean caseSensitiv;
	//Konstruktor
	/**
	 * <p>Constructor for Item.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 * @param caseSensitiv a boolean.
	 */
	public Item(String value, boolean caseSensitiv){
		this.value = value;
		this.caseSensitiv = caseSensitiv;
	}
	
	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 */
	public void setValue(String value){
		this.value = value;
	}
	/**
	 * <p>Setter for the field <code>caseSensitiv</code>.</p>
	 *
	 * @param caseSensitiv a boolean.
	 */
	public void setCaseSensitiv(boolean caseSensitiv){
		this.caseSensitiv = caseSensitiv;
	}
	
	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getValue(){
		return value;
	}
	/**
	 * <p>Getter for the field <code>caseSensitiv</code>.</p>
	 *
	 * @return a boolean.
	 */
	public boolean getCaseSensitiv(){
		return caseSensitiv;
	}
	
	
	/*
	 * Veraenderte equals Methode:
	 * wenn caseSensitiv == false wird mit equalsIgnoreCase verglichen
	 */
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj){
			if (obj == null) {
				return false;
			}
			
			if (caseSensitiv ==((Item)obj).caseSensitiv == true) {
				if((obj instanceof Item)&&(((Item)obj).value.equals(this.value))){
					return true;
				}
			}else {
				if((obj instanceof Item)&&(((Item)obj).value.equalsIgnoreCase(this.value))){
					return true;
				}
			}
			return false;
			
	}
	/*
	 * angepasster hashcode mit toLowerCase
	 */
	/** {@inheritDoc} */
	@Override
	public int hashCode(){
		return this.value.toLowerCase().hashCode();
	}
	

}
