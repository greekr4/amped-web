0 : {
		move(0,1.2);
		changeFace(1);
		fadeinAnimation( 360.0 , 360.0 , 360.0 , 4.5 , 0, 0.2) ;
	}
250 : {
		move(0,3.5);
		makeShapeModel("/effect/spark.oa",0,-0.1,0.5,0,0,1,1);
		startAnimation( 360.0 , 361.0 , 361.0 , 10.0 , 0) ;
	}
370 : {
		attack("dronedashkick");
		makeShapeModel("/effect/flash.oa",0,0.0,0.7,0,0,1);
		startAnimation( 361.0 , 363.0 , 361.0 , 24.0 , 1) ;
	}
670 : {
		move(0,2.0);
		startAnimation( 363.0 , 364.0 , 364.0 , 4.0 , 0) ;
		nextTech();
	}
940 : {
		stop();
	}
980 : {
		recover();
	}
