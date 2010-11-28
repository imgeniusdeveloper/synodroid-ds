/**
 * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.jared.synodroid.common.preference;

import org.jared.synodroid.ds.R;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * A preference which shows the current value
 * 
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class ListPreferenceWithValue extends ListPreference implements PreferenceWithValue {

  private TextView value;
  // The current value
  private String currentValue;
  // External OnPreferenceChangeListener
  private OnPreferenceChangeListener listener = null;

  /**
   * Constructor
   * 
   * @param context
   * @param attrs
   */
  public ListPreferenceWithValue(Context context, AttributeSet attrs) {
    super(context, attrs);
    setLayoutResource(R.layout.preference_with_value);
    initInternalChangeListener();
  }

  /**
   * Constructor
   * 
   * @param context
   */
  public ListPreferenceWithValue(Context context) {
    super(context);
    setLayoutResource(R.layout.preference_with_value);
    initInternalChangeListener();
  }

  /**
   * Binds the view to the data for this preference
   */
  @Override
  protected void onBindView(View view) {
    super.onBindView(view);
    value = (TextView) view.findViewById(R.id.preference_value);
    if (value != null) {
      String v = getValue();
      int index = findIndexOfValue(v);
      if (index != -1) {
        value.setText(getEntries()[index]);
      }
      currentValue = getValue();
    }
  }

  /**
   * Init the internal listener to be able to update the new value
   */
  private void initInternalChangeListener() {
    // Call the super method as we overrided it
    super.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        int index = findIndexOfValue(newValue.toString());
        if (index != -1) {
          value.setText(getEntries()[index]);
          currentValue = value.getText().toString();
        }
        // By default update the preference's state
        boolean result = true;
        // If exist call the listener
        if (listener != null) {
          result = listener.onPreferenceChange(preference, newValue);
        }
        return result;
      }
    });
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.preference.PreferenceWithValue#getPrintableValue()
   */
  public String getPrintableValue() {
	  return getCurrentValue();
  }

	/*
   * (non-Javadoc)
   * @see android.preference.Preference#setOnPreferenceChangeListener(android.preference
   * .Preference.OnPreferenceChangeListener)
   */
  @Override
  public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
    listener = onPreferenceChangeListener;
  }

  /**
   * Return the current value. This value is NOT the value stored in the SharedPreference but the current value in the
   * view editor of this preference. Useful when you want to retrieve the new value before the preference is updated
   * 
   * @return
   */
  public String getCurrentValue() {
    String result = currentValue;
    // If not set then return the current state
    if (result == null) {
      result = getValue();
    }
    return currentValue;
  }

  /*
   * (non-Javadoc)
   * @see android.preference.ListPreference#setValue(java.lang.String)
   */
  @Override
  public void setValue(String value) {
    super.setValue(value);
  }

  /*
   * (non-Javadoc)
   * @see android.preference.ListPreference#setEntryValues(java.lang.CharSequence[])
   */
  @Override
  public void setEntryValues(CharSequence[] entryValuesP) {
    super.setEntryValues(entryValuesP);
    // Set the default value
    if (entryValuesP != null && entryValuesP.length > 0) {
      setValue(entryValuesP[0].toString());
    }
  }

  /**
   * Convenient method to create an instance of EditTextPreference
   * 
   * @param keyP
   * @param titleP
   * @param summaryP
   * @return
   */
  public static ListPreferenceWithValue create(Context contextP, String keyP, int titleP, int summaryP,
          String[] versions) {
    ListPreferenceWithValue pref = new ListPreferenceWithValue(contextP);
    pref.setKey(keyP);
    pref.setTitle(titleP);
    pref.setSummary(summaryP);
    pref.setDialogTitle(titleP);
    if (versions != null && versions.length > 0) {
      pref.setEntries(versions);
      pref.setEntryValues(versions);
      pref.setValue(versions[0]);
    }
    return pref;
  }

}
