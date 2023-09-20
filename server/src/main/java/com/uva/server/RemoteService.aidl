// RemoteService.aidl
package com.uva.server;

interface RemoteService {
    /** Request the process ID of this service. */
    int getPid();

    int doSmth();

    void sendMessage(String message);

    /** Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);
}