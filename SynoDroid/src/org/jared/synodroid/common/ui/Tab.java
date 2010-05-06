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
package org.jared.synodroid.common.ui;

/**
 * A tab instance
 * @author Eric Taix
 */
public class Tab {

  // The tab' unique id
  private String id;;
  // The normal icon id
  private int iconNormal;
  // The selected icon id
  private int iconSelected;
  
  /**
   * The minimal constructor
   * @param idP
   */
  public Tab(String idP) {
    id = idP;
  }

  /**
   * The constructor which initialize the label and the icon
   * @param idP
   * @param label
   * @param iconP
   */
  public Tab(String idP, int iconNormalP, int iconSelectedP) {
    this(idP);
    iconNormal = iconNormalP;
    iconSelected = iconSelectedP;
  }
  
  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param idP the id to set
   */
  public void setId(String idP) {
    id = idP;
  }

  /**
   * @return the iconNormal
   */
  public int getIconNormal() {
    return iconNormal;
  }

  /**
   * @param iconNormalP the iconNormal to set
   */
  public void setIconNormal(int iconNormalP) {
    iconNormal = iconNormalP;
  }

  /**
   * @return the iconSelected
   */
  public int getIconSelected() {
    return iconSelected;
  }

  /**
   * @param iconSelectedP the iconSelected to set
   */
  public void setIconSelected(int iconSelectedP) {
    iconSelected = iconSelectedP;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Tab other = (Tab) obj;
    if (id == null) {
      if (other.id != null) return false;
    }
    else if (!id.equals(other.id)) return false;
    return true;
  }
  
  
  
}
