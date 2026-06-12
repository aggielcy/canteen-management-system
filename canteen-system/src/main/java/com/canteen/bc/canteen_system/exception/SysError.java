package com.canteen.bc.canteen_system.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysError {
  SUCCESS("000000", "Success."), //
  INVALID_PASSWORD("000001", "Invalid Password."), //
  // "100000" - "199999"
  USERNAME_NOT_FOUND("100000", "Username Not Found."), //
  PASSWORD_INVALID_FORMAT("100001", "Invalid Password Format"), //
  ;

  private String code;
  private String message;
}
