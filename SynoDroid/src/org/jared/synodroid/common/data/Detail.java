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
package org.jared.synodroid.common.data;

import java.io.Serializable;

/**
 * A detail which consist in a name/value pair
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class Detail implements Serializable {

  private static final long serialVersionUID = 1L;

  // The name of this detail
	public String name;
	// The value of this detail
	public String value;
}
