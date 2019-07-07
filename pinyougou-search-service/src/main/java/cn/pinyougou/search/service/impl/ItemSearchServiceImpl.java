package cn.pinyougou.search.service.impl;

import cn.pinyougou.pojo.TbItem;
import cn.pinyougou.search.service.ItemSearchService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        Map map = new HashMap();
        //把返回的列表数据map集合 存入Map中 查询列表
        map.putAll(searchList(searchMap));
        //分组查询商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        //把分类列表添加到map集合中
        map.put("categoryList", categoryList);
        //查询品牌和规格列表
        if(categoryList.size()>0){
            //添加到Map集合
            map.putAll(searchBrandAndSpecList(categoryList.get(0)));
        }
        //返回结果
        return map;
    }

    /**
     * 查询列表
     *
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        //高亮显示查询
        HighlightQuery query = new SimpleHighlightQuery();
        //高亮选项对象
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//在哪显示高亮 高亮域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
        highlightOptions.setSimplePostfix("</em>");//后缀
        query.setHighlightOptions(highlightOptions);//为查询对象设置高亮选项
        //条件 关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //高亮页对象 进行分页查询
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //高亮入口集合（每条记录的高亮入口）
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : entryList) {
            //获取高亮列表 高亮域的个数（域表示字段）
            List<HighlightEntry.Highlight> highlights = entry.getHighlights();
            //判断集合中是否有高亮值
            if (highlights.size() > 0 && highlights.get(0).getSnipplets().size() > 0) {
                //获取集合中的高亮实体
                TbItem item = entry.getEntity();
                //获取集合中的第一个域和获取域中的第一个实体
                item.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }
        //存入map集合中
        map.put("rows", page.getContent());
        //返回结果
        return map;
    }

    /**
     * 查询分组列表
     *
     * @param searchMap
     * @return
     */
    private List<String> searchCategoryList(Map searchMap) {
        //创建list集合
        List<String> list = new ArrayList();
        //创建分组查询对象
        Query query = new SimpleQuery();
        //按照关键字查询设置查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //获取分组页对象
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //根据分组结果集得到分组结果入口页对象
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //根据分组结果入口页对象获取分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        //遍历分组入口集合 每一个元素都是分组入口
        for (GroupEntry<TbItem> entry : content) {
            //将分组结果添加到返回值中
            list.add(entry.getGroupValue());
        }
        //返回list集合
        return list;
    }

    /**
     * 根据商品分类名称查询品牌和规格列表数据
     * @param category 商品分类名称
     * @return
     */
    public Map searchBrandAndSpecList(String category) {
        //创建Map集合
        Map map = new HashMap();
        //根据商品分类名称获取模板id
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        //判断模板id不为null
        if(templateId!= null){
            //根据模板id获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            //将数据存入map集合
            map.put("brandList",brandList);
            //根据模板id获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            //将数据存入map集合
            map.put("specList",specList);
        }
        //返回数据
        return map;
    }
}
