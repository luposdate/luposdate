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
package lupos.event.producer.ebay;

/**
 * Parser for durations based on the ISO 8601.
 */
public class Duration {
	
	/**
	 * Number of years
	 */
	private int years = 0;
	
	/**
	 * Number of months
	 */
	private int months = 0;
	
	/**
	 * Number of weeks
	 */
	private int weeks = 0;
	
	/**
	 * Number of days
	 */
	private int days = 0;
	
	/**
	 * Number of hours
	 */
	private int hours = 0;
	
	/**
	 * Number of minutes
	 */
	private int minutes = 0;
	
	/**
	 * Number of seconds
	 */
	private int seconds = 0;
	
	/**
	 * Parses the given string and stores the retrieved values in the corresponding properties
	 * 
	 * @param	duration	Duration string that fulfills the syntax structure given by ISO 8601
	 */
	public Duration(String duration) {
		int len = duration.length();
		int currentValue = 0;
		boolean seperator = false;
		
		// Parses the given string byte wise
		for (int i = 0; i < len; i++) {
			char c = duration.charAt(i);
			
			// Implements the separation of values based on the syntax of ISO 8601
			switch(c) {
			case 'P':
				currentValue = 0;
				break;
			case 'T':
				seperator = true;
				break;
			case 'Y':
				this.years = currentValue;
				currentValue = 0;
				break;
			case 'M': 
				if (seperator) {
					this.minutes = currentValue;
				}
				else {
					this.months = currentValue;
				}
				currentValue = 0;
				break;
			case 'W':
				this.weeks = currentValue;
				currentValue = 0;
				break;
			case 'D':
				this.days = currentValue;
				currentValue = 0;
				break;
			case 'H': 
				this.hours = currentValue;
				currentValue = 0;
				break;
			case 'S': 
				this.seconds = currentValue;
				currentValue = 0;
				break;
			case '\n':
				break;
			default: 
				try {
					currentValue *= 10;
					currentValue += Integer.parseInt(""+c);
				}
				catch (NumberFormatException e) {
					System.err.println("Wrong character found: "+c);
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
	}

	/**
	 * Returns the number of years
	 * 
	 * @return	Number of years
	 */
	public int getYears() {
		return this.years;
	}

	/**
	 * Returns the number of months
	 * 
	 * @return	Number of months
	 */
	public int getMonths() {
		return this.months;
	}

	/**
	 * Returns the number of weeks
	 * 
	 * @return	Number of weeks
	 */
	public int getWeeks() {
		return this.weeks;
	}

	/**
	 * Returns the number of days
	 * 
	 * @return	Number of days
	 */
	public int getDays() {
		return this.days;
	}

	/**
	 * Returns the number of hours
	 * 
	 * @return	Number of hours
	 */
	public int getHours() {
		return this.hours;
	}

	/**
	 * Returns the number of minutes
	 * 
	 * @return	Number of minutes
	 */
	public int getMinutes() {
		return this.minutes;
	}

	/**
	 * Returns the number of seconds
	 * 
	 * @return	Number of seconds
	 */
	public int getSeconds() {
		return this.seconds;
	}

	/**
	 * Returns the number of seconds this duration totally longs.
	 * 
	 * @return	The total number of seconds this duration longs, or <strong>-1</strong>, if this
	 * 			duration includes whole weeks, months or years
	 */
	public int toTimestamp() {
		int secs = -1;
		
		if (this.years + this.months + this.weeks == 0) {
			secs = ((this.days * 24 + this.hours) * 60 + this.minutes) * 60 + this.seconds;
		}
		
		return secs;
	}
	
	/**
	 * Returns the duration values concatinated to a legible string together with their units.
	 * 
	 * @return	The duration values concatinated to a legible string
	 */
	public String toLegibleString() {
		/* Weeks and years are ignored in this method because eBay does
		 * not deliver values for anything "higher" than days.
		 */
		return new StringBuilder()
				.append(this.days).append(" days, ")
				.append(this.hours).append(" hours, ")
				.append(this.minutes).append(" minutes, ")
				.append(this.seconds).append(" seconds")
				.toString();
	}

	@Override
	public String toString() {
		return toTimestamp() + "";
	}
	
}