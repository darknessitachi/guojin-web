$(window).load(function(){
	var width = $('body').width();
    $('.content').css('width',Number(width)-150).show();
    $('.index-sort-box').css('width',Number($('.content').width())-210).show();
});
$(window).resize(function(){
	var width = $('body').width();
	$('.content').css('width',Number(width)-150).show();
    $('.index-sort-box').css('width',Number($('.content').width())-210).show();
});
$(function(){
    //sideBar点击事件
    $('.side-bar-menu').click(function(){
        $(this).addClass('side-bar-menu-click');
        $('.side-bar-menu').not($(this)).removeClass('side-bar-menu-click');
    });
    $('.side-bar-menu-more').click(function(){
      if($(this).hasClass('side-bar-menu-more-click')){
        $(this).removeClass('side-bar-menu-more-click');
        $(this).parent().css('height','auto');
        $(this).children('.icon-open').removeClass('icon-close');
      }else{
        $(this).addClass('side-bar-menu-more-click');
        $(this).parent().css('height','36px');
        $(this).children('.icon-open').addClass('icon-close');
      }
    });
    //筛选
    $('.screening-tab ul li').click(function(){
        var indexNub = $(this).index();
        var $screening = $('.screening-box .chart-choose');
        $(this).addClass('screening-tab-click');
        $('.screening-tab ul li').not($(this)).removeClass('screening-tab-click');
        $screening.eq(indexNub).show();
        $screening.not($screening.eq(indexNub)).hide();
    });
    $('.screening-table-tab ul li').click(function(){
        var indexNubTab = $(this).index();
        var $screeningTable = $('.search-table-wrapper');
        $(this).addClass('screening-table-click');
        $('.screening-table-tab ul li').not($(this)).removeClass('screening-table-click');
        $screeningTable.eq(indexNubTab).show();
        $screeningTable.not($screeningTable.eq(indexNubTab)).hide();
    });
    //头部选择网站select
    $(document).bind("click",function(e){ 
        var target = $(e.target); 
        if(target.closest(".select").length == 0){ 
            $(".option").hide(); 
        }
        else if(target.closest(".option").length != 0){
            $('.option').hide();
        }
        else{
            $('.option').show();
        }
    }); 
    //点击省份展开地区
    $('.icon-close').click(function() {
        if($(this).hasClass('icon-open')){
            $(this).removeClass('icon-open');
            $(this).parent().parent().next('.province-box').hide();
        }else{
            $(this).addClass('icon-open');
            $(this).parent().parent().next('.province-box').show();
        }
    });
 
    //弹框
    $('.popur-close-btn').click(function() {
        $('.popur-box').hide();
        $('.popur-layer').hide();
    });
    $('#add-heat-page').click(function() {
        $('#popur-heat-map').show();
        $('.popur-layer').show();
    });
    $('.send-email-btn').click(function() {
        $('#popur-send-email').show();
        $('.popur-layer').show();
    });
    //location-tab
    $('.location ul li').click(function() {
       var indexNub = $(this).index();
       var $tabBox = $(this).parent().parent().parent().find('.row');
       $(this).addClass('location-tab');
       $('.location ul li').not($(this)).removeClass('location-tab');
       $tabBox.eq(indexNub).show();
       $tabBox.not($tabBox.eq(indexNub)).hide();
    });
//    //筛选条件
//    $('.icon-plus').click(function(event) {
//        if ($(event.target).is('.icon-plus')) {
//            $(this).children('.screening-children').toggle();
//        }
//        else if($(event.target).is('.screening-btn')){
//            $('#filter-list').append('<li><span>条件1</span><span class="screening-filter-close" onclick="$(this).parent().remove()">X</span></li>');
//            $('.screening-children').hide();
//        }
//    });
//    $('.screening-filter-close').click(function() {
//        $(this).parent().remove();
//    });
    //点击显示饼状图
    $('#show-pie-chart').click(function() {
        if($(this).html()=='显示饼状图'){
            $('#pie-chart-wrapper').show();
            $(this).html('隐藏饼状图');
        }else{
            $('#pie-chart-wrapper').hide();
            $(this).html('显示饼状图');
        }
    });
    //用户群定制删除
    $('.icon-close-red').click(function() {
        $(this).parent().parent('li').remove();
    });
    //指标排序
    $('.index-choose-box input').click(function() {
        var inputText = $(this).parent().children('span').html();
        $('.index-sort-box').append('<div class="index-sort-list"><div class="icon icon-close-red"></div><span>'+inputText+'</span></div>');
    });
    //选择星期几
    $('.week-choose-box p').click(function() {
        $(this).addClass('week-choose-click');
        $('.week-choose-box p').not($(this)).removeClass('week-choose-click');
    });
    $('.month-choose-box span').click(function() {
        $(this).addClass('month-choose-click');
        $('.month-choose-box span').not($(this)).removeClass('month-choose-click');
    });
    $('.cycle-plan-choose li input').click(function() {
        var indexNub = $(this).parent().index();
        var $cyclePlan01 = $('.cycle-plan-01 ul');
        var $cyclePlan02 = $('.cycle-plan-02 ul');
        $cyclePlan01.eq(indexNub-1).show();
        $cyclePlan01.not($cyclePlan01.eq(indexNub-1)).hide();
        $cyclePlan02.eq(indexNub-1).show();
        $cyclePlan02.not($cyclePlan02.eq(indexNub-1)).hide();
    });
    //页面上下游点击效果
    $('.page-down-parent-arrow-left').click(function() {
        var indexNub = Number($(this).parent().index());
        var $thisDiv = $(this).parent().parent().find('.page-down-parent-arrow-left').not($(this));
        var mapTop = 90*indexNub;
        $(this).hide();
        $(this).parent().find('.page-down-arrow-left').show();
        $thisDiv.show();
        $thisDiv.parent().find('.page-down-arrow-left').hide();
        $(this).parent().addClass('show-flag');
        $thisDiv.parent().removeClass('show-flag');
        $('#page-down-box-01').show();
        $('#page-down-box-01').animate({'margin-top':mapTop}, 500);
    });
    $('.page-down-parent-arrow-right').click(function() {
        var indexNub = $(this).parent().index();
        var $thisDiv = $(this).parent().parent().find('.page-down-parent-arrow-right').not($(this));
        var mapTop = 90*indexNub;
        var marTopPrev = 90*(indexNub+1);
        $(this).hide();
        $(this).parent().find('.page-down-arrow-right').show();
        $thisDiv.show();
        $thisDiv.parent().find('.page-down-arrow-right').hide();
        $(this).parent().addClass('show-flag');
        $thisDiv.parent().removeClass('show-flag');
        if($(this).parent().parent().attr('id')=="page-down-box-02"){
            $('#page-down-box-03').show();
            $('#page-down-box-03').animate({'margin-top':mapTop}, 500);
        }
        else if($(this).parent().parent().attr('id')=="page-down-box-03"){
            var indexNub = $(this).parent().index();
            var parent_margintop = $(this).parent().parent().css('margin-top');
            var mapTop = 90*indexNub;            
            $('#page-down-box-04').show();
            $('#page-down-box-04').animate({'margin-top':parseInt(parent_margintop)+mapTop}, 500);
        }
    });
    $('.select-arrow').click(function (event) {    
        //取消事件冒泡    
        event.stopPropagation();  
        //按钮的toggle,如果div是可见的,点击按钮切换为隐藏的;如果是隐藏的,切换为可见的。    
        $('.flow-chart-center-option').toggle();    
    });    
    //点击空白处或者自身隐藏弹出层，下面分别为滑动和淡出效果。    
    $(document).click(function (event) { $('.flow-chart-center-option').hide()});    
    $('.flow-chart-center-option').click(function (event) { $(this).hide()});    
    //页面上下游设置弹框
//    $('.pages-down-delete-icon').click(function() {
//        $(this).parent().remove();
//    });
    $('#pages-down-btn').click(function() {
        $('.pages-down-popur').show();
        $('.popur-layer').show();
    });
});
