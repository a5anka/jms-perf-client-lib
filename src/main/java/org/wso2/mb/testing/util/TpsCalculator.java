package org.wso2.mb.testing.util;

import java.util.concurrent.TimeUnit;

public class TpsCalculator {
    private enum State{
        RUNNING, STOPPED;
    }

    private State currentState = State.STOPPED;

    private long startTime;

    private long endTime;

    private int numberOfEvents = 0;

    public void start(){
        currentState = State.RUNNING;
        startTime = getSystemTime();
    }

    public void stop() {
        endTime = getSystemTime();
        currentState = State.STOPPED;
    }

    public double getTps() {
        if (currentState == State.RUNNING) {
            long duration = endTime - startTime;
            return numberOfEvents * 1000 / TimeUnit.MILLISECONDS.toSeconds(duration);
        } else {
            throw new IllegalStateException(TpsCalculator.class + " should be stopped before calculating the TPS");
        }
    }

    private long getSystemTime() {
        return System.currentTimeMillis();
    }

    public void mark() {
        numberOfEvents++;
    }
}
