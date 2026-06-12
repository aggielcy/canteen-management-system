package com.canteen.bc.canteen_system.exception;

public class InsufficientFundsException extends RuntimeException {

  public InsufficientFundsException(String message) {
    super(message);
  }
}
