/**
 * Copyright 2010 Eric Taix
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 */
package org.jared.synodroid.ds;

/**
 * As usual a utility class
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class Utils {

	
	/**
	 * Compute the time left in the following format d h m s
	 * 
	 * @param etaP
	 * @return
	 */
	public static String computeTimeLeft(int etaP) {
		String result = "";
		// Only if time left is known
		if (etaP != -1) {
			// Days
			int d = etaP / (60 * 60 * 24);
			if (d > 0) {
				result += d + "d ";
				etaP -= d * (60 * 60 * 24);
			}
			// Hours
			int h = etaP / (60 * 60);
			if (h > 0 || d > 0) {
				result += h + "h ";
				etaP -= h * (60 * 60);
			}
			// Minutes
			int m = etaP / 60;
			if (m > 0 || h > 0 || m > 0) {
				result += m + "m ";
				etaP -= m * 60;
			}
			// Secondes
			result += etaP + "s";
		}
		return result;
	}

}
