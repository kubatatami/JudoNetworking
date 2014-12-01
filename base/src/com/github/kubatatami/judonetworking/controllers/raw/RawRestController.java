package com.github.kubatatami.judonetworking.controllers.raw;


import com.github.kubatatami.judonetworking.ProtocolController;
import com.github.kubatatami.judonetworking.ReflectionCache;
import com.github.kubatatami.judonetworking.RequestInputStreamEntity;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbogacki
 * Date: 16.09.2013
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 */
public class RawRestController extends RawController {

    protected HashMap<String, Object> customKeys = new HashMap<String, Object>();

    public void addCustomKey(String name, Object value) {
        customKeys.put(name, value);
    }

    public void removeCustomKey(String name) {
        customKeys.remove(name);
    }

    @Override
    public ProtocolController.RequestInfo createRequest(String url, RequestInterface request) throws JudoException {
        ProtocolController.RequestInfo requestInfo = new ProtocolController.RequestInfo();
        String result;
        Rest ann = ReflectionCache.getAnnotationInherited(request.getMethod(), Rest.class);
        if (ann != null) {
            result = ann.value();
            if (request.getName() != null) {
                result = result.replaceAll("\\{name\\}", request.getName());
            }
            if (request.getArgs() != null) {
                int i = 0;
                for (Object arg : request.getArgs()) {
                    try {
                        result = result.replaceAll("\\{" + i + "\\}", URLEncoder.encode(arg + "", "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        result = result.replaceAll("\\{" + i + "\\}", arg + "");
                    }
                    i++;
                }
            }
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) request.getAdditionalData()).entrySet()) {
                try {
                    result = result.replaceAll("\\{" + entry.getKey() + "\\}", URLEncoder.encode(entry.getValue()+ "", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    result = result.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue() + "");
                }
            }
            if (ReflectionCache.getAnnotationInherited(request.getMethod(), RawPost.class) != null) {
                int i = 0;
                String stringContent = "";
                for (Annotation[] annotations : ReflectionCache.getParameterAnnotations(request.getMethod())) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Post) {
                            stringContent += request.getArgs()[i].toString();
                        }
                    }
                    i++;
                }
                byte[] content=stringContent.getBytes();
                requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(content), content.length);
            } else if (ReflectionCache.getAnnotationInherited(request.getMethod(), FormPost.class) != null) {
                requestInfo.mimeType = "application/x-www-form-urlencoded";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                int i = 0;
                for (Annotation[] annotations : ReflectionCache.getParameterAnnotations(request.getMethod())) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Post) {
                            Object arg = request.getArgs()[i];
                            nameValuePairs.add(new BasicNameValuePair(((Post) annotation).value(), arg == null ? "" : arg.toString()));
                        }
                    }
                    i++;
                }
                byte[] content = URLEncodedUtils.format(nameValuePairs, HTTP.UTF_8).replaceAll("\\+", "%20").getBytes();
                requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(content), content.length);
            } else if (ReflectionCache.getAnnotationInherited(request.getMethod(), FilePost.class) != null) {
                requestInfo.mimeType = "multipart/form-data";
                int i = 0;
                File file=null;
                for (Annotation[] annotations : ReflectionCache.getParameterAnnotations(request.getMethod())) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Post && request.getArgs()[i] instanceof File) {
                            file = (File) request.getArgs()[i];
                            break;
                        }
                    }
                    i++;
                }
                if(file!=null) {
                    try {
                        requestInfo.entity = new RequestInputStreamEntity(new FileInputStream(file), file.length());
                    } catch (FileNotFoundException e) {
                        throw new JudoException("File is not exist.", e);
                    }
                }else{
                    throw new JudoException("No file param.");
                }
            }
            if (!ann.mimeType().equals("")) {
                requestInfo.mimeType = ann.mimeType();
            }
        } else {
            result = request.getName();
        }

        requestInfo.url = url + (url.lastIndexOf("/") != url.length() - 1 ? "/" : "") + result;

        return requestInfo;
    }


    @Override
    public Serializable getAdditionalRequestData() {
        return customKeys;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Rest {
        String value() default "";

        String mimeType() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Post {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface RawPost {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface FormPost {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface FilePost {
    }

    @Override
    public void setApiKey(String name, String key) {
        customKeys.put(name, key);
    }

    @Override
    public void setApiKey(String key) {
        throw new UnsupportedOperationException("You must set key name or use addCustomKey method.");
    }
}
