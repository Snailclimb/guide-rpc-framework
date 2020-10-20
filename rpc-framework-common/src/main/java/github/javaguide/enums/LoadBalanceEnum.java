package github.javaguide.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LoadBalanceEnum {
  COMPLETELY_RANDOM((byte) 0x01),

  CONSISTENT_HASH((byte) 0x02);

  private final byte mode;
}
