package com.krt.basemodule.push;

import java.io.Serializable;

/**
 * @author KRT
 * 2018/11/23
 */
public interface IMessage extends Serializable {
    byte[] getData();
}
