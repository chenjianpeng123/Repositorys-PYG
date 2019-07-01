package cn.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import cn.pinyougou.mapper.TbSpecificationOptionMapper;
import cn.pinyougou.pojo.TbSpecificationOption;
import cn.pinyougou.pojo.TbSpecificationOptionExample;
import cn.pinyougou.pojo.TbTypeTemplateExample;
import cn.pinyougou.sellergoods.service.TypeTemplateService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import cn.pinyougou.mapper.TbTypeTemplateMapper;
import cn.pinyougou.pojo.TbTypeTemplate;


import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbTypeTemplate> findAll() {
        return typeTemplateMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.insert(typeTemplate);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKey(typeTemplate);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbTypeTemplate findOne(Long id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbTypeTemplateExample example = new TbTypeTemplateExample();
        TbTypeTemplateExample.Criteria criteria = example.createCriteria();

        if (typeTemplate != null) {
            if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
                criteria.andNameLike("%" + typeTemplate.getName() + "%");
            }
            if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
                criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
            }
            if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
                criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
            }
            if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
                criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
            }

        }

        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 下拉列表数据
     *
     * @return
     */
    @Override
    public List<Map> selectTypeTemplateList() {
        return typeTemplateMapper.selectTypeTemplateList();
    }

    /**
     * 返回规格列表
     *
     * @param id
     * @return
     */
    @Override
    public List<Map> findSpecList(Long id) {
        //查询模板
        TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
        //获取模板中的规格json字符串通过JSON.parseArray 转换为Map集合
        List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
        //遍历
        for (Map map : list) {
            //根据条件查询规格选项列表
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            //把id转换为Long类型
            criteria.andSpecIdEqualTo( new Long( (Integer)map.get("id") ) );
            //获取出规格选项列表
            List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);
            map.put("options", options);
        }
        //返回list集合
        return list;
    }
}
