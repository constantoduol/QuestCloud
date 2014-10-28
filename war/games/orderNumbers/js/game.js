var game = {
    resizables : [],
    resizableIds : [],
    numbers : [2,3,4,5,6,7,8,9,10],
    numberList : [1],
    numberPositions : {},
    lastDrag : 0,
    lastTime : 0,
    gameIsIdle : function(){
      if( (game.lastDrag + 10000) < $.now()){
    	  return true;
      }
      else {
    	 return false;
      }
    },
    init : function(){
    	
    	game.numberList = [1];
    	game.numberPositions = {};
    	game.lastDrag = 0;
    	game.lastTime = 0;
    	
    	soundManager.play("arrange_below"); //play the introduction message
    	
        var width = game.getDim()[0];
        var height = game.getDim()[1];
        
        var body = $("body");
        body.html("");
        body.css("background","url(img/background.png)"); //set the background image
        body.css("background-size",""+width+"px "+height+"px");
        
        var input = $("<input type='text' id='answer_area' disabled='disabled' value=1>"); //set the input 
        input.css("top",height*0.5+"px");
        input.css("left",width*0.005+"px");
        input.css("width",width*0.75+"px");
        input.css("font-size",height*0.09+"px"); 
        
        var numDiv = $("<div id='number_area'></div>"); //initialize the numbers
        numDiv.css("top",0.1*height+"px");
        numDiv.css("font-size",height*0.09+"px");
        
        var kiddoGif = $("<img src='img/talking.gif' id='kiddo_gif' style='position:relative;'>");
        kiddoGif.css("left",0.62*width+"px");
        kiddoGif.css("height",0.35*width+"px");
        kiddoGif.css("top",0.15*height+"px");
        
        body.append(numDiv);
        body.append(input);
        body.append(kiddoGif);
        //the animated talking gif talks only for 8 seconds i.e the duration
        //of the audio, after that we show the waiting gif
        game.runLater(6000,function(){
        	$("#kiddo_gif").attr("src","img/waiting.gif");
        });
        
        
        
        game.numbers = game.shuffle(game.numbers);
        for(var x = 0; x < game.numbers.length; x++ ){
           game.addNumber(game.numbers[x]);
        }
        $("#answer_area").droppable({
            drop: function( event, ui ) {
            	
              var id = ui.helper.context.id;
              var numSpan = $("#"+id);
              var num = parseInt(numSpan.html());
              var lastNum = game.numberList[game.numberList.length - 1];
              
              var width = game.getDim()[0];
              var height = game.getDim()[1];
              
              if( (lastNum + 1) === num ){
            	  
                 numSpan.css("color","green");
                 var ans = $("#answer_area");
                 ans.css("border-color","orange");
                 game.runLater(1000, function(){
                    ans.css("border-color","white");
                 });
                 
                 game.numberList.push(num);
                 
                 numSpan.draggable('disable');
                 numSpan.css("display","none");
                 
                 var value = "";
                 for(var x = 0; x < game.numberList.length; x++ ){
                	 value = value +" "+ game.numberList[x];
                 }
                 ans.val(value);
                 
                 soundManager.play("correct_answer");
                 $("#kiddo_gif").attr("src","img/smile.gif");
                 game.runLater(4000,function(){  //4 seconds is the duration of well done that was correct answer
                 	$("#kiddo_gif").attr("src","img/waiting.gif");
                 });
                 if(num === 10){
                         //end of the game
                	 game.runLater(2000, function(){
                         body.html("<div style='position : relative;font-size:24px;top : "+0.6*height+"px; left :"+0.2*width+"px'>" +
                         		"<a href='javascript:game.init()' title='Replay'>" +
                         		"<img src='img/replay.png'></a>" +
                         		"<br><span>Replay</span>" +
                         		"</div>");
                         body.css("background","url(img/endscreen.png)");
                         body.css("background-size",""+width+"px "+height+"px");
                         clearInterval(game.lastTime);
                	 });
                    }
              }
              else {
            	  soundManager.play("try_again");
            	  $("#kiddo_gif").attr("src","img/sad.gif");
                  game.runLater(4000,function(){  //3 seconds is the duration of oh no please try again
                  	$("#kiddo_gif").attr("src","img/waiting.gif");
                  });
                  numSpan.animate({
                      color: "white",
                      top : game.numberPositions[id][0],
                      left : game.numberPositions[id][1]
                  }, 1000 );
              }
            }
        });
        game.lastTime = time = setInterval(function(){
           if(game.gameIsIdle()){
              soundManager.play("your_turn");
              $("#kiddo_gif").attr("src","img/waiting.gif");
           }  
        },10000); 
        game.resize();
        window.onresize=game.resize;
    },
    addNumber : function(num){
       var id = "_drag_span_"+Math.floor(Math.random()*100000000);
       var numSpan = $("<span id='"+id+"'>");
       numSpan.html(num);
       numSpan.css("padding","20px");
       numSpan.css("cursor","move");
       numSpan.css("z-index","2");
       var width = game.getDim()[0];
       var height = game.getDim()[1];
       var inputTop = height*0.4;
       var inputHeight = height*0.02;
       $("#number_area").append(numSpan);
       $("#"+id).draggable({
          start: function() {
        	  this.style.color = "red";
        	  game.lastDrag = $.now();
          },
          drag : function(){
            if(!game.numberPositions[id]){
               game.numberPositions[id] = [this.style.top,this.style.left];
            }
          },
          stop : function(){
        	  var num = parseInt(numSpan.html());
        	  if(game.numberList.indexOf(num) === -1){
        		  numSpan.animate({
                    color: "white",
                    top : game.numberPositions[id][0],
                    left : game.numberPositions[id][1]
                }, 1000 );
        	  }
          }
       });
    },
    runLater : function(limit,func){
        return setTimeout(func,limit);      
    },
    shuffle : function (arr){ //v1.0
        for(var j, x, i = arr.length; i; j = Math.floor(Math.random() * i), x = arr[--i], arr[i] = arr[j], arr[j] = x);
        return arr;
    },
    getDim: function(){
    	var body = window.document.body;
    	var screenHeight;
    	var screenWidth;
    	if (window.innerHeight) {
    		screenHeight = window.innerHeight;
    		screenWidth = window.innerWidth;
    	} else if (body.parentElement.clientHeight) {
    		screenHeight = body.parentElement.clientHeight;
    		screenWidth = body.parentElement.clientWidth;
    	} else if (body && body.clientHeight) {
    		screenHeight = body.clientHeight;
    		screenWidth = body.clientWidth;
    	}
    	     return [screenWidth,screenHeight];        
    },
    resizable : function(resizable){
    	if(game.resizableIds.indexOf(resizable.id))
    		return;
    	game.resizables.push(resizable);
    },
    resize: function(){
              var arr=game.resizables;
              for(var index in arr){
             	   var obj=arr[index];
             	   var element=dom.el(obj.id);
             	   if(!element){
             		 console.log("Element "+obj.id+" not found!");
             		 continue;  
             	   }
             	   element.style.width=game.getDim()[0]*obj.width+"px";
                   element.style.height=game.getDim()[1]*obj.height+"px";
                   if(obj.style){
                	 for(var style in obj.style){
                		var factor=obj.style[style].factor;
                		var along=obj.style[style].along;
                		if(along==="height"){
                			element.style[style]=factor*game.getDim()[1]+"px";
                		}
                		else if(along==="width"){
                			element.style[style]=factor*game.getDim()[0]+"px";
                		}
                	 }  
                   }
                   
              }
              
            }
		
};