//页面详细页
app.controller('itemController',function($scope){
	//记录用户所选的规格
	$scope.specificationItems={};
	 //数量操作
	 $scope.addNum=function(x){
		 $scope.num+=x;
		 if($scope.num<1){
			 $scope.num=1;
		 }
	 }
	 
	//用户选择的规格
	$scope.selectSpecification=function(key,value){
		$scope.specificationItems[key]=value;
		//读取sku数据
		searchSku();
	}
	
	//判断规格是否被用户选中
	$scope.isSelected=function(key,value){
		if($scope.specificationItems[key]==value){
			return true;
		}else{
			return false;
		}
	}
	//当前选择的sku
	$scope.sku={};
	//加载默认的sku列表数据
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;
	}
	
	//匹配两个对象是否相等
	matchObject=function(map1,map2){
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}
		}
		
		return true;
	}
	
  //根据规格查询SKU列表数据
  searchSku=function(){
	  //遍历skuList
	  for(var i=0;i<skuList.length;i++ ){
		  //判断用户选择的和数据库的数据是否相等
		  if(matchObject(skuList[i].spec,$scope.specificationItems)){
			  //当前选择和查出的数据相等
			  $scope.sku=skuList[i];
			  return;
		  }
	
	  }
	  	 //如果没有匹配
      $scope.sku={id:0,title:'没有数据',price:0};		
  }
  
  //添加购物车
  $scope.addToCart=function(){
	  alert('skud:'+$scope.sku.id);
  }
	
});