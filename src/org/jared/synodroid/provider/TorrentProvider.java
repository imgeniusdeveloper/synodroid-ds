/**
 * 
 */
package org.jared.synodroid.provider;

import java.util.Random;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * @author Eric Taix
 */
public class TorrentProvider extends ContentProvider {

  private static Random r = new Random(System.currentTimeMillis());

  public static final String PROVIDER_NAME = "org.transdroid.provider.torrents";

  /** The content URI to use. Useful if the application have access to this class. Otherwise it must build the URI like<br/>
   <code>Uri uri = Uri.parse("content://org.transdroid.provider.torrents/search/eric%20Taix");</code><br/>
   And within an activity then call:<br/>
   <code>Cursor cur = managedQuery(uri, null, null, null, null);</code>
   **/
  public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/search");

  private static final int SEARCH_TERM = 1;

  // Static intialization of the URI matcher
  private static final UriMatcher uriMatcher;
  static {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(PROVIDER_NAME, "search/*", SEARCH_TERM);
  }

  /*
   * (non-Javadoc)
   * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
   */
  @Override
  public int delete(Uri uriP, String selectionP, String[] selectionArgsP) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see android.content.ContentProvider#getType(android.net.Uri)
   */
  @Override
  public String getType(Uri uriP) {
    switch (uriMatcher.match(uriP)) {
      case SEARCH_TERM:
        return "vnd.android.cursor.dir/vnd.transdroid.torrent";
      default:
        throw new IllegalArgumentException("Unsupported URI: " + uriP);
    }
  }

  /*
   * (non-Javadoc)
   * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
   */
  @Override
  public Uri insert(Uri uriP, ContentValues valuesP) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see android.content.ContentProvider#onCreate()
   */
  @Override
  public boolean onCreate() {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String,
   * java.lang.String[], java.lang.String)
   */
  @Override
  public Cursor query(Uri uriP, String[] projectionP, String selectionP, String[] selectionArgsP, String sortOrderP) {
    String[] columnNames = new String[] { "NAME", "URL", "SEEDERS", "LEECHERS", "SIZE" };
    MatrixCursor curs = new MatrixCursor(columnNames);
    // Retrieve the search term
    if (uriMatcher.match(uriP) == SEARCH_TERM) {
      String term = uriP.getPathSegments().get(1);

      //------- Mock code to simulate the final request -----
      String[] linux = new String[] { "Ubuntu", "Mac OSX ", "Fedora " };
      int nb = r.nextInt(100) + 1;
      for (int tLoop = 0; tLoop < nb; tLoop++) {
        Object[] values = new Object[5];
        values[0] = "" + linux[r.nextInt(3)] + "v" + (r.nextInt(10) + 1) + ".0 ("+term+")";
        values[1] = "http://linux.tracker.org/id=1234585";
        values[2] = "" + r.nextInt(50);
        values[3] = "" + r.nextInt(500);
        values[4] = "" + (r.nextInt(500) + 500) + " Ko";
        curs.addRow(values);
      }
      //-------- End of mock code ---------
    }
    // Register to watch a content URI for changes (don't really know what it means ?)
    curs.setNotificationUri(getContext().getContentResolver(), uriP);
    return curs;
  }

  /*
   * (non-Javadoc)
   * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String,
   * java.lang.String[])
   */
  @Override
  public int update(Uri uriP, ContentValues valuesP, String selectionP, String[] selectionArgsP) {
    throw new UnsupportedOperationException();
  }

}
