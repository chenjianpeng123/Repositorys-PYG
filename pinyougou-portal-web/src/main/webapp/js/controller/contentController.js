app.controller('contentController',function ($scope, contentService) {
    //定义广告集合 （页面中有多个广告）
    $scope.contentList=[];
    //根据广告分类id查询广告列表数据
    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (response) {
                //把广告的id封装到集合中用哪个传那个 （因为有多条广告）
                $scope.contentList[categoryId]=response;
            }
        );
    }
});