package vn.tayjava.exception;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ErrorResponse implements Serializable {

    private Date timestamp;
    private int status;
    private String path;
    private String error;
    private String message;

}