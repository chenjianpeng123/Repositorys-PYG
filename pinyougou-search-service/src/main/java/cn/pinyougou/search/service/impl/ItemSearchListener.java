package cn.pinyougou.search.service.impl;

import cn.pinyougou.pojo.TbItem;
import cn.pinyougou.search.service.ItemSearchService;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * 监听类
 */
@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        TextMessage textMessage = (TextMessage) message;
        try {
            //获取数据
            String messageText = textMessage.getText();
            System.out.println("监听接收到消息...");
            List<TbItem> itemList = JSON.parseArray(messageText, TbItem.class);//将json字符串转为json集合
            //调用导入方法  导入数据
            itemSearchService.importList(itemList);
            System.out.println("成功导入到索引库");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
