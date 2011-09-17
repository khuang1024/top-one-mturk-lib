package edu.ucsc.cs.mturk.server.topone;

interface Algorithm {
    void start();
    boolean isDone();
    Object getFinalAnswer();
}
