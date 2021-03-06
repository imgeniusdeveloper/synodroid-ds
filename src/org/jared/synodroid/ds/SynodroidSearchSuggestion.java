package org.jared.synodroid.ds;

import android.content.SearchRecentSuggestionsProvider;

public class SynodroidSearchSuggestion extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "org.jared.synodroid.SynodroidSearchSuggestion";
	public final static int MODE = DATABASE_MODE_QUERIES;

	public SynodroidSearchSuggestion() {
		setupSuggestions(AUTHORITY, MODE);
	}
}
