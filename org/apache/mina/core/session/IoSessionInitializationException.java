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
package org.apache.mina.core.session;

/**
 * A {@link RuntimeException} that is thrown when the initialization of
 * an {@link IoSession} fails.
 * 
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 671827 $, $Date: 2008-06-26 10:49:48 +0200 (jeu, 26 jun 2008) $
 */
public class IoSessionInitializationException extends RuntimeException {
    private static final long serialVersionUID = -1205810145763696189L;

    public IoSessionInitializationException() {
        super();
    }

    public IoSessionInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IoSessionInitializationException(String message) {
        super(message);
    }

    public IoSessionInitializationException(Throwable cause) {
        super(cause);
    }
}
