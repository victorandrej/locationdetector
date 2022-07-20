package br.com.victorandrej.croct.locationdetector.record;

import br.com.victorandrej.croct.locationdetector.enums.ResponseStatus;


public record TopicResponse(ResponseStatus status, Object response) {

}
