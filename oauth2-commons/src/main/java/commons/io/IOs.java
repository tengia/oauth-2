/* 
 * Copyright (c) 2017 Georgi Pavlov (georgi.pavlov@isoft-technology.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT license which accompanies 
 * this distribution, and is available at 
 * https://github.com/tengia/oauth-2/blob/master/LICENSE
 */
package commons.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class IOs {

	static final int DEFAULT_BUFFER_SIZE = 1024;

	static int pipe(Reader in, Writer out, long limit) throws IOException {
		char[] buffer = new char[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = in.read(buffer)) && count <= limit) {
			out.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	static int pipe(InputStream in, OutputStream out, long limit) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = in.read(buffer)) && count <= limit) {
			out.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	public static String consume(InputStream stream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		StringWriter out = new StringWriter();
		pipe(in, out, Integer.MAX_VALUE);
		String contents = out.toString();
		return contents;
	}

	public static int produce(OutputStream stream, String payload) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
		int count = pipe(in, stream, Integer.MAX_VALUE);
		return count;
	}

}
