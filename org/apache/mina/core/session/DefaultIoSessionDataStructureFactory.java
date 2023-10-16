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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.util.CircularQueue;

/**
 * The default {@link IoSessionDataStructureFactory} implementation
 * that creates a new {@link HashMap}-based {@link IoSessionAttributeMap}
 * instance and a new synchronized {@link CircularQueue} instance per
 * {@link IoSession}.
 * 
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 671827 $, $Date: 2008-06-26 10:49:48 +0200 (jeu, 26 jun 2008) $
 */
public class DefaultIoSessionDataStructureFactory implements
        IoSessionDataStructureFactory {

    public IoSessionAttributeMap getAttributeMap(IoSession session)
            throws Exception {
        return new DefaultIoSessionAttributeMap();
    }
    
    public WriteRequestQueue getWriteRequestQueue(IoSession session)
            throws Exception {
        return new DefaultWriteRequestQueue();
    }

    private static class DefaultIoSessionAttributeMap implements IoSessionAttributeMap {

        private final Map<Object, Object> attributes =
            Collections.synchronizedMap(new HashMap<Object, Object>(4));

        public Object getAttribute(IoSession session, Object key, Object defaultValue) {
            if (key == null) {
                throw new NullPointerException("key");
            }

            Object answer = attributes.get(key);
            if (answer == null) {
                return defaultValue;
            } else {
                return answer;
            }
        }

        public Object setAttribute(IoSession session, Object key, Object value) {
            if (key == null) {
                throw new NullPointerException("key");
            }

            if (value == null) {
                return attributes.remove(key);
            } else {
                return attributes.put(key, value);
            }
        }

        public Object setAttributeIfAbsent(IoSession session, Object key, Object value) {
            if (key == null) {
                throw new NullPointerException("key");
            }

            if (value == null) {
                return null;
            }

            Object oldValue;
            synchronized (attributes) {
                oldValue = attributes.get(key);
                if (oldValue == null) {
                    attributes.put(key, value);
                }
            }
            return oldValue;
        }

        public Object removeAttribute(IoSession session, Object key) {
            if (key == null) {
                throw new NullPointerException("key");
            }

            return attributes.remove(key);
        }

        public boolean removeAttribute(IoSession session, Object key, Object value) {
            if (key == null) {
                throw new NullPointerException("key");
            }

            if (value == null) {
                return false;
            }

            synchronized (attributes) {
                if (value.equals(attributes.get(key))) {
                    attributes.remove(key);
                    return true;
                }
            }

            return false;
        }

        public boolean replaceAttribute(IoSession session, Object key, Object oldValue, Object newValue) {
            synchronized (attributes) {
                Object actualOldValue = attributes.get(key);
                if (actualOldValue == null) {
                    return false;
                }

                if (actualOldValue.equals(oldValue)) {
                    attributes.put(key, newValue);
                    return true;
                } else {
                    return false;
                }
            }
        }

        public boolean containsAttribute(IoSession session, Object key) {
            return attributes.containsKey(key);
        }

        public Set<Object> getAttributeKeys(IoSession session) {
            synchronized (attributes) {
                return new HashSet<Object>(attributes.keySet());
            }
        }

        public void dispose(IoSession session) throws Exception {
        }
    }
    
    private static class DefaultWriteRequestQueue implements WriteRequestQueue {

        private final Queue<WriteRequest> q = new CircularQueue<WriteRequest>(16);
        
        public void dispose(IoSession session) {
        }
        
        public void clear(IoSession session) {
            q.clear();
        }

        public synchronized boolean isEmpty(IoSession session) {
            return q.isEmpty();
        }

        public synchronized void offer(IoSession session, WriteRequest writeRequest) {
            q.offer(writeRequest);
        }

        public synchronized WriteRequest poll(IoSession session) {
            return q.poll();
        }
        
        @Override
        public String toString() {
            return q.toString();
        }
    }
}
