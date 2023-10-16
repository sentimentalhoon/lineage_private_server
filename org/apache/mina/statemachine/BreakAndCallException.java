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
package org.apache.mina.statemachine;

/**
 * Exception used internally by {@link StateControl}.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 588579 $, $Date: 2007-10-26 11:21:01 +0200 (ven, 26 oct 2007) $
 */
class BreakAndCallException extends BreakException {
    private static final long serialVersionUID = -5973306926764652458L;

    private final String stateId;
    private final String returnToStateId;
    private final boolean now;

    public BreakAndCallException(String stateId, boolean now) {
        this(stateId, null, now);
    }

    public BreakAndCallException(String stateId, String returnToStateId, boolean now) {
        if (stateId == null) {
            throw new NullPointerException("stateId");
        }
        this.stateId = stateId;
        this.returnToStateId = returnToStateId;
        this.now = now;
    }

    public boolean isNow() {
        return now;
    }

    public String getStateId() {
        return stateId;
    }

    public String getReturnToStateId() {
        return returnToStateId;
    }
}
