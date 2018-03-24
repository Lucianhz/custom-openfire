$(document).ready(function(){
  $(".footer-div-ul .item").click(function(){
    var index = $(this).index();
    $(".footer-div-ul .item").removeClass("active");
    $(this).addClass("active");
    
    $(".footer-div-ul1 .item").hide();
    $(".footer-div-ul1 .item").eq(index).show();
  });
})