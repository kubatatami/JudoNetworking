package com.implix.jsonrpc;

class JsonAsyncResult implements Runnable
    {
        JsonCallback<Object> callback;
        JsonBatch<?> transaction;
        Object result=null;
        Object results[]=null;
        Exception e=null;


        JsonAsyncResult(JsonBatch<?> callback, Object results[]) {
            this.results = results;
            this.transaction = callback;
        }

        JsonAsyncResult(JsonBatch<?> callback, Exception e) {
            this.e = e;
            this.transaction = callback;
        }

        JsonAsyncResult(JsonCallback<Object> callback, Object result) {
            this.result = result;
            this.callback = callback;
        }

        JsonAsyncResult(JsonCallback<Object> callback, Exception e) {
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