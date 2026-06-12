package com.canteen.bc.canteen_system.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
  private String code;

  public static BusinessException of(SysError sysError) {
    return new BusinessException(sysError);
  }

  private BusinessException(SysError sysError) {
    super(sysError.getMessage());
    this.code = sysError.getCode();
  }
}
