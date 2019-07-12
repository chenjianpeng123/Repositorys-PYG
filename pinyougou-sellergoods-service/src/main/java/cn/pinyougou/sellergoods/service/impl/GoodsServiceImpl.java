package cn.pinyougou.sellergoods.service.impl;

import java.io.Serializable;
import java.sql.Array;
import java.util.Arrays;
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
import com.sun.xml.internal.xsom.impl.scd.Iterators;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import cn.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
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
        goodsMapper.insert(goods.getGoods());//插入商品基本信息
        //设置id//将商品基本表的ID给商品扩展表
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        //插入商品扩展列表数据
        goodsDescMapper.insert(goods.getGoodsDesc());
        //插入商品SKU列表数据数据
        saveItemList(goods);
    }

    /**
     * 抽取：通用代码
     * 封装SKU数据
     *
     * @param goods
     * @param item
     */
    private void setItemValues(Goods goods, TbItem item) {
        //商品分类
        item.setGoodsId(goods.getGoods().getId());//商品SPU编号
        item.setSellerId(goods.getGoods().getSellerId());//商家编号
        item.setCategoryid(goods.getGoods().getCategory3Id());//商品三级分类编号
        item.setCreateTime(new Date());//创建日期
        item.setUpdateTime(new Date());//修改日期
        //分类名称
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
     * 抽取 ：插入SKU列表数据
     *
     * @param goods
     */
    public void saveItemList(Goods goods) {
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
                setItemValues(goods, item);
                itemMapper.insert(item);
            }
        } else {//没有启用规格
            TbItem item = new TbItem();
            //商品 KPU+规格描述串作为SKU 名称
            item.setTitle(goods.getGoods().getGoodsName());
            item.setPrice(goods.getGoods().getPrice());//价格
            item.setStatus("1");//状态
            item.setIsDefault("1"); //是否默认
            item.setNum(99999);//库存数量
            item.setSpec("{}");//空的规格
            setItemValues(goods, item);//SKU数据
            itemMapper.insert(item);
        }
    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //设置为未申请状态 如果修改需要从新设置状态
        goods.getGoods().setAuditStatus("0");
        goodsMapper.updateByPrimaryKey(goods.getGoods());//保存商品表
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());//保存商品扩展表
        //删除原有的SKU列表商品数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(example);
        //添加新的SKU列表数据
        saveItemList(goods);

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        //获取商品基本表
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        //获取商品扩展表
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(tbGoodsDesc);
        // 查询SKU商品列表 查询条件 商品id
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //物理删除
            //goodsMapper.deleteByPrimaryKey(id);
            //逻辑删除
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsDelete("1");
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        TbGoodsExample.Criteria criteria = example.createCriteria();
        //排除已删除的
        criteria.andIsDeleteIsNull();
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

    /**
     * 修改状态
     *
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        //遍历
        for (Long id : ids) {
            //查询商品状态信息
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            //设置审核状态
            goods.setAuditStatus(status);
            //修改状态
            goodsMapper.updateByPrimaryKey(goods);
        }
    }

    /**
     * 根据商品id和状态查询商品列表
     *
     * @param goodsIds
     * @param status
     * @return
     */
    @Override
    public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodsIds, String status) {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //审核状态
        criteria.andStatusEqualTo(status);
        //将商品id转换为list集合 因为有多个商品id
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));
        List<TbItem> list = itemMapper.selectByExample(example);
        return list;
    }
}
