0 : {
		stop();
		changeFace(2);
		fadeinAnimation( 380.0 , 380.0 , 380.0 , 4.5 , 0, 0.22) ;
	}
260 : {
		move(0,3.0);
		makeShapeModel("/effect/spark.oa",0,0.1,0.5,0,0,1,1);
		startAnimation( 380.0 , 381.0 , 381.0 , 8.0 , 0) ;
	}
380 : {
		attack("tackle");
		startAnimation( 381.0 , 383.0 , 381.0 , 24.0 , 1) ;
	}
750 : {
		fadeinAnimation( 384.0 , 384.0 , 384.0 , 3.0 , 0, 0.4) ;
	}
900 : {
		move(0,1.0);
	}
1300 : {
		startAnimation( 384.0 , 385.0 , 385.0 , 5.0 , 0) ;
	}
1450 : {
		recover();
	}
