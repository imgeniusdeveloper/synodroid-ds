/**
 * 
 */
package org.jared.synodroid.common.protocol;

import java.io.File;
import java.io.FileInputStream;

import android.net.Uri;

/**
 * An utility class which generates a stream from an Uri
 * @author Eric Taix (eric.taix at gmail.com)
 */
public class StreamFactory {

	/**
	 * Return the stream according to the Uri
	 * @param uriP
	 * @return
	 * @throws Exception
	 */
	public static byte[] getStream(Uri uriP) throws Exception {
		String path = uriP.getPath();
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);

		int maxBufferSize = 1 * 1024 * 1024;
		int bytesAvailable = fis.available();
		int bufferSize = Math.min(bytesAvailable, maxBufferSize);
		byte[] buffer = new byte[bufferSize];
		fis.read(buffer, 0, bufferSize);
		fis.close();
		return buffer;
	}
	
}
