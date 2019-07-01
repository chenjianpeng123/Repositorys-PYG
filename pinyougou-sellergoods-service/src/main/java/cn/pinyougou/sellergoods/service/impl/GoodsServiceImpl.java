package cn.pinyougou.sellergoods.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.pinyougou.mapper.*;
import cn.pinyougou.pojo.*;
import cn.pinyougou.pojogroup.Goods;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import cn.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbSellerMapper sellerMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //设置为申请的状态
        goods.getGoods().setAuditStatus("0");
        goodsMapper.insert(goods.getGoods());
        //设置id
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        //插入商品扩展数据
        goodsDescMapper.insert(goods.getGoodsDesc());
        //判断是否启用规格
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            //获取规格数据列表集合 遍历集合
            for (TbItem item : goods.getItemList()) {
                //构建标题 SPU名称 + 规格选项值
                String title = goods.getGoods().getGoodsName();//商品名称
                Map<String, Object> map = JSON.parseObject(item.getSpec());//获取规格列表集合数据
                for (String key : map.keySet()) {
                    title += " " + map.get(key);//拼接SUP名称+规格列表数据
                }
                item.setTitle(title);
                setItemValues(goods,item);
                itemMapper.insert(item);
            }
        }else {//没有启用规格
            TbItem item = new TbItem();
            //商品 KPU+规格描述串作为SKU 名称
            item.setTitle(goods.getGoods().getGoodsName());
            item.setPrice(goods.getGoods().getPrice());//价格
            item.setStatus("1");//状态
            item.setIsDefault("1"); //是否默认
            item.setNum(99999);//库存数量
            item.setSpec("{}");//空的规格
            setItemValues(goods,item);
            itemMapper.insert(item);
        }
    }

    /**
     * 抽取通用代码
     * @param goods
     * @param item
     */
   public void setItemValues(Goods goods,TbItem item){

       item.setGoodsId(goods.getGoods().getId());//商品SPU编号
       item.setSellerId(goods.getGoods().getSellerId());//商家编号
       item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号
       item.setCreateTime(new Date());//创建日期
       item.setUpdateTime(new Date());//修改日期

       //根据商品分类id查询商品名称
       TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
       item.setCategory(tbItemCat.getName());
       //品牌名称
       TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
       item.setBrand(brand.getName());
       //商家名称
       TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
       item.setSeller(seller.getNickName());
       //获取图片集合(读取第一张图片)
       List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
       if (imageList.size() > 0) {
           item.setImage((String) imageList.get(0).get("url"));
       }
   }
    /**
     * 修改
     */
    @Override
    public void update(TbGoods goods) {
        goodsMapper.updateByPrimaryKey(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbGoods findOne(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        TbGoodsExample.Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
               // criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                //精确查询
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

}
