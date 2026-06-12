package com.canteen.bc.canteen_system;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CanteenSystemApplication {

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));
    SpringApplication.run(CanteenSystemApplication.class, args);
  }
}
