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

/**
 * Here are defined all torrent'status
 * @author Eric Taix (eric.taix at gmail.com)
 */
public enum TaskStatus {
  TASK_WAITING, TASK_DOWNLOADING, TASK_SEEDING, TASK_PAUSED, TASK_FINISHED, TASK_HASH_CHECKING
}