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
package lupos.misc;

import java.util.Date;
public class TimeInterval {

	private final long difference;
	
	private final static long SECONDS_IN_MILLISECONDS = 1000;
	private final static long MINUTES_IN_MILLISECONDS = 60 * SECONDS_IN_MILLISECONDS;
	private final static long HOURS_IN_MILLISECONDS = 60 * MINUTES_IN_MILLISECONDS;
	private final static long DAYS_IN_MILLISECONDS = 24 * HOURS_IN_MILLISECONDS;
	private final static long WEEKS_IN_MILLISECONDS = 7 * DAYS_IN_MILLISECONDS;

	/**
	 * <p>Constructor for TimeInterval.</p>
	 *
	 * @param difference a long.
	 */
	public TimeInterval(final long difference){
		this.difference = difference;
	}
	
	
	/**
	 * <p>Constructor for TimeInterval.</p>
	 *
	 * @param start a long.
	 * @param end a long.
	 */
	public TimeInterval(final long start, final long end){
		this(end - start);
	}
	
	/**
	 * <p>Constructor for TimeInterval.</p>
	 *
	 * @param start a {@link java.util.Date} object.
	 * @param end a {@link java.util.Date} object.
	 */
	public TimeInterval(final Date start, final Date end){
		this(start.getTime(), end.getTime());
	}
	
	/**
	 * <p>getMilliseconds.</p>
	 *
	 * @return a long.
	 */
	public long getMilliseconds(){
		return this.difference;
	}
	
	/**
	 * <p>getSeconds.</p>
	 *
	 * @return a long.
	 */
	public long getSeconds(){
		return TimeInterval.getSeconds(this.difference);
	}
	
	/**
	 * <p>getMinutes.</p>
	 *
	 * @return a long.
	 */
	public long getMinutes(){
		return TimeInterval.getMinutes(this.difference);
	}
	
	/**
	 * <p>getHours.</p>
	 *
	 * @return a long.
	 */
	public long getHours(){
		return TimeInterval.getHours(this.difference);
	}
	
	/**
	 * <p>getDays.</p>
	 *
	 * @return a long.
	 */
	public long getDays(){
		return TimeInterval.getDays(this.difference);
	}
	
	/**
	 * <p>getWeeks.</p>
	 *
	 * @return a long.
	 */
	public long getWeeks(){
		return TimeInterval.getWeeks(this.difference);
	}

	/**
	 * <p>getSeconds.</p>
	 *
	 * @param difference a long.
	 * @return a long.
	 */
	public static long getSeconds(final long difference){
		return difference / SECONDS_IN_MILLISECONDS;
	}
	
	/**
	 * <p>getMinutes.</p>
	 *
	 * @param difference a long.
	 * @return a long.
	 */
	public static long getMinutes(final long difference){
		return difference / MINUTES_IN_MILLISECONDS;
	}
	
	/**
	 * <p>getHours.</p>
	 *
	 * @param difference a long.
	 * @return a long.
	 */
	public static long getHours(final long difference){
		return difference / HOURS_IN_MILLISECONDS;
	}
	
	/**
	 * <p>getDays.</p>
	 *
	 * @param difference a long.
	 * @return a long.
	 */
	public static long getDays(final long difference){
		return difference / DAYS_IN_MILLISECONDS;
	}
	
	/**
	 * <p>getWeeks.</p>
	 *
	 * @param difference a long.
	 * @return a long.
	 */
	public static long getWeeks(final long difference){
		return difference / WEEKS_IN_MILLISECONDS;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString(){
		return TimeInterval.toString(this.difference);
	}
	
	/**
	 * <p>toString.</p>
	 *
	 * @param difference a long.
	 * @return a {@link java.lang.String} object.
	 */
	public static String toString(final long difference){
		Tuple<String, Long> result1 = TimeInterval.addString(difference, TimeInterval.getWeeks(difference), WEEKS_IN_MILLISECONDS, "week", "");
		Tuple<String, Long> result2 = TimeInterval.addString(result1.getSecond(), TimeInterval.getDays(result1.getSecond()), DAYS_IN_MILLISECONDS, "day", result1.getFirst());
		Tuple<String, Long> result3 = TimeInterval.addString(result2.getSecond(), TimeInterval.getHours(result2.getSecond()), HOURS_IN_MILLISECONDS, "hour", result2.getFirst());
		Tuple<String, Long> result4 = TimeInterval.addString(result3.getSecond(), TimeInterval.getMinutes(result3.getSecond()), MINUTES_IN_MILLISECONDS, "minute", result3.getFirst());
		Tuple<String, Long> result5 = TimeInterval.addString(result4.getSecond(), TimeInterval.getSeconds(result4.getSecond()), SECONDS_IN_MILLISECONDS, "second",  result4.getFirst());
		Tuple<String, Long> result6 = TimeInterval.addString(result5.getSecond(), result5.getSecond(), 1, "millisecond",  result5.getFirst());
		return result6.getFirst();
	}
	
	private static Tuple<String, Long> addString(final long remainingDifference, final long times, final long factor, final String intervalComponent, final String currentString){
		String resultantString = currentString;
		long resultantDifference = remainingDifference;
		if(times>0 || resultantString.length()>0){
			if(resultantString.length()>0){
				resultantString += " ";
			}
			resultantString += times + " " + intervalComponent;
			if(times>1){
				resultantString += "s";
			}
			resultantDifference = resultantDifference - times * factor; 
		}
		return new Tuple<String, Long>(resultantString, resultantDifference);
	}
}
