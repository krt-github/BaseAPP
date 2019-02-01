package com.krt.basemodule.connection;

/**
 * @author KRT
 * 2018/11/27
 */
public interface IEOFListener {
    boolean isEOF(byte[] rawData);
}
