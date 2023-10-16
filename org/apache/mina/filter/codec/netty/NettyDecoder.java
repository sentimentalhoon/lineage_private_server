/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.mina.filter.codec.netty;

import java.nio.ByteBuffer;

import net.gleamynode.netty2.Message;
import net.gleamynode.netty2.MessageParseException;
import net.gleamynode.netty2.MessageRecognizer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * A MINA {@link ProtocolDecoder} that decodes buffers into
 * Netty2 {@link Message}s using specified {@link MessageRecognizer}s.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 671827 $, $Date: 2008-06-26 10:49:48 +0200 (jeu, 26 jun 2008) $,
 */
public class NettyDecoder extends ProtocolDecoderAdapter {
    private final MessageRecognizer recognizer;

    private ByteBuffer readBuf = ByteBuffer.allocate(1024);

    private Message readingMessage;

    /**
     * Creates a new instance with the specified {@link MessageRecognizer}.
     */
    public NettyDecoder(MessageRecognizer recognizer) {
        if (recognizer == null) {
            throw new NullPointerException();
        }

        this.recognizer = recognizer;
    }

    private void put(IoBuffer in) {
        // copy to read buffer
        if (in.remaining() > readBuf.remaining()) {
            expand((readBuf.position() + in.remaining()) * 3 / 2);
        }
        readBuf.put(in.buf());
    }

    private void expand(int newCapacity) {
        ByteBuffer newBuf = ByteBuffer.allocate(newCapacity);
        readBuf.flip();
        newBuf.put(readBuf);
        readBuf = newBuf;
    }

    public void decode(IoSession session, IoBuffer in,
            ProtocolDecoderOutput out) throws Exception {
        put(in);

        Message m = readingMessage;
        try {
            for (;;) {
                readBuf.flip();
                if (m == null) {
                    int limit = readBuf.limit();
                    boolean failed = true;
                    try {
                        m = recognizer.recognize(readBuf);
                        failed = false;
                    } finally {
                        if (failed) {
                            // clear the read buffer if failed to recognize
                            readBuf.clear();
                            break;
                        } else {
                            if (m == null) {
                                readBuf.limit(readBuf.capacity());
                                readBuf.position(limit);
                                break; // finish decoding
                            } else {
                                // reset buffer for read
                                readBuf.limit(limit);
                                readBuf.position(0);
                            }
                        }
                    }
                }

                if (m != null) {
                    try {
                        if (m.read(readBuf)) {
                            out.write(m);
                            m = null;
                        } else {
                            break;
                        }
                    } finally {
                        if (readBuf.hasRemaining()) {
                            readBuf.compact();
                        } else {
                            readBuf.clear();
                            break;
                        }
                    }
                }
            }
        } catch (MessageParseException e) {
            m = null; // discard reading message
            throw new ProtocolDecoderException("Failed to decode.", e);
        } finally {
            readingMessage = m;
        }
    }
}
