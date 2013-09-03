package cc.concurrent.conan.core.consumer;

import cc.concurrent.conan.Msg;

import java.util.List;

/**
 * User: yanghe.liang
 * Date: 13-8-27
 * Time: 下午10:25
 */
public interface Consumer {

    /**
     * 处理消息
     * @param msgs
     * @return
     */
    public boolean handle(List<Msg> msgs);

    /**
     * 检测异常
     * @return
     */
    public boolean check();

}
