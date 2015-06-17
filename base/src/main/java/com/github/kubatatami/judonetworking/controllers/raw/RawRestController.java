package com.github.kubatatami.judonetworking.controllers.raw;


import com.github.kubatatami.judonetworking.Request;
import com.github.kubatatami.judonetworking.controllers.ProtocolController;
import com.github.kubatatami.judonetworking.exceptions.JudoException;
import com.github.kubatatami.judonetworking.internals.streams.RequestInputStreamEntity;
import com.github.kubatatami.judonetworking.utils.ReflectionCache;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
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

    protected HashMap<String, Object> customGetKeys = new HashMap<>();
    protected HashMap<String, Object> customPostKeys = new HashMap<>();

    public void addCustomGetKey(String name, Object value) {
        customGetKeys.put(name, value);
    }

    public void removeCustomGetKey(String name) {
        customGetKeys.remove(name);
    }

    public Object getCustomGetKey(String name) {
        return customGetKeys.get(name);
    }

    public void addCustomPostKey(String name, Object value) {
        customPostKeys.put(name, value);
    }

    public void removeCustomPostKey(String name) {
        customPostKeys.remove(name);
    }

    public Object getCustomPostKey(String name) {
        return customPostKeys.get(name);
    }

    @Override
    public ProtocolController.RequestInfo createRequest(String url, Request request) throws JudoException {
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
            AdditionalRequestData additionalRequestData = (AdditionalRequestData) request.getAdditionalData();
            for (Map.Entry<String, Object> entry : additionalRequestData.getCustomGetKeys().entrySet()) {
                try {
                    result = result.replaceAll("\\{" + entry.getKey() + "\\}", URLEncoder.encode(entry.getValue() + "", "UTF-8"));
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
                byte[] content = stringContent.getBytes();
                requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(content), content.length);
            } else if (ReflectionCache.getAnnotationInherited(request.getMethod(), FormPost.class) != null) {
                requestInfo.mimeType = "application/x-www-form-urlencoded";
                List<NameValuePair> noEncodeNameValuePairs = new ArrayList<>();
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                int i = 0;
                for (Annotation[] annotations : ReflectionCache.getParameterAnnotations(request.getMethod())) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Post) {
                            addFormPostParam(nameValuePairs, ((Post) annotation).value(), request.getArgs()[i]);
                        } else if (annotation instanceof AdditionalPostParam) {
                            AdditionalPostParam additionalPostParam = (AdditionalPostParam) annotation;
                            if (additionalPostParam.urlEncode()) {
                                nameValuePairs.addAll((java.util.Collection<? extends NameValuePair>) request.getArgs()[i]);
                            } else {
                                noEncodeNameValuePairs.addAll((java.util.Collection<? extends NameValuePair>) request.getArgs()[i]);
                            }
                        }
                    }
                    i++;

                }
                for (Map.Entry<String, Object> entry : additionalRequestData.getCustomPostKeys().entrySet()) {
                    addFormPostParam(nameValuePairs, entry.getKey(), entry.getValue());
                }
                String formRequest = URLEncodedUtils.format(nameValuePairs, HTTP.UTF_8).replaceAll("\\+", "%20");
                for (NameValuePair nameValuePair : noEncodeNameValuePairs) {
                    formRequest += "&" + nameValuePair.getName() + "=" + nameValuePair.getValue();
                }
                byte[] content = formRequest.getBytes();
                requestInfo.entity = new RequestInputStreamEntity(new ByteArrayInputStream(content), content.length);
            } else if (ReflectionCache.getAnnotationInherited(request.getMethod(), FilePost.class) != null) {
                requestInfo.mimeType = "multipart/form-data";
                int i = 0;
                File file = null;
                for (Annotation[] annotations : ReflectionCache.getParameterAnnotations(request.getMethod())) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Post && request.getArgs()[i] instanceof File) {
                            file = (File) request.getArgs()[i];
                            break;
                        }
                    }
                    i++;
                }
                if (file != null) {
                    try {
                        requestInfo.entity = new RequestInputStreamEntity(new FileInputStream(file), file.length());
                    } catch (FileNotFoundException e) {
                        throw new JudoException("File is not exist.", e);
                    }
                } else {
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

    protected void addFormPostParam(List<NameValuePair> nameValuePairs, String name, Object arg) {
        if (arg != null && arg instanceof Map<?, ?>) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) arg).entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(name + "[" + entry.getKey().toString() + "]", entry.getValue() == null ? "" : entry.getValue().toString()));
            }
        } else if (arg != null && (arg instanceof List<?> || arg.getClass().isArray())) {
            for (Object obj : (Iterable<?>) arg) {
                nameValuePairs.add(new BasicNameValuePair(name + "[]", obj == null ? "" : obj.toString()));
            }
        } else {
            nameValuePairs.add(new BasicNameValuePair(name, arg == null ? "" : arg.toString()));
        }
    }

    protected static class AdditionalRequestData implements Serializable {

        private static final long serialVersionUID = -5849466248972640154L;
        protected HashMap<String, Object> customGetKeys;
        protected HashMap<String, Object> customPostKeys;

        protected AdditionalRequestData(HashMap<String, Object> customGetKeys, HashMap<String, Object> customPostKeys) {
            this.customGetKeys = new HashMap<>(customGetKeys);
            this.customPostKeys = new HashMap<>(customPostKeys);
        }

        public HashMap<String, Object> getCustomGetKeys() {
            return customGetKeys;
        }

        public HashMap<String, Object> getCustomPostKeys() {
            return customPostKeys;
        }
    }

    @Override
    public Serializable getAdditionalRequestData() {
        return new AdditionalRequestData(customGetKeys, customPostKeys);
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
    @Target(ElementType.PARAMETER)
    public @interface AdditionalPostParam {
        boolean urlEncode() default true;
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
        customGetKeys.put(name, key);
    }

    @Override
    public void setApiKey(String key) {
        throw new UnsupportedOperationException("You must set key name or use addCustomKey method.");
    }
}
