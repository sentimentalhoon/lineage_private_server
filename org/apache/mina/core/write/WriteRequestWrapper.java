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
package org.apache.mina.core.write;

import java.net.SocketAddress;

import org.apache.mina.core.future.WriteFuture;

/**
 * A wrapper for an existing {@link WriteRequest}.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 671827 $, $Date: 2008-06-26 10:49:48 +0200 (jeu, 26 jun 2008) $
 */
public class WriteRequestWrapper implements WriteRequest {

    private final WriteRequest parentRequest;

    /**
     * Creates a new instance that wraps the specified request.
     */
    public WriteRequestWrapper(WriteRequest parentRequest) {
        if (parentRequest == null) {
            throw new NullPointerException("parentRequest");
        }
        this.parentRequest = parentRequest;
    }

    public SocketAddress getDestination() {
        return parentRequest.getDestination();
    }

    public WriteFuture getFuture() {
        return parentRequest.getFuture();
    }

    public Object getMessage() {
        return parentRequest.getMessage();
    }

    public WriteRequest getOriginalRequest() {
        return parentRequest.getOriginalRequest();
    }

    /**
     * Returns the wrapped request object.
     */
    public WriteRequest getParentRequest() {
        return parentRequest;
    }

    @Override
    public String toString() {
        if (getDestination() == null) {
            return getMessage().toString();
        } else {
            return getMessage().toString() + " => " + getDestination();
        }
    }
}
