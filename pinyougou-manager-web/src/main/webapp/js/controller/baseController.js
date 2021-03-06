app.controller('baseController', function ($scope) {
    //重新加载列表数据 刷新
    $scope.reloadList = function () {
        //切换页码
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };
    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,//当前页
        totalItems: 10,//总记录数
        itemsPerPage: 10,//每页记录数
        perPageOptions: [10, 20, 30, 40, 50],//分页选项
        //当前页码变更后自动触发的方法
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };
    //用户的勾选复选框设置
    $scope.selectIds = [];//用户勾选的id集合
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id);//向集合添加元素
        } else {
            var index = $scope.selectIds.indexOf(id);//查找值的位置
            $scope.selectIds.splice(index, 1);//参数1 移除的位置，参数2 移除的个数
        }
    };
    //提取json字符串数据中的数据 返回拼接字符串 逗号隔开
    $scope.jsonToString = function (jsonString,key){
        var json = JSON.parse(jsonString);//将json字符串转换为json对象
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if(i>0){
                value+=",";
            }
            value += json[i][key];
        }
        return value;
    }
    //从集合中按照key查询对象
    $scope.searchObjectByKey = function (list, key, keyValue) {
        //遍历集合
        for (var i = 0; i < list.length; i++) {
            //判断集合中是否有对应的值
            if (list[i][key] == keyValue) {
                //返回查到的值
                return list[i];
            }
        }
        //如果没查到返回null
        return null;
    }
});
    
