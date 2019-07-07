package cn.pinyougou.solrutil;

import cn.pinyougou.mapper.TbItemMapper;
import cn.pinyougou.pojo.TbItem;
import cn.pinyougou.pojo.TbItemExample;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 批量导入数据
 */
@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 查询数据
     */
    public void importItemData() {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过才能导入
        List<TbItem> itemList = itemMapper.selectByExample(example);
        System.out.println("---商品列表---");
        for (TbItem item : itemList) {
            System.out.println(item.getId() + " " + item.getTitle() + " " + item.getPrice());
            //动态域中的规格数据
            Map specMap = JSON.parseObject(item.getSpec(), Map.class);//将spec中的json字符串转换为Map
            item.setSpecMap(specMap);//给带注解的字段赋值
        }
        //导入数据
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("---结束---");
    }

    public static void main(String[] args) {
        //加载配置文件
        ApplicationContext app = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrutil = (SolrUtil) app.getBean("solrUtil");
        solrutil.importItemData();
    }
}
