package cn.pinyougou.sellergoods.service.impl;

import java.util.List;


import cn.pinyougou.mapper.TbTypeTemplateMapper;
import cn.pinyougou.pojo.TbItemCatExample;
import cn.pinyougou.pojogroup.ItemCat;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import cn.pinyougou.mapper.TbItemCatMapper;
import cn.pinyougou.pojo.TbItemCat;
import cn.pinyougou.sellergoods.service.ItemCatService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbTypeTemplateMapper tbTypeTemplateMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbItemCat> findAll() {
        return itemCatMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(ItemCat itemCat) {
        TbItemCat tbItemCat = new TbItemCat();
        tbItemCat.setId(itemCat.getId());
        tbItemCat.setName(itemCat.getName());
        tbItemCat.setParentId(itemCat.getParentId());
        tbItemCat.setTypeId(itemCat.getTypeTemplate().getId());
        itemCatMapper.insert(tbItemCat);


    }


    /**
     * 修改
     */
    @Override
    public void update(TbItemCat itemCat) {
        itemCatMapper.updateByPrimaryKey(itemCat);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public ItemCat findOne(Long id) {
        TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(id);
        ItemCat itemCat = new ItemCat();
        itemCat.setId(tbItemCat.getId());
        itemCat.setName(tbItemCat.getName());
        itemCat.setParentId(tbItemCat.getParentId());
        itemCat.setTypeTemplate(tbTypeTemplateMapper.selectByPrimaryKey(tbItemCat.getTypeId()));
        return itemCat;
        // return itemCatMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            itemCatMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbItemCatExample example = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = example.createCriteria();

        if (itemCat != null) {
            if (itemCat.getName() != null && itemCat.getName().length() > 0) {
                criteria.andNameLike("%" + itemCat.getName() + "%");
            }

        }

        Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据上级id 查询列表
     *
     * @param parentId
     * @return
     */
    @Override
    public List<TbItemCat> findByParentId(Long parentId) {
        TbItemCatExample catExample = new TbItemCatExample();
        TbItemCatExample.Criteria criteria = catExample.createCriteria();
        //设置条件
        criteria.andParentIdEqualTo(parentId);
        //每次执行查询的时候，一次性读取缓存进行存储 (因为每次增删改都要执行此方法)
        //把模板id放入缓存以商品分类名称为key 模板id作为value
        List<TbItemCat> itemCatList = findAll();
        for (TbItemCat itemCat : itemCatList) {
            redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
        }
        System.out.println("更新缓存:商品分类表");
        //条件查询
        return itemCatMapper.selectByExample(catExample);
    }


}
