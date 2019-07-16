package cn.pinyougou.page.service.impl;

import cn.pinyougou.mapper.TbGoodsDescMapper;
import cn.pinyougou.mapper.TbGoodsMapper;
import cn.pinyougou.mapper.TbItemCatMapper;
import cn.pinyougou.mapper.TbItemMapper;
import cn.pinyougou.page.service.ItemPageService;
import cn.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;


import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ItemPageServiceImpl implements ItemPageService {


    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Value("${pagedir}")
    private String pagedir;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            //获取配置类
            Configuration configuration = freeMarkerConfig.getConfiguration();
            //加载模板文件
            Template template = configuration.getTemplate("item.ftl");
            //创建数据模型
            Map dataModel = new HashMap();
            //加载商品表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);
            //加载商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);
            //加载商品分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1", itemCat1);
            dataModel.put("itemCat2", itemCat2);
            dataModel.put("itemCat3", itemCat3);
            //加载sku列表数据
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            example.setOrderByClause("is_default desc");//按照是否是默认字段降序排序 保证第一个sku为默认值
            criteria.andStatusEqualTo("1");//查询状态为1的数据
            criteria.andGoodsIdEqualTo(goodsId);//根据商品id查询sku列表数据
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", itemList);

            //获取输出流对象
            Writer out = new FileWriter(pagedir + goodsId + ".html");
            //输出
            template.process(dataModel, out);
            //关闭输出流对象
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除商品详情静态页
     *
     * @param goodsIds
     * @return
     */
    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {

        try {
            for (Long goodsId : goodsIds) {
                new File(pagedir + goodsId + ".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
