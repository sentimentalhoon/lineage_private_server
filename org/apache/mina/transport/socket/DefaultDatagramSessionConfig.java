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
package org.apache.mina.transport.socket;

import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.mina.util.ExceptionMonitor;

/**
 * A default implementation of {@link DatagramSessionConfig}.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 439913 $, $Date: 2006-09-04 05:12:43 +0200 (mån, 04 sep 2006) $
 */
public class DefaultDatagramSessionConfig extends AbstractDatagramSessionConfig {

    private static boolean SET_RECEIVE_BUFFER_SIZE_AVAILABLE = false;
    private static boolean SET_SEND_BUFFER_SIZE_AVAILABLE = false;
    private static boolean GET_TRAFFIC_CLASS_AVAILABLE = false;
    private static final boolean SET_TRAFFIC_CLASS_AVAILABLE = false;
    private static boolean DEFAULT_BROADCAST = false;
    private static boolean DEFAULT_REUSE_ADDRESS = false;
    private static int DEFAULT_RECEIVE_BUFFER_SIZE = 1024;
    private static int DEFAULT_SEND_BUFFER_SIZE = 1024;
    private static int DEFAULT_TRAFFIC_CLASS = 0;

    static {
        initialize();
    }

    private static void initialize() {
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();
            DEFAULT_BROADCAST = socket.getBroadcast();
            DEFAULT_REUSE_ADDRESS = socket.getReuseAddress();
            DEFAULT_RECEIVE_BUFFER_SIZE = socket.getReceiveBufferSize();
            DEFAULT_SEND_BUFFER_SIZE = socket.getSendBufferSize();

            // Check if setReceiveBufferSize is supported.
            try {
                socket.setReceiveBufferSize(DEFAULT_RECEIVE_BUFFER_SIZE);
                SET_RECEIVE_BUFFER_SIZE_AVAILABLE = true;
            } catch (SocketException e) {
                SET_RECEIVE_BUFFER_SIZE_AVAILABLE = false;
            }

            // Check if setSendBufferSize is supported.
            try {
                socket.setSendBufferSize(DEFAULT_SEND_BUFFER_SIZE);
                SET_SEND_BUFFER_SIZE_AVAILABLE = true;
            } catch (SocketException e) {
                SET_SEND_BUFFER_SIZE_AVAILABLE = false;
            }

            // Check if getTrafficClass is supported.
            try {
                DEFAULT_TRAFFIC_CLASS = socket.getTrafficClass();
                GET_TRAFFIC_CLASS_AVAILABLE = true;
            } catch (SocketException e) {
                GET_TRAFFIC_CLASS_AVAILABLE = false;
                DEFAULT_TRAFFIC_CLASS = 0;
            }
        } catch (SocketException e) {
            ExceptionMonitor.getInstance().exceptionCaught(e);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public static boolean isSetReceiveBufferSizeAvailable() {
        return SET_RECEIVE_BUFFER_SIZE_AVAILABLE;
    }

    public static boolean isSetSendBufferSizeAvailable() {
        return SET_SEND_BUFFER_SIZE_AVAILABLE;
    }

    public static boolean isGetTrafficClassAvailable() {
        return GET_TRAFFIC_CLASS_AVAILABLE;
    }

    public static boolean isSetTrafficClassAvailable() {
        return SET_TRAFFIC_CLASS_AVAILABLE;
    }

    private boolean broadcast = DEFAULT_BROADCAST;
    private boolean reuseAddress = DEFAULT_REUSE_ADDRESS;
    private int receiveBufferSize = DEFAULT_RECEIVE_BUFFER_SIZE;
    private int sendBufferSize = DEFAULT_SEND_BUFFER_SIZE;
    private int trafficClass = DEFAULT_TRAFFIC_CLASS;
    
    /**
     * Creates a new instance.
     */
    public DefaultDatagramSessionConfig() {
    }

    /**
     * @see DatagramSocket#getBroadcast()
     */
    public boolean isBroadcast() {
        return broadcast;
    }

    /**
     * @see DatagramSocket#setBroadcast(boolean)
     */
    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    /**
     * @see DatagramSocket#getReuseAddress()
     */
    public boolean isReuseAddress() {
        return reuseAddress;
    }

    /**
     * @see DatagramSocket#setReuseAddress(boolean)
     */
    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    /**
     * @see DatagramSocket#getReceiveBufferSize()
     */
    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * @see DatagramSocket#setReceiveBufferSize(int)
     */
    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    /**
     * @see DatagramSocket#getSendBufferSize()
     */
    public int getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * @see DatagramSocket#setSendBufferSize(int)
     */
    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    /**
     * @see DatagramSocket#getTrafficClass()
     */
    public int getTrafficClass() {
        return trafficClass;
    }

    /**
     * @see DatagramSocket#setTrafficClass(int)
     */
    public void setTrafficClass(int trafficClass) {
        this.trafficClass = trafficClass;
    }

    @Override
    protected boolean isBroadcastChanged() {
        return broadcast != DEFAULT_BROADCAST;
    }

    @Override
    protected boolean isReceiveBufferSizeChanged() {
        return receiveBufferSize != DEFAULT_RECEIVE_BUFFER_SIZE;
    }

    @Override
    protected boolean isReuseAddressChanged() {
        return reuseAddress != DEFAULT_REUSE_ADDRESS;
    }

    @Override
    protected boolean isSendBufferSizeChanged() {
        return sendBufferSize != DEFAULT_SEND_BUFFER_SIZE;
    }

    @Override
    protected boolean isTrafficClassChanged() {
        return trafficClass != DEFAULT_TRAFFIC_CLASS;
    }
    
}