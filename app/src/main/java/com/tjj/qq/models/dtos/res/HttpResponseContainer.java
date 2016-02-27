package com.tjj.qq.models.dtos.res;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tunnel_togashi on 15/12/02.
 */
public class HttpResponseContainer<Res extends BaseHttpResponseDto> {

    @SerializedName("data")
    private Res responseDto;

    @SerializedName("status")
    private boolean succeeded;

    @SerializedName("error")
    private RcError error;

    private int statusCode;

    private final List<Exception> exceptionList = new ArrayList<>();

    public Res getResponseDto() {
        return responseDto;
    }

    public void addException(Exception e){
        exceptionList.add(e);
    }

    public List<Exception> getExceptionList() {
        return exceptionList;
    }

    public void setResponseDto(Res responseDto) {
        this.responseDto = responseDto;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public RcError getError() {
        return error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean succeeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public void setError(RcError error) {
        this.error = error;
    }


    public static class RcError {


        @SerializedName("type")
        private String type;

        @SerializedName("message")
        private String message;

        @SerializedName("rc_code")
        private int rcErrorCode;

    }
}
