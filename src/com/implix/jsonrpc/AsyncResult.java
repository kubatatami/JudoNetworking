package com.implix.jsonrpc;

class AsyncResult implements Runnable
    {
        JsonCallback<Object> callback;
        JsonTransactionCallback transaction;
        Object result=null;
        Object results[]=null;
        Exception e=null;


        AsyncResult(JsonTransactionCallback callback,Object results[]) {
            this.results = results;
            this.transaction = callback;
        }

        AsyncResult(JsonTransactionCallback callback,Exception e) {
            this.e = e;
            this.transaction = callback;
        }

        AsyncResult(JsonCallback<Object> callback,Object result) {
            this.result = result;
            this.callback = callback;
        }

        AsyncResult(JsonCallback<Object> callback,Exception e) {
            this.e = e;
            this.callback = callback;
        }

        @Override
        public void run() {
            if(callback!=null)
            {
                if(result!=null)
                {
                    callback.onFinish(result);
                }
                else if(e!=null)
                {
                    callback.onError(e);
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