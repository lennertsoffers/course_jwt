package be.lennertsoffers.supportportalapplication.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Getter
@Setter
@RequiredArgsConstructor
public class HttpResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss", timezone = "Europe/Brussels")
    private final Date timestamp = new Date();
    private final int httpStatusCode;
    private final HttpStatus httpStatus;
    private final String reason;
    private final String message;
}
