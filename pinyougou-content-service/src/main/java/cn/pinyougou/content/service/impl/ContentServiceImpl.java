package cn.pinyougou.content.service.impl;
import java.util.List;

import cn.pinyougou.content.service.ContentService;
import cn.pinyougou.pojo.TbContentExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import cn.pinyougou.mapper.TbContentMapper;
import cn.pinyougou.pojo.TbContent;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		//清除缓存
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		contentMapper.insert(content);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//先查询出修改前的分类id
		Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		//清除修改前的缓存
		redisTemplate.boundHashOps("content").delete(categoryId);
		contentMapper.updateByPrimaryKey(content);
		//判断修改前的分类id是否改变 如果改变就清除缓存如果没有则跳过
		if(categoryId.longValue()!=content.getCategoryId().longValue()){
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}

	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//先查询出要删除的广告分类id
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
			//清除缓存
			redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
		}
	}


		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbContentExample example=new TbContentExample();
		TbContentExample.Criteria criteria = example.createCriteria();

		if(content!=null){
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}

		}

		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据条件查询广告列表数据
	 * @param categoryId
	 * @return
	 */
	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		//获取缓存中的数据
		List<TbContent> list = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);

		//判断缓存中是否有数据
         if(list == null){
			 System.out.println("缓存中没有数据");
			 TbContentExample example = new TbContentExample();
			 TbContentExample.Criteria criteria = example.createCriteria();
			 criteria.andCategoryIdEqualTo(categoryId);//传递id查询广告列表
			 criteria.andStatusEqualTo("1");//指定条件 查询有效状态
			 example.setOrderByClause("sort_order");//排序查询
			 list = contentMapper.selectByExample(example);
			 //把数据存入缓存中
			 redisTemplate.boundHashOps("content").put(categoryId,list);
		 }else {
			 System.out.println("缓存中有数据");
		 }
		return list;
	}

}
