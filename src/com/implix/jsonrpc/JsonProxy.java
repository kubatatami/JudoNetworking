package com.implix.jsonrpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class JsonProxy implements InvocationHandler {

    String apiKey=null;
    JsonRpcImplementation rpc;
    int id=0;

    public JsonProxy(JsonRpcImplementation rpc)
    {
        this.rpc=rpc;
    }

	public JsonProxy(JsonRpcImplementation rpc,String apiKey)
	{
        this.rpc=rpc;
        this.apiKey=apiKey;
	}

    private Method getMethod(Object obj, String name) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

	public Object invoke(Object proxy, Method m, Object[] args)  throws Throwable
	{
		try
		{
            Method method = getMethod(this,m.getName());
            if(method!=null)
            {
                return method.invoke(this,args);
            }
            else
            {
                String paramNames[]=null;
                String name=m.getName();
                Integer timeout=null;
                boolean async=false,notification=false;
                JsonMethod ann = m.getAnnotation(JsonMethod.class);
                if(ann !=null)
                {
                    if(!ann.name().equals(""))
                    {
                        name=ann.name();
                    }
                    if(ann.paramNames().length>0)
                    {
                        paramNames=ann.paramNames();
                    }
                    async=ann.async();
                    notification=ann.notification();
                    timeout=ann.timeout();
                }
                if(m.getReturnType().equals(Void.TYPE) && !async && notification)
                {
                    rpc.getConnection().notify(name, paramNames, args, timeout, apiKey);
                    return null;
                }
                else if(!async)
                {
                    return rpc.getConnection().call(++id, name, paramNames, args, m.getGenericReturnType(), timeout, apiKey);
                }
                else
                {
                    JsonRequest request = callAsync(++id,name,paramNames,args,m.getGenericParameterTypes(),timeout,apiKey);
                    if(rpc.isTransaction())
                    {
                        rpc.registerAsyncRequest(request);
                        return null;
                    }
                    else
                    {
                        Thread thread = new Thread(request);
                        thread.start();
                        if(m.getReturnType().equals(Thread.class))
                        {
                            return thread;
                        }
                        else
                        {
                            return null;
                        }
                    }

                }
            }
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}


    public JsonRequest callAsync(int id,String name,String[] params,Object[] args,Type[] types, Integer timeout,String apiKey) throws Exception
    {
        Object[] newArgs=null;
        JsonCallback<Object> callback= (JsonCallback<Object>) args[args.length-1];
        if(args.length>1)
        {
            newArgs=new Object[args.length-1];
            System.arraycopy(args, 0, newArgs, 0,args.length-1);
        }
        Type type = ((ParameterizedType)types[args.length-1]).getActualTypeArguments()[0];
        return new JsonRequest(id,rpc,callback,name,params,newArgs,type,timeout,apiKey);
    }

}
