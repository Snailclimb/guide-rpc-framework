package github.javaguide;

import java.io.Serializable;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:04:00
 */

public class Hello implements Serializable {
    private String message;
    private String description;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Hello(String message, String description) {
        this.message = message;
        this.description = description;
    }
}
