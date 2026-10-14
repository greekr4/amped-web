0 : {
		stop();
		changeFace(2);
		fadeinAnimation( 245.0 , 245.0 , 245.0 , 4.5 , 0, 0.2) ;
		makeShapeModel("/effect/flash.oa",0,0.4,-0.3,0,1,0);
	}
220 : {
		startAnimation( 245.0 , 246.0 , 246.0 , 6.0 , 0) ;
	}
340 : {
		attack("dart");
		makeShapeModel("/effect/spark.oa",0,0.2,0.5,0,0,1);
		startAnimation( 246.0 , 248.0 , 246.0 , 24.0 , 1) ;
	}
700 : {
		nextTech();
	}
1000 : {
		recover();
	}
