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
package org.apache.mina.filter.statistic;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.util.CopyOnWriteMap;

/**
 * This class will measure, the time it takes for a
 * method in the {@link IoFilterAdapter} class to execute.  The basic
 * premise of the logic in this class is to get the current time
 * at the beginning of the method, call method on nextFilter, and
 * then get the current time again.  An example of how to use
 * the filter is:
 *
 * <pre>
 * ProfilerTimerFilter profiler = new ProfilerTimerFilter(
 *         TimeUnit.MILLISECOND, IoEventType.MESSAGE_RECEIVED);
 * chain.addFirst("Profiler", profiler);
 * </pre>
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 671827 $, $Date: 2008-06-26 10:49:48 +0200 (jeu, 26 jun 2008) $
 */
public class ProfilerTimerFilter extends IoFilterAdapter {
    
    private volatile EnumSet<IoEventType> eventsToProfile;
    private volatile ProfilerTimerUnit timeUnit;
    private final Map<IoEventType, TimerWorker> timerManager;

    /**
     * Creates a new instance of ProfilerFilter.  This is the
     * default constructor and will print out timings for
     * messageReceived and messageSent and the time increment
     * will be in milliseconds.
     */
    public ProfilerTimerFilter() {
        this(
                TimeUnit.MILLISECONDS, EnumSet.of(IoEventType.MESSAGE_RECEIVED,
                                IoEventType.MESSAGE_SENT));
    }
    
    /**
     * Creates a new instance of ProfilerFilter.  This is the
     * default constructor and will print out timings for
     * messageReceived and messageSent and the time increment
     * will be in milliseconds.
     */
    public ProfilerTimerFilter(TimeUnit unit) {
        this(
                unit, 
                IoEventType.MESSAGE_RECEIVED, IoEventType.MESSAGE_SENT);
    }
    
    /**
     * Creates a new instance of ProfilerFilter.  An example
     * of this call would be:
     *
     * <pre>
     * new ProfilerTimerFilter(
     *         TimeUnit.MILLISECONDS,
     *         IoEventType.MESSAGE_RECEIVED, IoEventType.MESSAGE_SENT);
     * </pre>
     * @param unit
     *  Used to determine the level of precision you need in your timing.
     * @param firstEventType an event type to profile
     * @param otherEventTypes event types to profile
     */
    public ProfilerTimerFilter(TimeUnit unit, IoEventType firstEventType, IoEventType... otherEventTypes) {
        this(unit, EnumSet.of(firstEventType, otherEventTypes));
    }


    /**
     * Creates a new instance of ProfilerFilter.  An example
     * of this call would be:
     *
     * <pre>
     * new ProfilerTimerFilter(
     *         TimeUnit.MILLISECONDS,
     *         EnumSet.of(IoEventType.MESSAGE_RECEIVED, IoEventType.MESSAGE_SENT));
     * </pre>
     * @param unit
     *  Used to determine the level of precision you need in your timing.
     * @param eventTypes
     *  A set of {@link IoEventType} representation of the methods to profile
     */
    public ProfilerTimerFilter(TimeUnit unit, EnumSet<IoEventType> eventTypes) {
        setTimeUnit(unit);
        setEventsToProfile(eventTypes);

        timerManager = new CopyOnWriteMap<IoEventType, TimerWorker>();
        for (IoEventType type : eventsToProfile) {
            timerManager.put(type, new TimerWorker());
        }
    }

    /**
     * Sets the {@link TimeUnit} being used.
     *
     * @param unit the new {@link TimeUnit} to be used.
     */
    public void setTimeUnit(TimeUnit unit) {
        if (unit == TimeUnit.MILLISECONDS) {
            this.timeUnit = ProfilerTimerUnit.MILLISECONDS;
        } else if (unit == TimeUnit.NANOSECONDS) {
            this.timeUnit = ProfilerTimerUnit.NANOSECONDS;
        } else if (unit == TimeUnit.SECONDS) {
            this.timeUnit = ProfilerTimerUnit.SECONDS;
        } else {
            throw new IllegalArgumentException(
                    "Invalid Time specified: " + unit + " (expected: " +
                    TimeUnit.MILLISECONDS + ", " +
                    TimeUnit.NANOSECONDS + " or " +
                    TimeUnit.SECONDS + ')');
        }
    }

    /**
     * Add an {@link IoEventType} to profile
     *
     * @param type
     *  The {@link IoEventType} to profile
     */
    public void addEventToProfile(IoEventType type) {
        if (!timerManager.containsKey(type)) {
            timerManager.put(type, new TimerWorker());
        }
    }

    /**
     * Remove an {@link IoEventType} to profile
     *
     * @param type
     *  The {@link IoEventType} to profile
     */
    public void removeEventToProfile(IoEventType type) {
        timerManager.remove(type);
    }

    /**
     * Return the bitmask that is being used to display
     * timing information for this filter.
     *
     * @return
     *  An int representing the methods that will be logged
     */
    public Set<IoEventType> getEventsToProfile() {
        return Collections.unmodifiableSet(eventsToProfile);
    }

    /**
     * Set the bitmask in order to tell this filter which
     * methods to print out timing information
     */
    public void setEventsToProfile(IoEventType firstEventType, IoEventType... otherEventTypes) {
        this.setEventsToProfile(EnumSet.of(firstEventType, otherEventTypes));
    }

    /**
     * Set the bitmask in order to tell this filter which
     * methods to print out timing information
     *
     * @param eventTypes
     *  An int representing the new methods that should be logged
     */
    public void setEventsToProfile(Set<IoEventType> eventTypes) {
        if (eventTypes == null) {
            throw new NullPointerException("eventTypes");
        }
        if (eventTypes.isEmpty()) {
            throw new IllegalArgumentException("eventTypes is empty.");
        }

        EnumSet<IoEventType> newEventsToProfile = EnumSet.noneOf(IoEventType.class);
        for (IoEventType e: eventTypes) {
            newEventsToProfile.add(e);
        }
        
        this.eventsToProfile = newEventsToProfile;
    }

    @Override
    public void messageReceived(NextFilter nextFilter, IoSession session,
            Object message) throws Exception {
        long start = timeUnit.timeNow();
        nextFilter.messageReceived(session, message);
        long end = timeUnit.timeNow();

        if (getEventsToProfile().contains(IoEventType.MESSAGE_RECEIVED)) {
            timerManager.get(IoEventType.MESSAGE_RECEIVED).addNewReading(
                    end - start);
        }
    }

    @Override
    public void messageSent(NextFilter nextFilter, IoSession session,
            WriteRequest writeRequest) throws Exception {
        long start = timeUnit.timeNow();
        nextFilter.messageSent(session, writeRequest);
        long end = timeUnit.timeNow();

        if (getEventsToProfile().contains(IoEventType.MESSAGE_SENT)) {
            timerManager.get(IoEventType.MESSAGE_SENT).addNewReading(
                    end - start);
        }
    }

    @Override
    public void sessionClosed(NextFilter nextFilter, IoSession session)
            throws Exception {
        long start = timeUnit.timeNow();
        nextFilter.sessionClosed(session);
        long end = timeUnit.timeNow();

        if (getEventsToProfile().contains(IoEventType.SESSION_CLOSED)) {
            timerManager.get(IoEventType.SESSION_CLOSED).addNewReading(
                    end - start);
        }
    }

    @Override
    public void sessionCreated(NextFilter nextFilter, IoSession session)
            throws Exception {
        long start = timeUnit.timeNow();
        nextFilter.sessionCreated(session);
        long end = timeUnit.timeNow();

        if (getEventsToProfile().contains(IoEventType.SESSION_CREATED)) {
            timerManager.get(IoEventType.SESSION_CREATED).addNewReading(
                    end - start);
        }
    }

    @Override
    public void sessionIdle(NextFilter nextFilter, IoSession session,
            IdleStatus status) throws Exception {
        long start = timeUnit.timeNow();
        nextFilter.sessionIdle(session, status);
        long end = timeUnit.timeNow();

        if (getEventsToProfile().contains(IoEventType.SESSION_IDLE)) {
            timerManager.get(IoEventType.SESSION_IDLE).addNewReading(
                    end - start);
        }
    }

    @Override
    public void sessionOpened(NextFilter nextFilter, IoSession session)
            throws Exception {
        long start = timeUnit.timeNow();
        nextFilter.sessionOpened(session);
        long end = timeUnit.timeNow();

        if (getEventsToProfile().contains(IoEventType.SESSION_OPENED)) {
            timerManager.get(IoEventType.SESSION_OPENED).addNewReading(
                    end - start);
        }
    }

    /**
     * Get the average time for the specified method represented by the {@link IoEventType}
     *
     * @param type
     *  The {@link IoEventType} that the user wants to get the average method call time
     * @return
     *  The average time it took to execute the method represented by the {@link IoEventType}
     */
    public double getAverageTime(IoEventType type) {
        if (!timerManager.containsKey(type)) {
            throw new IllegalArgumentException(
                    "You are not monitoring this event.  Please add this event first.");
        }

        return timerManager.get(type).getAverage();
    }

    /**
     * Gets the total number of times the method has been called that is represented by the
     * {@link IoEventType}
     *
     * @param type
     *  The {@link IoEventType} that the user wants to get the total number of method calls
     * @return
     *  The total number of method calls for the method represented by the {@link IoEventType}
     */
    public long getTotalCalls(IoEventType type) {
        if (!timerManager.containsKey(type)) {
            throw new IllegalArgumentException(
                    "You are not monitoring this event.  Please add this event first.");
        }

        return timerManager.get(type).getCalls();
    }

    /**
     * The total time this method has been executing
     *
     * @param type
     *  The {@link IoEventType} that the user wants to get the total time this method has
     *  been executing
     * @return
     *  The total time for the method represented by the {@link IoEventType}
     */
    public long getTotalTime(IoEventType type) {
        if (!timerManager.containsKey(type)) {
            throw new IllegalArgumentException(
                    "You are not monitoring this event.  Please add this event first.");
        }

        return timerManager.get(type).getTotal();
    }

    /**
     * The minimum time the method represented by {@link IoEventType} has executed
     *
     * @param type
     *  The {@link IoEventType} that the user wants to get the minimum time this method has
     *  executed
     * @return
     *  The minimum time this method has executed represented by the {@link IoEventType}
     */
    public long getMinimumTime(IoEventType type) {
        if (!timerManager.containsKey(type)) {
            throw new IllegalArgumentException(
                    "You are not monitoring this event.  Please add this event first.");
        }

        return timerManager.get(type).getMinimum();
    }

    /**
     * The maximum time the method represented by {@link IoEventType} has executed
     *
     * @param type
     *  The {@link IoEventType} that the user wants to get the maximum time this method has
     *  executed
     * @return
     *  The maximum time this method has executed represented by the {@link IoEventType}
     */
    public long getMaximumTime(IoEventType type) {
        if (!timerManager.containsKey(type)) {
            throw new IllegalArgumentException(
                    "You are not monitoring this event.  Please add this event first.");
        }

        return timerManager.get(type).getMaximum();
    }

    /**
     * Class that will track the time each method takes and be able to provide information
     * for each method.
     *
     */
    private class TimerWorker {

        private final AtomicLong total;
        private final AtomicLong calls;
        private final AtomicLong minimum;
        private final AtomicLong maximum;
        private final Object lock = new Object();

        /**
         * Creates a new instance of TimerWorker.
         *
         */
        public TimerWorker() {
            total = new AtomicLong();
            calls = new AtomicLong();
            minimum = new AtomicLong();
            maximum = new AtomicLong();
        }

        /**
         * Add a new reading to this class.  Total is updated
         * and calls is incremented
         *
         * @param newReading
         *  The new reading
         */
        public void addNewReading(long newReading) {
            calls.incrementAndGet();
            total.addAndGet(newReading);

            synchronized (lock) {
                // this is not entirely thread-safe, must lock
                if (newReading < minimum.longValue()) {
                    minimum.set(newReading);
                }

                // this is not entirely thread-safe, must lock
                if (newReading > maximum.longValue()) {
                    maximum.set(newReading);
                }
            }
        }

        /**
         * Gets the average reading for this event
         *
         * @return
         *  Gets the average reading for this event
         */
        public double getAverage() {
            return total.longValue() / calls.longValue();
        }

        /**
         * Returns the total number of readings
         *
         * @return
         *  total number of readings
         */
        public long getCalls() {
            return calls.longValue();
        }

        /**
         * Returns the total time
         *
         * @return
         *  the total time
         */
        public long getTotal() {
            return total.longValue();
        }

        /**
         * Returns the minimum value
         *
         * @return
         *  the minimum value
         */
        public long getMinimum() {
            return minimum.longValue();
        }

        /**
         * Returns the maximum value
         *
         * @return
         *  the maximum value
         */
        public long getMaximum() {
            return maximum.longValue();
        }
    }

    private enum ProfilerTimerUnit {
        SECONDS {
            @Override
            public long timeNow() {
                return System.currentTimeMillis() / 1000;
            }

            @Override
            public String getDescription() {
                return "seconds";
            }
        },
        MILLISECONDS {
            @Override
            public long timeNow() {
                return System.currentTimeMillis();
            }

            @Override
            public String getDescription() {
                return "milliseconds";
            }
        },
        NANOSECONDS {
            @Override
            public long timeNow() {
                return System.nanoTime();
            }

            @Override
            public String getDescription() {
                return "nanoseconds";
            }
        };

        /*
         * I was looking at possibly using the java.util.concurrent.TimeUnit
         * and I found this construct for writing enums.  Here is what the
         * JDK developers say for why these methods below cannot be marked as
         * abstract, but should act in an abstract way...
         *
         *     To maintain full signature compatibility with 1.5, and to improve the
         *     clarity of the generated javadoc (see 6287639: Abstract methods in
         *     enum classes should not be listed as abstract), method convert
         *     etc. are not declared abstract but otherwise act as abstract methods.
         */
        public long timeNow() {
            throw new AbstractMethodError();
        }

        public String getDescription() {
            throw new AbstractMethodError();
        }
    }
}
