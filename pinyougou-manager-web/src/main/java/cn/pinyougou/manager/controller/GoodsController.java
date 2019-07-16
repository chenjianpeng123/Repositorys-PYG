package cn.pinyougou.manager.controller;


import java.util.List;


import cn.pinyougou.pojo.TbItem;
import cn.pinyougou.pojogroup.Goods;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import cn.pinyougou.pojo.TbGoods;
import cn.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.*;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;
    //    @Reference(timeout = 100000)
//    private ItemSearchService itemSearchService;
//    @Reference(timeout = 400000)
//    private ItemPageService itemPageService;


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

//	/**
//	 * 增加
//	 * @param goods
//	 * @return
//	 */
//	@RequestMapping("/add")
//	public Result add(@RequestBody Goods goods){
//		try {
//			goodsService.add(goods);
//			return new Result(true, "增加成功");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new Result(false, "增加失败");
//		}
//	}

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    @Autowired
    @Qualifier("queueSolrDeleteDestination")
    private Destination queueSolrDeleteDestination;//删除solr索引库 点对点

    @Autowired
    @Qualifier("topicPageDeleteDestination")
    private Destination topicPageDeleteDestination;//删除生成的静态页面
    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(final Long[] ids) {
        try {
            goodsService.delete(ids);
//            itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            //删除索引库数据
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    ObjectMessage objectMessage = session.createObjectMessage(ids);
                    return objectMessage;
                }
            });
            //删除静态页面
            jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    ObjectMessage objectMessage = session.createObjectMessage(ids);
                    return objectMessage;
                }
            });
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }


    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("queueSolrDestination")
    private Destination queueSolrDestination;//用于发送solr导入的消息（点对点）
    @Autowired
    @Qualifier("topicPageDestination")
    private Destination topicPageDestination;//生成商品详情静态页  （发布订阅）

    /**
     * 更新状态
     *
     * @param ids
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
            //根据SPU Id查询SKU 列表 （状态为 1 的）
            if ("1".equals(status)) {//审核通过
                //得到需要导入的SKU列表A
                List<TbItem> itemList = goodsService.findItemListByGoodsIdAndStatus(ids, status);
                if (itemList.size() > 0) {
                    //导入数据到solr
                    // itemSearchService.importList(itemList);
                    final String jsonString = JSON.toJSONString(itemList);//将数据转换为json字符串

                    jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            TextMessage textMessage = session.createTextMessage(jsonString);
                            return textMessage;
                        }
                    });
                } else {
                    System.out.println("没有数据！！！");
                }
                //静态页面生成
                for (final Long goodsId : ids) {
//                    itemPageService.genItemHtml(goodsId);

                    jmsTemplate.send(topicPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
//                           //发送的消息是文本类型
                            TextMessage textMessage = session.createTextMessage(goodsId + "");
                            return textMessage;
                        }
                    });
                }
            }
            return new Result(true, "成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "失败");
        }
    }

    ///**
//     * 生成静态页（测试）
//     *
//     * @param goodsId
//     */
//    @RequestMapping("/genHtml")
    public void genHtml(Long goodsId) {
//     itemPageService.genItemHtml(goodsId);
    }


}
