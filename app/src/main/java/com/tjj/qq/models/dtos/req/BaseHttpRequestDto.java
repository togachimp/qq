package com.tjj.qq.models.dtos.req;

import android.graphics.Bitmap;

import com.example.tunnel_togashi.togashiremover.utils.URLEnc;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by tunnel_togashi on 15/12/02.
 */
public abstract class BaseHttpRequestDto {

    public enum HttpMethod {GET, POST, PUT, DELETE}

    private HttpMethod method;

    public BaseHttpRequestDto(HttpMethod method) {
        this.method = method;

    }

    public abstract String getDomain();

    public abstract String getPath();


    public HttpMethod getMethod() {
        return method;
    }

    public final String convertToQueryString() {
        Field[] declaredFields = getClass().getDeclaredFields();
        StringBuilder queryBuilder = new StringBuilder("?");

        for (Field field : declaredFields) {
            try {
                field.setAccessible(true);
                Object paramValue = field.get(this);

                if (paramValue instanceof Bitmap) {
                    continue;
                }

                Annotation[] annotations = field.getDeclaredAnnotations();

                ParamName annotation = null;
                for (Annotation declaredAnnotation : annotations) {
                    if (declaredAnnotation instanceof ParamName) {

                        annotation = (ParamName) declaredAnnotation;
                        break;
                    }

                }

                if (paramValue == null || annotation == null) {
                    continue;
                }
                String paramName = annotation.value();
                AvoidEncode avoidEncode = field.getAnnotation(AvoidEncode.class);
                queryBuilder.append(URLEnc.encode(paramName));
                queryBuilder.append("=");

                if (paramValue instanceof Integer) {
                    queryBuilder.append(((Integer) paramValue).intValue());
                } else if (paramValue instanceof String) {
                    if (avoidEncode != null) {
                        queryBuilder.append((String) paramValue);
                    } else {
                        queryBuilder.append(URLEnc.encode((String) paramValue));
                    }
                } else if (paramValue instanceof Float) {
                    queryBuilder.append(((Float) paramValue).floatValue());
                } else if (paramValue instanceof Long) {
                    queryBuilder.append(((Long) paramValue).longValue());
                } else if (paramValue instanceof Boolean) {
                    queryBuilder.append(((Boolean) paramValue).booleanValue());
                } else if (paramValue instanceof List) {
                    // queryBuilder.append("これListよ");
                    List list = (List) paramValue;
                    queryBuilder.append("[");
                    for (Object object : list) {
                        queryBuilder.append(object.toString());
                        if (list.get(list.size() - 1) == object) {
                            queryBuilder.append(",");
                        }
                    }
                    queryBuilder.append("]");

                } else {
                    throw new RuntimeException(
                            "HttpRequestParameterDtoのメンバ変数は、StringとInteger以外使わないでください");
                }

                queryBuilder.append("&");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }

        String ret = queryBuilder.deleteCharAt(queryBuilder.length() - 1).toString();
        return ret;

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ParamName {
        public String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface AvoidEncode {
    }
}
