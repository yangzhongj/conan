# conan

conan是一个异步的消息记录工具。业务逻辑线程将消息放入消息队列中，再由工作线程组将消息队列中的消息取出，并组装成json字符串，最后再通过http请求发送到nginx进行消息汇总，这样多个java后台服务器的消息会汇总在一个地方，方便查找。同时不光是java后台服务器可以使用这个nginx汇总服务，在浏览器前端也可以对这个nginx汇总服务发送http请求记录消息。

## 简单使用

首先到[这里](https://github.com/javacc/conan/releases)下载最新的kid包，conan能独立运行，不依赖任何jar包。
将conan包添加到工程的classpath下，就能使用conan的功能。
请看下面的一段代码
```java
final MsgExecutor me = MsgExecutor.remoteExecutor("concurrent.cc", 80, "log", "p");
for (int i = 0; i < 5; i++) {
    new Thread(new Runnable() {
        @Override
        public void run() {
            for (int j = 0; j < 100; j++) {
                Msg msg = Msg.create().put("msgKey" + j, "msgValue" + j).put("key", "value");
                me.handle(msg);
            }
        }
    }).start();
}
Thread.sleep(1000);
me.stop();
```
上面的代码中，5个线程分别处理了100个消息，这些消息会被拼装成json字符串，发送到concurrent.cc，拼接成的url为http://concurrent.cc/log?p=json字符串，这样只需要打开nginx汇总服务的access log并记录http请求的参数，就能得到这些json字符串的消息了。
