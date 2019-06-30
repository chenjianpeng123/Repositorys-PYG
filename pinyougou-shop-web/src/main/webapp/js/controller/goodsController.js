 //控制层 
app.controller('goodsController' ,function($scope,$controller ,goodsService,itemCatService,uploadService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.add=function(){
$scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(
			function(response){
				if(response.success){
					alert('保存成功');
		        	$scope.entity={};//清空列表
					editor.html('');//清空文本编辑器
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    //上传图片
	$scope.uploadFile=function () {
		uploadService.uploadFile().success(
			function (response) {
				if(response.success){//如果文件上传成功 取出url
					$scope.image_entity.url= response.message;//设置文件地址
				}else {
					alert(response.message);
				}
			}).error(function () {
			alert('上传时发生错误！');
		});
	}
	//定义页面实体结构
	$scope.entity={goods:{},goodsDesc:{itemImages:[]}};
	//添加图片列表
	$scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

	//移除图片
	$scope.remove_image_entity=function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1);
		
	}
	//显示一级下拉列表数据
	$scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(
			function (response) {
              $scope.itemCat1List=response;
			}
		);
	}
	//显示二级下拉列表数据
	$scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {
     //根据选择的值查询二级分类
		if(newValue){
			itemCatService.findByParentId(newValue).success(
				function (response) {
					$scope.itemCat2List=response;
				}
			);
		}

	});
	//显示三级下拉列表数据
	$scope.$watch('entity.goods.category2Id',function (newValue, oldValue) {
		if(newValue){
			itemCatService.findByParentId(newValue).success(
				function (response) {
					$scope.itemCat3List=response;
				}
			);
		}
	});

	//读取模板ID
	$scope.$watch('entity.goods.category3Id',function(newValue,oldValue){
		if(newValue) {
			itemCatService.findOne(newValue).success(
				function (response) {
					$scope.entity.goods.typeTemplateId = response.typeId;
				}
			);
		}
	});
});
