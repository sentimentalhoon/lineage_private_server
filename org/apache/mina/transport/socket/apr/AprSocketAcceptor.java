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
package org.apache.mina.transport.socket.apr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.polling.AbstractPollingIoAcceptor;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.transport.socket.DefaultSocketSessionConfig;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.util.CircularQueue;
import org.apache.tomcat.jni.Address;
import org.apache.tomcat.jni.Poll;
import org.apache.tomcat.jni.Pool;
import org.apache.tomcat.jni.Socket;
import org.apache.tomcat.jni.Status;

/**
 * TODO Add documentation
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 671827 $, $Date: 2008-06-26 10:49:48 +0200 (jeu, 26 jun 2008) $
 */
public final class AprSocketAcceptor extends AbstractPollingIoAcceptor<AprSession, Long> implements SocketAcceptor {

    private static final int POLLSET_SIZE = 1024;

    private final Object wakeupLock = new Object();
    private volatile long wakeupSocket;
    private volatile boolean toBeWakenUp;

    private int backlog = 50;
    private boolean reuseAddress = false;

    private volatile long pool;
    private volatile long pollset; // socket poller
    private final long[] polledSockets = new long[POLLSET_SIZE << 1];
    private final List<Long> polledHandles =
        new CircularQueue<Long>(POLLSET_SIZE);

    public AprSocketAcceptor() {
        super(new DefaultSocketSessionConfig(), AprIoProcessor.class);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public AprSocketAcceptor(int processorCount) {
        super(new DefaultSocketSessionConfig(), AprIoProcessor.class, processorCount);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public AprSocketAcceptor(IoProcessor<AprSession> processor) {
        super(new DefaultSocketSessionConfig(), processor);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    public AprSocketAcceptor(Executor executor,
            IoProcessor<AprSession> processor) {
        super(new DefaultSocketSessionConfig(), executor, processor);
        ((DefaultSocketSessionConfig) getSessionConfig()).init(this);
    }

    @Override
    protected AprSession accept(IoProcessor<AprSession> processor, Long handle) throws Exception {
        long s = Socket.accept(handle);
        boolean success = false;
        try {
            AprSession result = new AprSocketSession(this, processor, s);
            success = true;
            return result;
        } finally {
            if (!success) {
                Socket.close(s);
            }
        }
    }

    @Override
    protected Long open(SocketAddress localAddress) throws Exception {
        InetSocketAddress la = (InetSocketAddress) localAddress;
        long handle = Socket.create(
                Socket.APR_INET, Socket.SOCK_STREAM, Socket.APR_PROTO_TCP, pool);

        boolean success = false;
        try {
            int result = Socket.optSet(handle, Socket.APR_SO_NONBLOCK, 1);
            if (result != Status.APR_SUCCESS) {
                throwException(result);
            }
            result = Socket.timeoutSet(handle, 0);
            if (result != Status.APR_SUCCESS) {
                throwException(result);
            }

            // Configure the server socket,
            result = Socket.optSet(handle, Socket.APR_SO_REUSEADDR, isReuseAddress()? 1 : 0);
            if (result != Status.APR_SUCCESS) {
                throwException(result);
            }
            result = Socket.optSet(handle, Socket.APR_SO_RCVBUF, getSessionConfig().getReceiveBufferSize());
            if (result != Status.APR_SUCCESS) {
                throwException(result);
            }

            // and bind.
            long sa;
            if (la != null) {
                if (la.getAddress() == null) {
                    sa = Address.info(Address.APR_ANYADDR, Socket.APR_INET, la.getPort(), 0, pool);
                } else {
                    sa = Address.info(la.getAddress().getHostAddress(), Socket.APR_INET, la.getPort(), 0, pool);
                }
            } else {
                sa = Address.info(Address.APR_ANYADDR, Socket.APR_INET, 0, 0, pool);
            }

            result = Socket.bind(handle, sa);
            if (result != Status.APR_SUCCESS) {
                throwException(result);
            }
            result = Socket.listen(handle, getBacklog());
            if (result != Status.APR_SUCCESS) {
                throwException(result);
            }

            result = Poll.add(pollset, handle, Poll.APR_POLLIN);
            if (result != Status.APR_SUCCESS) {
                throwException(result);
            }
            success = true;
        } finally {
            if (!success) {
                close(handle);
            }
        }
        return handle;
    }

    @Override
    protected void init() throws Exception {
        // initialize a memory pool for APR functions
        pool = Pool.create(AprLibrary.getInstance().getRootPool());

        wakeupSocket = Socket.create(
                Socket.APR_INET, Socket.SOCK_DGRAM, Socket.APR_PROTO_UDP, pool);

        pollset = Poll.create(
                        POLLSET_SIZE,
                        pool,
                        Poll.APR_POLLSET_THREADSAFE,
                        Long.MAX_VALUE);

        if (pollset <= 0) {
            pollset = Poll.create(
                    62,
                    pool,
                    Poll.APR_POLLSET_THREADSAFE,
                    Long.MAX_VALUE);
        }

        if (pollset <= 0) {
            if (Status.APR_STATUS_IS_ENOTIMPL(- (int) pollset)) {
                throw new RuntimeIoException(
                        "Thread-safe pollset is not supported in this platform.");
            }
        }
    }

    @Override
    protected void destroy() throws Exception {
        if (wakeupSocket > 0) {
            Socket.close(wakeupSocket);
        }
        if (pollset > 0) {
            Poll.destroy(pollset);
        }
        if (pool > 0) {
            Pool.destroy(pool);
        }
    }

    @Override
    protected SocketAddress localAddress(Long handle) throws Exception {
        long la = Address.get(Socket.APR_LOCAL, handle);
        return new InetSocketAddress(Address.getip(la), Address.getInfo(la).port);
    }

    @Override
    protected boolean select() throws Exception {
        int rv = Poll.poll(pollset, Integer.MAX_VALUE, polledSockets, false);
        if (rv <= 0) {
            if (rv != -120001) {
                throwException(rv);
            }

            rv = Poll.maintain(pollset, polledSockets, true);
            if (rv > 0) {
                for (int i = 0; i < rv; i ++) {
                    Poll.add(pollset, polledSockets[i], Poll.APR_POLLIN);
                }
            } else if (rv < 0) {
                throwException(rv);
            }

            return false;
        } else {
            rv <<= 1;
            if (!polledHandles.isEmpty()) {
                polledHandles.clear();
            }

            for (int i = 0; i < rv; i ++) {
                long flag = polledSockets[i];
                long socket = polledSockets[++i];
                if (socket == wakeupSocket) {
                    synchronized (wakeupLock) {
                        Poll.remove(pollset, wakeupSocket);
                        toBeWakenUp = false;
                    }
                    continue;
                }

                if ((flag & Poll.APR_POLLIN) != 0) {
                    polledHandles.add(socket);
                }
            }
            return !polledHandles.isEmpty();
        }
    }

    @Override
    protected Iterator<Long> selectedHandles() {
        return polledHandles.iterator();
    }

    @Override
    protected void close(Long handle) throws Exception {
        Poll.remove(pollset, handle);
        int result = Socket.close(handle);
        if (result != Status.APR_SUCCESS) {
            throwException(result);
        }
    }

    @Override
    protected void wakeup() {
        if (toBeWakenUp) {
            return;
        }

        // Add a dummy socket to the pollset.
        synchronized (wakeupLock) {
            toBeWakenUp = true;
            Poll.add(pollset, wakeupSocket, Poll.APR_POLLOUT);
        }
    }

    public int getBacklog() {
        return backlog;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public void setBacklog(int backlog) {
        synchronized (bindLock) {
            if (isActive()) {
                throw new IllegalStateException(
                        "backlog can't be set while the acceptor is bound.");
            }

            this.backlog = backlog;
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) super.getLocalAddress();
    }

    @Override
    public InetSocketAddress getDefaultLocalAddress() {
        return (InetSocketAddress) super.getDefaultLocalAddress();
    }

    public void setDefaultLocalAddress(InetSocketAddress localAddress) {
        super.setDefaultLocalAddress(localAddress);
    }

    public void setReuseAddress(boolean reuseAddress) {
        synchronized (bindLock) {
            if (isActive()) {
                throw new IllegalStateException(
                        "backlog can't be set while the acceptor is bound.");
            }

            this.reuseAddress = reuseAddress;
        }
    }

    public TransportMetadata getTransportMetadata() {
        return AprSocketSession.METADATA;
    }

    @Override
    public SocketSessionConfig getSessionConfig() {
        return (SocketSessionConfig) super.getSessionConfig();
    }

    private void throwException(int code) throws IOException {
        throw new IOException(
                org.apache.tomcat.jni.Error.strerror(-code) +
                " (code: " + code + ")");
    }
}
