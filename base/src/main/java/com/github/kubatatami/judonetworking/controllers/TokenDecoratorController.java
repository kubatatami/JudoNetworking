package com.github.kubatatami.judonetworking.controllers;

import com.github.kubatatami.judonetworking.Endpoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Kuba on 24/02/14.
 */
public class TokenDecoratorController<T, Z> extends ProtocolControllerWrapper {

    protected String login;

    protected String password;

    protected String keyName;

    protected int tokenLifetime; //min

    protected Method authMethod;

    protected Class<T> authInterface;

    protected Field keyField;

    protected ProtocolController baseController;

    protected TokenCaller tokenCaller = new SimpleTokenCaller();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AuthMethod {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface AuthKey {

    }


    public TokenDecoratorController(ProtocolController baseController, String keyName, int tokenLifetime) {
        super(baseController);
        this.baseController = baseController;
        this.keyName = keyName;
        this.tokenLifetime = tokenLifetime;
        Type[] genericTypes = ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments();
        if (genericTypes.length != 2) {
            throw new RuntimeException("JsonCustomModelController must have two generic types!");
        }
        authMethod = findAuthMethod((Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
        keyField = findKeyField((Class<Z>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1]);
    }

    private Method findAuthMethod(Class<T> authInterface) {
        this.authInterface = authInterface;
        for (Method method : authInterface.getMethods()) {
            if (method.getAnnotation(AuthMethod.class) != null) {
                return method;
            }
        }
        throw new RuntimeException("No AuthMethod annotation on any method");
    }

    private Field findKeyField(Class<Z> resultModel) {
        for (Field field : resultModel.getFields()) {
            if (field.getAnnotation(AuthKey.class) != null) {
                return field;
            }
        }
        return null;
    }


    @Override
    public TokenCaller getTokenCaller() {
        return tokenCaller;
    }

    protected class SimpleTokenCaller extends TokenCaller {

        T api;

        @Override
        public long doTokenRequest(Endpoint endpoint) throws Exception {
            if (api == null) {
                api = endpoint.getService(authInterface);
            }
            Z loginData = (Z) authMethod.invoke(api, login, password);
            setApiKey(keyName, getKey(loginData));
            return System.currentTimeMillis() + tokenLifetime * 1000 * 60;
        }

        @Override
        public boolean checkIsTokenException(Exception exception) {
            return TokenDecoratorController.this.checkIsTokenException(exception);
        }

    }

    protected String getKey(Z loginModel) throws Exception {
        if (keyField == null) {
            throw new RuntimeException("No AuthKey annotation on any field");
        } else {
            return (String) keyField.get(loginModel);
        }
    }

    protected boolean checkIsTokenException(Exception exception) {
        return false;
    }

    public void setLoginAndPassword(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public ProtocolController getBaseController() {
        return baseController;
    }
}
