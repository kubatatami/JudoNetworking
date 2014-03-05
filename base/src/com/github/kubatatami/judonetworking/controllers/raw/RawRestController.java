package com.github.kubatatami.judonetworking.controllers.raw;


import com.github.kubatatami.judonetworking.ProtocolController;
import com.github.kubatatami.judonetworking.RequestInputStreamEntity;
import com.github.kubatatami.judonetworking.RequestInterface;
import com.github.kubatatami.judonetworking.exceptions.JudoException;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayInputStream;
import java.io.StringBufferInputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
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

    protected Map<String, Object> customKeys = new HashMap<String, Object>();

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
        Rest ann = request.getMethod().getAnnotation(Rest.class);
        if (ann != null) {
            result = ann.value();
            if (request.getName() != null) {
                result = result.replaceAll("\\{name\\}", request.getName());
            }
            if (request.getArgs() != null) {
                int i = 0;
                for (Object arg : request.getArgs()) {
                    result = result.replaceAll("\\{" + i + "\\}", arg + "");
                    i++;
                }
            }
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) request.getAdditionalData()).entrySet()) {
                result = result.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue() + "");
            }
            String content=null;
            if(ann.postMode() == PostMode.RAW){
                int i=0;
                for(Annotation[] annotations : request.getMethod().getParameterAnnotations()){
                    for(Annotation annotation : annotations){
                        if(annotation instanceof Post){
                            if(content==null){
                                content=request.getArgs()[i].toString();
                            }else{
                                content+=request.getArgs()[i].toString();
                            }
                        }
                    }
                    i++;
                }
            }else if(ann.postMode() == PostMode.FORM){
                requestInfo.mimeType = "application/x-www-form-urlencoded";
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                int i=0;
                for(Annotation[] annotations : request.getMethod().getParameterAnnotations()){
                    for(Annotation annotation : annotations){
                        if(annotation instanceof Post){
                            Object arg = request.getArgs()[i];
                            nameValuePairs.add(new BasicNameValuePair(((Post)annotation).value(), arg == null ? "" : arg.toString()));
                        }
                    }
                    i++;
                }
                content= URLEncodedUtils.format(nameValuePairs, HTTP.UTF_8).replaceAll("\\+", "%20");
            }
            if(content!=null){
                if(ann.mimeType()!=null){
                    requestInfo.mimeType = ann.mimeType();
                }
                requestInfo.entity = new RequestInputStreamEntity(new StringBufferInputStream(content),content.length());

            }
        } else {
            result = request.getName();
        }

        requestInfo.url = url + (url.lastIndexOf("/") != url.length()-1 ? "/" : "") + result;



        return requestInfo;
    }


    @Override
    public Object getAdditionalRequestData() {
        return customKeys;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Rest {
        String value() default "";
        PostMode postMode() default PostMode.NONE;
        String mimeType() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Post {
        String value() default "";
    }

    public enum PostMode{
        NONE,RAW,FORM
    }

    @Override
    public void setApiKey(String name, String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setApiKey(String key) {
        throw new UnsupportedOperationException();
    }
}
