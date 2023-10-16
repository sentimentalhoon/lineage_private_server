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
package org.apache.mina.integration.beans;

import java.beans.PropertyEditor;

import org.apache.mina.core.session.TrafficMask;

/**
 * A {@link PropertyEditor} which converts a {@link String} into a
 * {@link TrafficMask} and vice versa.   "<tt>all</tt>", "<tt>read</tt>", 
 * "<tt>write</tt>" and "<tt>none</tt>" are allowed.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Revision: 671827 $, $Date: 2008-06-26 10:49:48 +0200 (jeu, 26 jun 2008) $
 *
 * @see org.apache.mina.transport.vmpipe.VmPipeAddress
 */
public class TrafficMaskEditor extends AbstractPropertyEditor {
    @Override
    protected String toText(Object value) {
        return ((TrafficMask) value).getName().toUpperCase();
    }

    @Override
    protected Object toValue(String text) throws IllegalArgumentException {
        if ("all".equalsIgnoreCase(text)) {
            return TrafficMask.ALL;
        }
        if ("read".equalsIgnoreCase(text)) {
            return TrafficMask.READ;
        }
        if ("write".equalsIgnoreCase(text)) {
            return TrafficMask.WRITE;
        }
        if ("none".equalsIgnoreCase(text)) {
            return TrafficMask.NONE;
        }
        throw new IllegalArgumentException(
                text + " (expected: all, read, write or none)");
    }
}
