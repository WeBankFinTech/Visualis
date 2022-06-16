package com.webank.wedatasphere.dss.visualis.exception;

import org.apache.linkis.common.exception.ErrorException;

public class VGErrorException extends ErrorException {


    public VGErrorException(int errCode, String desc) {
        super(errCode, desc);
    }

    public VGErrorException(int errCode, String desc, String ip, int port, String serviceKind) {
        super(errCode, desc, ip, port, serviceKind);
    }
}
