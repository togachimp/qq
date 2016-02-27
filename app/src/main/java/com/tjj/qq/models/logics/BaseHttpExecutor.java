package com.tjj.qq.models.logics;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tjj.qq.models.dtos.req.BaseHttpRequestDto;
import com.tjj.qq.models.dtos.res.BaseHttpResponseDto;
import com.tjj.qq.models.dtos.res.HttpResponseContainer;
import com.tjj.qq.utils.URLEnc;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tunnel_togashi on 15/12/02.
 */
public class BaseHttpExecutor<Req extends BaseHttpRequestDto, Res extends BaseHttpResponseDto> {


    private RequestQueue requestQueue;
    private boolean useMockData = false;

    private OnRequestFinishListener<Res> onRequestFinishListener;
    private OnNetworkErrorListener onNetworkErrorListener;

    public BaseHttpExecutor(Context context) {
//        this.requestQueue = requestQueue;
        if (context instanceof Application) {
            requestQueue = Volley.newRequestQueue(context);
        } else {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        requestQueue.start();
    }

    public void setOnNetworkErrorListener(OnNetworkErrorListener onNetworkErrorListener) {
        this.onNetworkErrorListener = onNetworkErrorListener;
    }

    public void setOnRequestFinishListener(OnRequestFinishListener<Res> onRequestFinishListener) {
        this.onRequestFinishListener = onRequestFinishListener;
    }

    public void setUseMockData(boolean useMockData) {
        this.useMockData = useMockData;
    }

    public void execute(final Req request) {
        if (useMockData) {

            new AsyncTask<Void, Void, Object>() {

                @Override
                protected Object doInBackground(Void... params) {

                    try {
                        HttpResponseContainer<Res> container = new HttpResponseContainer<>();
                        int statusCode = 200;
                        container.setStatusCode(statusCode);
                        String responseString = getMockJasonString(request);
                        Class clazz = this.getClass();

                        ParameterizedType genericSuperClass = (ParameterizedType) clazz.getGenericSuperclass();
                        Type[] actualTypeArguments = genericSuperClass.getActualTypeArguments();
                        Class<Res> responseClass = (Class<Res>) actualTypeArguments[1];

                        Gson gson = new Gson();

                        JsonObject responseJson = (JsonObject) new JsonParser().parse(responseString);


                        boolean succeeded = responseJson.get("status").getAsBoolean();
                        container.setSucceeded(succeeded);

                        if (succeeded) {
                            JsonElement data = responseJson.get("data");
                            Res responseDto = gson.fromJson(data, responseClass);
                            container.setResponseDto(responseDto);

                        } else {
                            JsonElement errorJson = responseJson.get("error");
                            HttpResponseContainer.RcError error = gson.fromJson(errorJson, HttpResponseContainer.RcError.class);
                            container.setError(error);
                        }

                        return container;
                    } catch (Throwable th) {

                        ArrayList<Throwable> exceptionList = new ArrayList<>();
                        exceptionList.add(th);
                        return exceptionList;

                    }

                }

                @Override
                protected void onPostExecute(Object object) {
                    if(object instanceof HttpResponseContainer){
                        onRequestFinishListener.onRequestFinish((HttpResponseContainer<Res>) object);
                    }else{
                        onNetworkErrorListener.onNetworkError(null, (List<Throwable>)object);
                    }
                }
            };

            return;
        }


        String url;
        int method = getMethod(request.getMethod());

        if (method == Request.Method.POST || method == Request.Method.PUT) {
            url = String.format("%s%s", request.getDomain(), request.getPath());
        } else {
            url = String.format("%s%s%s", request.getDomain(), request.getPath(), request.convertToQueryString());
        }

        NetworkResponseRequest netRequest = new NetworkResponseRequest(method, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {

                try {
                    HttpResponseContainer<Res> container = new HttpResponseContainer<>();
                    int statusCode = response.statusCode;
                    container.setStatusCode(statusCode);
                    String responseString = parseToString(response);
                    Class clazz = this.getClass();

                    ParameterizedType genericSuperClass = (ParameterizedType) clazz.getGenericSuperclass();
                    Type[] actualTypeArguments = genericSuperClass.getActualTypeArguments();
                    Class<Res> responseClass = (Class<Res>) actualTypeArguments[1];

                    Gson gson = new Gson();

                    JsonObject responseJson = (JsonObject) new JsonParser().parse(responseString);


                    boolean succeeded = responseJson.get("status").getAsBoolean();
                    container.setSucceeded(succeeded);

                    if (succeeded) {
                        JsonElement data = responseJson.get("data");
                        Res responseDto = gson.fromJson(data, responseClass);
                        container.setResponseDto(responseDto);

                    } else {
                        JsonElement errorJson = responseJson.get("error");
                        HttpResponseContainer.RcError error = gson.fromJson(errorJson, HttpResponseContainer.RcError.class);
                        container.setError(error);
                    }

                    onRequestFinishListener.onRequestFinish(container);
                } catch (Throwable th) {

                    ArrayList<Throwable> exceptionList = new ArrayList<>();
                    exceptionList.add(th);
                    onNetworkErrorListener.onNetworkError(null, exceptionList);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onNetworkErrorListener.onNetworkError(error, new ArrayList<Throwable>());
            }
        });

        if (method == Request.Method.POST || method == Request.Method.PUT) {
            netRequest.addParam(request);
        }
        requestQueue.add(netRequest);


    }

    protected String getMockJasonString(Req request) {
        return null;
    }


    private class NetworkResponseRequest extends Request<NetworkResponse> {

        private Response.Listener<NetworkResponse> listener;

        private final Map<String, String> postParam = new HashMap<>();

        public NetworkResponseRequest(int method, String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.listener = listener;
        }

        @Override
        protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
            return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
        }

        @Override
        protected void deliverResponse(NetworkResponse response) {
            listener.onResponse(response);
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return super.getParams();
        }

        public void addParam(Req request) {

            Field[] declaredFields = request.getClass().getDeclaredFields();
            StringBuilder queryBuilder = new StringBuilder("?");

            for (Field field : declaredFields) {
                try {
                    field.setAccessible(true);
                    Object paramValue = field.get(this);

                    if (paramValue instanceof Bitmap) {
                        continue;
                    }

                    Annotation[] annotations = field.getDeclaredAnnotations();

                    BaseHttpRequestDto.ParamName annotation = null;
                    for (Annotation declaredAnnotation : annotations) {
                        if (declaredAnnotation instanceof BaseHttpRequestDto.ParamName) {

                            annotation = (BaseHttpRequestDto.ParamName) declaredAnnotation;
                            break;
                        }

                    }

                    if (paramValue == null || annotation == null) {
                        continue;
                    }
                    String paramName = annotation.value();

                    BaseHttpRequestDto.AvoidEncode avoidEncode = field.getAnnotation(BaseHttpRequestDto.AvoidEncode.class);
//                    BaseHttpRequestDto.AvoidEncode avoidEncode = field.getAnnotation(BaseHttpRequestDto.AvoidEncode.class);


//                    queryBuilder.append(URLEnc.encode(paramName));
//                    queryBuilder.append("=");

                    if (!(paramValue instanceof String)) {
//                        queryBuilder.append(((Integer) paramValue).intValue());
                        postParam.put(paramName, paramValue.toString());
                    } else {
                        if (avoidEncode != null) {
                            queryBuilder.append((String) paramValue);
                        } else {
                            queryBuilder.append(URLEnc.encode((String) paramValue));
                        }
                    }
//
//                    } else {
//                        throw new RuntimeException(
//                                "HttpRequestParameterDtoのメンバ変数は、StringとInteger以外使わないでください");
//                    }

//                    queryBuilder.append("&");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            }


        }
    }

    private String parseToString(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return parsed;
    }

    private int getMethod(BaseHttpRequestDto.HttpMethod method) {

        switch (method) {
            case DELETE:
                return Request.Method.DELETE;
            case GET:
                return Request.Method.GET;
            case POST:
                return Request.Method.POST;
            case PUT:
                return Request.Method.PUT;
            default:
                throw new RuntimeException();
        }
    }

    public interface OnRequestFinishListener<Res extends BaseHttpResponseDto> {
        void onRequestFinish(HttpResponseContainer<Res> responseContainer);
    }

    public interface OnNetworkErrorListener {

        void onNetworkError(VolleyError error, List<Throwable> exceptionList);
    }


}
