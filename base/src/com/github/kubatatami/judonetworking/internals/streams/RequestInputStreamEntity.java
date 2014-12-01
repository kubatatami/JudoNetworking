/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.github.kubatatami.judonetworking.internals.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A streamed, non-repeatable entity that obtains its content from
 * an {@link InputStream}.
 */
public class RequestInputStreamEntity {

    private final static long BUFFER_SIZE = 4096;

    private final InputStream content;
    private final long length;
    byte[] buffer;

    public RequestInputStreamEntity(final InputStream instream, long length) {
        if (instream == null) {
            throw new IllegalArgumentException("Source input stream may not be null");
        }
        this.content = instream;
        this.length = length;
        buffer = new byte[(int) Math.min(BUFFER_SIZE,length)];
    }

    public long getContentLength() {
        return this.length;
    }

    public void writeTo(final OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream instream = this.content;

        int l;
        if (this.length < 0) {
            // consume until EOF
            while ((l = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, l);
            }
        } else {
            // consume no more than length
            long remaining = this.length;
            while (remaining > 0) {
                l = instream.read(buffer, 0, (int) Math.min(BUFFER_SIZE, remaining));
                if (l == -1) {
                    break;
                }
                outstream.write(buffer, 0, l);
                remaining -= l;
            }
        }
    }

    // non-javadoc, see interface HttpEntity
    public void close() throws IOException {
        this.content.close();
    }

    public void reset() throws IOException {
        this.content.reset();
    }

    public InputStream getContent() {
        return content;
    }
} // class InputStreamEntity