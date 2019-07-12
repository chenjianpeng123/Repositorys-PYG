app.controller('searchController', function ($scope,$location, searchService) {
    //创建搜索对象
    $scope.searchMap = {
        'keywords': '',    //关键字
        'category': '',    //分类
        'brand': '',       //品牌
        'spec': {},        //规格
        'price': '',       //价格
        'pageNo': 1,       //页码
        'pageSize': 40,    //每页记录数
        'sortField':'',    //排序字段
        'sort':''          //是否排序
    };
    //搜索
    $scope.search = function () {
        //转换为int 否则提交到后端有可能变成字符串
        $scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;//返回搜索结果
                buildPageLabel();//调用分页构建
            }
        );
    }

    //添加搜索项
    $scope.addSearchItem = function (key, value) {
        //判断点击的是品牌还是分类或者价钱
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {//如果都不是那就是规格选项
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//执行搜索
    }
    //移除选项
    $scope.removeSearchItem = function (key) {
        if (key == 'category' || key == 'brand' || key == 'price') {//判断点击的是分类还是品牌或者价钱
            $scope.searchMap[key] = ""; //让他的值为空字符串
        } else {//如果都不是就是规格
            delete $scope.searchMap.spec[key];//如果是规格就删除
        }
        $scope.search();//执行搜索
    }

    //构建分页标签
    buildPageLabel = function () {

        $scope.pageLabel = [];//设置分页栏属性
        var maxPageNo = $scope.resultMap.totalPages; //获取总页数
        var firstPage = 1; //开始页码
        var lastPage = maxPageNo;//结束页码
        $scope.firstDot=true;//前面加点
        $scope.lastDot=true;//后面加点
        if($scope.resultMap.totalPages>5){//如果总页数大于 5 页,显示部分页码
           if($scope.searchMap.pageNo<=3){//如果当前页小于等于3
                lastPage = 5;//显示前5页
               $scope.firstDot=false;//取消前面的点
           }else if($scope.searchMap.pageNo>=lastPage-2){//如果当前页码大于等于结束页码-2
               firstPage=maxPageNo-4;//显示后5页
              $scope.lastDot=false;//取消后面的点
           }else {
               //显示当前页为中心的5页
               firstPage=$scope.searchMap.pageNo-2;
               lastPage=$scope.searchMap.pageNo+2;
           }
        }else {
            $scope.firstDot=false;//取消前面的点
            $scope.lastDot=false;//取消后面的点
        }
        //循环产生页码标签
        for (var i = firstPage; i <=lastPage ; i++) {
            $scope.pageLabel.push(i);
        }
    }
    //根据页码查询
    $scope.queryByPage=function (pageNo) {
        //页码验证
        if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();
    }
    //页码不可用的样式
    // 判断当前页为第一页
    $scope.isTopPage=function () {
        if($scope.searchMap.pageNo==1){
            return true;
        }else {
            return false;
        }
    }
    //判断当前页是否是最后一页
    $scope.isEndPage=function () {
        if($scope.searchMap.pageNo == $scope.resultMap.pageNo){
            return true;
        }else{
            return false;
        }
    }
    //设置排序规则
    $scope.sortSearch=function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }
    //隐藏品牌列表 判断关键字是否是品牌
    $scope.keywordsIsBrand=function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                //如果包含返回true
                return true;
            }
        }
        //不包含返回false
        return false;
    }

    //加载查询字符串
    $scope.loadkeywords=function () {
        //传递关键字
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.search();//查询
    }

});