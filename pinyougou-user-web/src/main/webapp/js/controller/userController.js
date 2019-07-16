 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
	// $controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		userService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		userService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		userService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=userService.update( $scope.entity ); //修改  
		}else{
			serviceObject=userService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		userService.dele( $scope.selectIds ).success(
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
		userService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//注册
	$scope.reg=function () {
		//判断两次输入的密码是否一致
		if($scope.entity.password != $scope.password){
			alert("你输入的密码有误！！！");
			$scope.entity.password="";
			$scope.password="";
			return;
		}
		userService.add($scope.entity,$scope.smsCode).success(
			function (response) {
				alert(response.message);
			}
		);
	}
    //发送短信验证码
	$scope.sendCode=function () {
		//校验手机号码
		var reg_telephone = new RegExp("^(13[09]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$");
		//判断手机号码是否为空
		if($scope.entity.phone==null || $scope.entity.phone==""){
			alert("请填写手机号码");
			return;
		}
		//校验
		if(!reg_telephone.test($scope.entity.phone)){
			alert("手机号码不合法！！！");
			return;
		}
        userService.sendCode($scope.entity.phone).success(
        	function (response) {
				alert(response.message);
			}
		)
	}
});	
