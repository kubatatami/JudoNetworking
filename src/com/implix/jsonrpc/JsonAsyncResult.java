package com.implix.jsonrpc;

class JsonAsyncResult implements Runnable
    {
        JsonCallbackInterface<Object> callback;
        JsonBatchInterface<?> transaction;
        Object result=null;
        Object results[]=null;
        Exception e=null;


        JsonAsyncResult(JsonBatchInterface<?> callback, Object results[]) {
            this.results = results;
            this.transaction = callback;
        }

        JsonAsyncResult(JsonBatchInterface<?> callback, Exception e) {
            this.e = e;
            this.transaction = callback;
        }

        JsonAsyncResult(JsonCallbackInterface<Object> callback, Object result) {
            this.result = result;
            this.callback = callback;
        }

        JsonAsyncResult(JsonCallbackInterface<Object> callback, Exception e) {
            this.e = e;
            this.callback = callback;
        }

        @Override
        public void run() {
            if(callback!=null)
            {
                if(e!=null)
                {
                    callback.onError(e);
                }
                else
                {
                    callback.onFinish(result);
                }
            }
            else
            {
                if(e!=null)
                {
                    transaction.onError(e);
                }
                else
                {
                    transaction.onFinish(results);
                }
            }
        }
    }